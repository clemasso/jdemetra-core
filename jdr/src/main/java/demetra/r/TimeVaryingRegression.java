/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r;

import demetra.arima.ssf.SsfArima;
import jdplus.data.DataBlock;
import demetra.information.InformationMapping;
import jdplus.maths.functions.IParametricMapping;
import jdplus.maths.functions.ParamValidation;
import jdplus.maths.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.QuadraticForm;
import jdplus.maths.matrices.SymmetricMatrix;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaSpecification;
import demetra.descriptors.arima.SarimaDescriptor;
import demetra.descriptors.stats.DiffuseConcentratedLikelihoodDescriptor;
import demetra.likelihood.DiffuseConcentratedLikelihood;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.dk.SsfFunction;
import demetra.ssf.dk.SsfFunctionPoint;
import demetra.ssf.implementations.RegSsf;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.SsfData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsUnit;
import demetra.timeseries.calendars.DayClustering;
import demetra.modelling.regression.GenericTradingDaysVariable;
import demetra.modelling.regression.Regression;
import demetra.timeseries.TsData;
import java.util.LinkedHashMap;
import java.util.Map;
import demetra.processing.ProcResults;
import jdplus.sarima.estimation.SarimaMapping;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.data.DoubleSeq;
import demetra.data.Doubles;
import jdplus.maths.matrices.SubMatrix;
import demetra.maths.matrices.Matrix;
import demetra.modelling.spi.ArimaProcessorUtility;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class TimeVaryingRegression {

    @lombok.Data
    static class Airline {

        CanonicalMatrix td;
        double regVariance;
        double theta, btheta;
    }

    @lombok.Value
    @lombok.Builder
    public static class Results implements ProcResults {

        TsDomain domain;
        FastMatrix variables;
        FastMatrix coefficients;
        FastMatrix coefficientsStde;
        SarimaModel arima0, arima;
        DiffuseConcentratedLikelihood ll0;
        DiffuseConcentratedLikelihood ll;
        double nvar;

        private static final String ARIMA0 = "arima0", LL0 = "likelihood0",
                ARIMA = "arima", LL = "likelihood",
                STDCOEFF = "coefficients.stde", COEFF = "coefficients.value", TD = "td", TDEFFECT = "tdeffect";
        private static final InformationMapping<Results> MAPPING = new InformationMapping<>(Results.class);

        static {
            MAPPING.delegate(ARIMA0,SarimaDescriptor.getMapping(), r ->  ArimaProcessorUtility.convert(r.getArima0()));
            MAPPING.delegate(LL0, DiffuseConcentratedLikelihoodDescriptor.getMapping(), r -> r.getLl0());
            MAPPING.delegate(ARIMA, SarimaDescriptor.getMapping(), r -> ArimaProcessorUtility.convert(r.getArima()));
            MAPPING.delegate(LL, DiffuseConcentratedLikelihoodDescriptor.getMapping(), r -> r.getLl());
            MAPPING.set("aic0", Double.class, r -> r.getLl0().AIC(2));
            MAPPING.set("aic", Double.class, r -> r.getLl().AIC(3));
            MAPPING.set("tdvar", Double.class, r -> r.getNvar());
            MAPPING.set(COEFF, FastMatrix.class, r -> r.getCoefficients());
            MAPPING.set(STDCOEFF, FastMatrix.class, r -> r.getCoefficientsStde());
            MAPPING.set(TD, FastMatrix.class, r -> r.getVariables());
            MAPPING.set(TDEFFECT, TsData.class, r
                    -> {
                DataBlock tmp = DataBlock.make(r.getDomain().length());
                DataBlock prod = DataBlock.make(r.getDomain().length());
                for (int i = 0; i < r.variables.getColumnsCount(); ++i) {
                    prod.set(r.getCoefficients().column(i), r.getVariables().column(i), (a, b) -> a * b);
                    tmp.add(prod);
                }
                return TsData.ofInternal(r.getDomain().getStartPeriod(), tmp);
            });
        }

        @Override
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return MAPPING.getData(this, id, tclass);
        }

        public static final InformationMapping<Results> getMapping() {
            return MAPPING;
        }
    }

    public Results regarima(TsData s, String td, String svar, double aicdiff) {
        int freq = s.getTsUnit().ratioOf(TsUnit.YEAR);
        SarimaSpecification spec = new SarimaSpecification(freq);
        spec.airline(true);
        DayClustering dc = days(td);
        CanonicalMatrix mtd = generate(s.getDomain(), dc);
        CanonicalMatrix nvar = generateVar(dc, svar);
        SsfData data = new SsfData(s.getValues());

        LevenbergMarquardtMinimizer min = LevenbergMarquardtMinimizer
                .builder()
                .functionPrecision(1e-9)
                .build();

        // fixed model
        TDvarMapping mapping0 = new TDvarMapping(mtd, true);
        SsfFunction<Airline, ISsf> fn0 = buildFunction(data, spec, mapping0, mtd, nvar);
        min.minimize(fn0.evaluate(mapping0.getDefaultParameters()));
        SsfFunctionPoint<Airline, ISsf> rfn0 = (SsfFunctionPoint<Airline, ISsf>) min.getResult();

        // compute the unconstrained solution
        TDvarMapping mapping = new TDvarMapping(mtd, false);
        SsfFunction<Airline, ISsf> fn = buildFunction(data, spec, mapping, mtd, nvar);

        Airline air0 = rfn0.getCore();
        air0.setRegVariance(.001);
        min.minimize(fn.ssqEvaluate(mapping.parametersOf(air0)));
        SsfFunctionPoint<Airline, ISsf> rfn = (SsfFunctionPoint<Airline, ISsf>) min.getResult();

        // compute AIC
        double aic0 = rfn0.getLikelihood().AIC(2);
        double aic = rfn.getLikelihood().AIC(3);
        ISsf ssf;
        if (aic + aicdiff < aic0) {
            ssf = rfn.getSsf();
        } else {
            ssf = rfn0.getSsf();
        }

        DefaultSmoothingResults fs = DkToolkit.sqrtSmooth(ssf, data, true, true);
        CanonicalMatrix c = CanonicalMatrix.make(mtd.getRowsCount(), mtd.getColumnsCount() + 1);
        CanonicalMatrix ec = CanonicalMatrix.make(mtd.getRowsCount(), mtd.getColumnsCount() + 1);

        int del = freq + 2;
        double nwe = dc.getGroupCount(0);
        double[] z = new double[c.getColumnsCount() - 1];
        for (int i = 0; i < z.length; ++i) {
            c.column(i).copy(fs.getComponent(del + i));
            ec.column(i).copy(fs.getComponentVariance(del + i));
            z[i] = dc.getGroupCount(i + 1) / nwe;
            c.column(z.length).addAY(-z[i], c.column(i));
        }
        DataBlock Z = DataBlock.of(z);
        for (int i = 0; i < c.getRowsCount(); ++i) {
            SubMatrix var = fs.P(i).dropTopLeft(del, del);
            ec.set(i, z.length, QuadraticForm.apply(var, Z));
        }
        ec.apply(x -> x <= 0 ? 0 : Math.sqrt(x));

        Airline air = rfn.getCore();

        SarimaModel arima0 = SarimaModel.builder(spec)
                .theta(air0.theta)
                .btheta(air0.btheta)
                .build();
        SarimaModel arima = SarimaModel.builder(spec)
                .theta(air.theta)
                .btheta(air.btheta)
                .build();
        return Results.builder()
                .domain(s.getDomain())
                .arima0(arima0)
                .arima(arima)
                .ll0(rfn0.getLikelihood())
                .ll(rfn.getLikelihood())
                .nvar(air.regVariance)
                .variables(mtd)
                .coefficients(c)
                .coefficientsStde(ec)
                .build();
    }

    private SsfFunction<Airline, ISsf> buildFunction(SsfData data, SarimaSpecification spec, TDvarMapping mapping, CanonicalMatrix mtd, CanonicalMatrix nvar) {
        return SsfFunction.builder(data, mapping,
                params
                -> {
            SarimaModel arima = SarimaModel.builder(spec)
                    .theta(params.getTheta())
                    .btheta(params.getBtheta())
                    .build();
            ISsf ssf = SsfArima.of(arima);
            double nv = params.getRegVariance();
            CanonicalMatrix v = nvar.deepClone();
            v.mul(nv);
            return RegSsf.ofTimeVarying(ssf, mtd, v);
        }).build();
    }

    private DayClustering days(String td) {
        DayClustering dc;
        switch (td) {
            case "TD2":
                dc = DayClustering.TD2;
                break;
            case "TD3":
                dc = DayClustering.TD3;
                break;
            case "TD3c":
                dc = DayClustering.TD3c;
                break;
            case "TD4":
                dc = DayClustering.TD4;
                break;
            default:
                dc = DayClustering.TD7;
                break;
        }
        return dc;
    }

    public CanonicalMatrix generate(TsDomain domain, DayClustering dc) {
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        return Regression.matrix(domain, new GenericTradingDaysVariable(gtd));
    }

    public CanonicalMatrix generateVar(DayClustering dc, String var) {
        int groupsCount = dc.getGroupsCount();
        CanonicalMatrix full = CanonicalMatrix.square(7);
        if (!var.equalsIgnoreCase("Contrasts")) {
            full.set(-1.0 / 7.0);
        }
        full.diagonal().add(1);
        CanonicalMatrix Q = CanonicalMatrix.make(groupsCount - 1, 7);
        int[] gdef = dc.getGroupsDefinition();
        for (int i = 1; i < groupsCount; ++i) {
            for (int j = 0; j < 7; ++j) {
                if (gdef[j] == i) {
                    Q.set(i - 1, j, 1);
                }
            }
        }
        return SymmetricMatrix.XSXt(full, Q);
    }

    private static class TDvarMapping implements IParametricMapping<Airline> {

        private final CanonicalMatrix td;
        private final boolean fixed;
        private static final SarimaMapping airlineMapping;

        static {
            SarimaSpecification spec = new SarimaSpecification(12);
            spec.airline(true);
            airlineMapping = SarimaMapping.of(spec);
        }

        TDvarMapping(CanonicalMatrix td, boolean fixed) {
            this.td = td;
            this.fixed = fixed;
        }

        public CanonicalMatrix getTd() {
            return td;
        }

        @Override
        public Airline map(DoubleSeq p) {
            Airline airline = new Airline();
            airline.setTd(td);
            airline.setTheta(p.get(0));
            airline.setBtheta(p.get(1));
            if (fixed) {
                airline.setRegVariance(0);
            } else {
                airline.setRegVariance(p.get(2));
            }
            return airline;
        }

        public DoubleSeq parametersOf(Airline t) {
            double[] p = new double[fixed ? 2 : 3];
            p[0] = t.getTheta();
            p[1] = t.getBtheta();
            if (!fixed) {
                p[2] = t.getRegVariance();
            }
            return DoubleSeq.of(p);
        }

        @Override
        public boolean checkBoundaries(DoubleSeq inparams) {
            if (fixed) {
                return airlineMapping.checkBoundaries(inparams.extract(0, 2));
            } else {
                return inparams.get(2) >= 0 && airlineMapping.checkBoundaries(inparams.extract(0, 2));
            }
        }

        @Override
        public double epsilon(DoubleSeq inparams, int idx) {
            if (idx < 2) {
                return airlineMapping.epsilon(inparams, idx);
            }
            return inparams.get(2) * .001;
        }

        @Override
        public int getDim() {
            return fixed ? 2 : 3;
        }

        @Override
        public double lbound(int idx) {
            if (idx < 2) {
                return airlineMapping.lbound(idx);
            } else {
                return 0;
            }
        }

        @Override
        public double ubound(int idx) {
            if (idx < 2) {
                return airlineMapping.ubound(idx);
            } else {
                return 10;
            }
        }

        @Override
        public ParamValidation validate(DataBlock ioparams) {
            ParamValidation pv = ParamValidation.Valid;
            if (!fixed && ioparams.get(2) < 0) {
                pv = ParamValidation.Changed;
                ioparams.set(2, Math.min(10, -ioparams.get(2)));
            }
            if (!fixed && ioparams.get(2) > 10) {
                pv = ParamValidation.Changed;
                ioparams.set(2, 10);
            }
            ParamValidation pv2 = airlineMapping.validate(ioparams.extract(0, 2));
            if (pv == ParamValidation.Valid && pv2 == ParamValidation.Valid) {
                return ParamValidation.Valid;
            }
            if (pv == ParamValidation.Invalid || pv2 == ParamValidation.Invalid) {
                return ParamValidation.Invalid;
            }
            return ParamValidation.Changed;
        }

        @Override
        public String getDescription(int idx) {
            if (idx < 2) {
                return airlineMapping.getDescription(idx);
            } else {
                return "noise stdev";
            }
        }

        @Override
        public DoubleSeq getDefaultParameters() {
            return fixed ? Doubles.of(new double[]{-.6, -.6}) : Doubles.of(new double[]{-.6, -.6, .001});
        }
    }

}

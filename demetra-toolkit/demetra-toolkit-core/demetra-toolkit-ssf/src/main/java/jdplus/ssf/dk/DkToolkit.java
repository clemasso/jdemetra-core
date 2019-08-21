/*
 * Copyright 2016 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.ssf.dk;

import demetra.likelihood.DiffuseConcentratedLikelihood;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.data.DataBlockStorage;
import jdplus.maths.functions.IParametricMapping;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import jdplus.maths.matrices.UpperTriangularMatrix;
import jdplus.maths.matrices.decomposition.Householder;
import jdplus.ssf.dk.sqrt.DiffuseSquareRootInitializer;
import jdplus.ssf.ResultsRange;
import jdplus.ssf.ckms.CkmsDiffuseInitializer;
import jdplus.ssf.ckms.CkmsFilter;
import jdplus.ssf.dk.sqrt.CompositeDiffuseSquareRootFilteringResults;
import jdplus.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import jdplus.ssf.dk.sqrt.DiffuseSquareRootSmoother;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.IConcentratedLikelihoodComputer;
import jdplus.ssf.univariate.ILikelihoodComputer;
import jdplus.ssf.univariate.ISmoothingResults;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfBuilder;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.OrdinaryFilter;
import jdplus.ssf.univariate.SsfRegressionModel;
import demetra.data.LogSign;
import jdplus.ssf.StateInfo;
import jdplus.ssf.StateStorage;
import jdplus.ssf.dk.sqrt.IDiffuseSquareRootFilteringResults;
import jdplus.ssf.likelihood.MarginalLikelihood;
import jdplus.ssf.multivariate.IMultivariateSsf;
import jdplus.ssf.multivariate.IMultivariateSsfData;
import jdplus.ssf.multivariate.M2uAdapter;
import jdplus.ssf.univariate.IFilteringResults;
import demetra.data.DoubleSeq;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class DkToolkit {

    /**
     * Diffuse likelihood (see Durbin-Koopman)
     * @param ssf State space form
     * @param data Data
     * @param scalingfactor True if the likelihood is defined up to a scaling factor
     * @param res True if the likelihood will contain the residuals
     * @return 
     */
    public DiffuseLikelihood likelihood(ISsf ssf, ISsfData data, boolean scalingfactor, boolean res) {
        return likelihoodComputer(true, scalingfactor, res).compute(ssf, data);
    }

    /**
     * Marginal likelihood (see Franke...)
     * @param ssf State space form
     * @param data Data
     * @param scalingfactor True if the likelihood is defined up to a scaling factor
     * @param res True if the likelihood will contain the residuals
     * @return 
     */
    public MarginalLikelihood marginalLikelihood(ISsf ssf, ISsfData data, boolean scalingfactor, boolean res) {
        return new MLLComputer(scalingfactor, res).compute(ssf, data);
    }

   public DiffuseLikelihood likelihood(IMultivariateSsf ssf, IMultivariateSsfData data, boolean scalingfactor, boolean res) {
        ISsf ussf = M2uAdapter.of(ssf);
        ISsfData udata = M2uAdapter.of(data);
        return likelihoodComputer(true, scalingfactor, res).compute(ussf, udata);
   }
        
     /**
     * Diffuse likelihood computer (see Durbin-Koopman)
     * @param sqr True if the square root initialization is used
     * @param scalingfactor True if the likelihood is defined up to a scaling factor
     * @param res True if the likelihood will contain the residuals
     * @return 
     */
    public ILikelihoodComputer<DiffuseLikelihood> likelihoodComputer(boolean sqr, boolean scalingfactor, boolean res) {
        return sqr ? new LLComputer2(scalingfactor, res) : new LLComputer1(scalingfactor, res);
    }

    /**
     * Diffuse concentrated likelihood computer (see Durbin-Koopman)
     * @param sqr True if the square root initialization is used
     * @param fast Fast (Ckms) processing (if possible)
     * @param scalingfactor True if the likelihood is defined up to a scaling factor
     * @return 
     */
    public IConcentratedLikelihoodComputer<DiffuseConcentratedLikelihood> concentratedLikelihoodComputer(boolean sqr, boolean fast, boolean scalingfactor) {
        return new CLLComputer(sqr, fast, scalingfactor);
    }

    public DefaultDiffuseFilteringResults filter(ISsf ssf, ISsfData data, boolean all) {
        DefaultDiffuseFilteringResults frslts = all
                ? DefaultDiffuseFilteringResults.full() : DefaultDiffuseFilteringResults.light();
        frslts.prepare(ssf, 0, data.length());
        DurbinKoopmanInitializer initializer = new DurbinKoopmanInitializer(frslts);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        filter.process(ssf, data, frslts);
        return frslts;
    }

    public DefaultDiffuseSquareRootFilteringResults sqrtFilter(ISsf ssf, ISsfData data, boolean all) {
        DefaultDiffuseSquareRootFilteringResults frslts = all
                ? DefaultDiffuseSquareRootFilteringResults.full() : DefaultDiffuseSquareRootFilteringResults.light();
        frslts.prepare(ssf, 0, data.length());
        DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(frslts);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        filter.process(ssf, data, frslts);
        return frslts;
    }

    public void sqrtFilter(ISsf ssf, ISsfData data, IFilteringResults frslts, boolean all) {
        DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(null);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        filter.process(ssf, data, frslts);
    }

    public void sqrtFilter(ISsf ssf, ISsfData data, IDiffuseSquareRootFilteringResults frslts, boolean all) {
        DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(frslts);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        filter.process(ssf, data, frslts);
    }


    /**
     * 
     * @param ssf State space form
     * @param data Data
     * @param all Computes also the variances
     * @param rescaleVariance If true, the variances are rescaled using the estimation done in the 
     * filtering phase. Otherwise, the raw variances are returned.
     * @return 
     */
    public DefaultSmoothingResults smooth(ISsf ssf, ISsfData data, boolean all, boolean rescaleVariance) {
        DiffuseSmoother smoother = DiffuseSmoother
                .builder(ssf)
                .calcVariance(all)
                .rescaleVariance(rescaleVariance)
                .build();
        DefaultSmoothingResults sresults = all ? DefaultSmoothingResults.full()
                : DefaultSmoothingResults.light();
        sresults.prepare(ssf.getStateDim(), 0, data.length());
        if (smoother.process(data, sresults)) {
            return sresults;
        } else {
            return null;
        }
    }

    /**
     * 
     * @param ssf State space form
     * @param data Data
     * @param all Computes also the variances
     * @param rescaleVariance If true, the variances are rescaled using the estimation done in the 
     * filtering phase. Otherwise, the raw variances are returned.
     * @return 
     */
    public StateStorage smooth(IMultivariateSsf ssf, IMultivariateSsfData data, boolean all, boolean rescaleVariance) {
        ISsf ussf = M2uAdapter.of(ssf);
        ISsfData udata = M2uAdapter.of(data);
        DefaultSmoothingResults sr = sqrtSmooth(ussf, udata, all, rescaleVariance);
        StateStorage ss = all ? StateStorage.full(StateInfo.Smoothed) : StateStorage.light(StateInfo.Smoothed);
        int m = data.getVarsCount(), n = data.getObsCount();
        ss.prepare(ussf.getStateDim(), 0, n);
        if (all) {
            for (int i = 0; i < n; ++i) {
                ss.save(i, sr.a(i * m), sr.P(i * m));
            }
        } else {
            for (int i = 0; i < n; ++i) {
                ss.save(i, sr.a(i * m), null);
            }
        }
        return ss;
    }

     /**
     * 
     * @param ssf State space form
     * @param data Data
     * @param sresults Storage for the results. The variances are computed or not following the properties of the storage
      * @param rescaleVariance If true, the variances are rescaled using the estimation done in the 
     * filtering phase. Otherwise, the raw variances are returned.
     * @return 
     */
   public static boolean smooth(ISsf ssf, ISsfData data, ISmoothingResults sresults, boolean rescaleVariance) {
        boolean all = sresults.hasVariances();
        DiffuseSmoother smoother = DiffuseSmoother
                .builder(ssf)
                .calcVariance(all)
                .rescaleVariance(rescaleVariance)
                .build();
        return smoother.process(data, sresults);
    }

    /**
     * Fast smoothing (using disturbance smoother)
     * @param ssf
     * @param data
     * @return 
     */
    public static DataBlockStorage fastSmooth(ISsf ssf, ISsfData data) {
        FastStateSmoother smoother = new FastStateSmoother(ssf);
        return smoother.process(data);
    }

    public static DefaultSmoothingResults sqrtSmooth(ISsf ssf, ISsfData data, boolean all, boolean rescaleVariance) {
        DiffuseSquareRootSmoother smoother = DiffuseSquareRootSmoother
                .builder(ssf)
                .calcVariance(all)
                .rescaleVariance(rescaleVariance)
                .build();
        DefaultSmoothingResults sresults = all ? DefaultSmoothingResults.full()
                : DefaultSmoothingResults.light();
        sresults.prepare(ssf.getStateDim(), 0, data.length());
        if (smoother.process(data, sresults)) {
            return sresults;
        } else {
            return null;
        }
    }

    public static boolean sqrtSmooth(ISsf ssf, ISsfData data, ISmoothingResults sresults, boolean rescaleVariance) {
        boolean all = sresults.hasVariances();
        DiffuseSquareRootSmoother smoother = DiffuseSquareRootSmoother
                .builder(ssf)
                .calcVariance(all)
                .rescaleVariance(rescaleVariance)
                .build();
        return smoother.process(data, sresults);
    }

    private static class LLComputer1 implements ILikelihoodComputer<DiffuseLikelihood> {

        private final boolean scalingfactor, res;

        LLComputer1(boolean scalingfactor, boolean res) {
            this.res = res;
            this.scalingfactor=scalingfactor;
        }

        @Override
        public DiffuseLikelihood compute(ISsf ssf, ISsfData data) {

            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(res);
            if (res) {
                pe.prepare(ssf, data.length());
            }
            DurbinKoopmanInitializer initializer = new DurbinKoopmanInitializer(pe);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, pe);
            return pe.likelihood(scalingfactor);
        }

    }

    private static class LLComputer2 implements ILikelihoodComputer<DiffuseLikelihood> {

        private final boolean scalingfactor, res;

        LLComputer2(boolean scalingfactor, boolean res) {
            this.res = res;
            this.scalingfactor=scalingfactor;
        }


        @Override
        public DiffuseLikelihood compute(ISsf ssf, ISsfData data) {

            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(res);
            if (res) {
                pe.prepare(ssf, data.length());
            }
            DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(pe);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, pe);
            return pe.likelihood(scalingfactor);
        }

        public MarginalLikelihood mcompute(ISsf ssf, ISsfData data, boolean scalingfactor) {

            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(res);
            if (res) {
                pe.prepare(ssf, data.length());
            }
            DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(pe);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, pe);
            DiffuseLikelihood likelihood = pe.likelihood(scalingfactor);
            int collapsing = pe.getEndDiffusePosition();
            CanonicalMatrix M = CanonicalMatrix.make(collapsing, ssf.getDiffuseDim());
            ssf.diffuseEffects(M);
            int j = 0;
            for (int i = 0; i < collapsing; ++i) {
                if (!data.isMissing(i)) {
                    if (i > j) {
                        M.row(j).copy(M.row(i));
                    }
                    j++;
                }
            }
            Householder hous = new Householder();
            hous.decompose(M.extract(0, j, 0, M.getColumnsCount()));
            double mc = 2 * LogSign.of(hous.rdiagonal(true)).getValue();
            return MarginalLikelihood.builder(likelihood.dim(), likelihood.getD())
                    .concentratedScalingFactor(scalingfactor)
                    .diffuseCorrection(likelihood.getDiffuseCorrection())
                    .legacy(false)
                    .logDeterminant(likelihood.logDeterminant())
                    .ssqErr(likelihood.ssq())
                    .residuals(pe.errors(true, true))
                    .marginalCorrection(mc)
                    .build();
        }
    }

    private static class MLLComputer implements ILikelihoodComputer<MarginalLikelihood> {

        private final boolean res, scalingfactor;

        MLLComputer(boolean scalingfactor, boolean res) {
            this.res = res;
            this.scalingfactor = scalingfactor;
        }

        @Override
        public MarginalLikelihood compute(ISsf ssf, ISsfData data) {

            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(res);
            if (res) {
                pe.prepare(ssf, data.length());
            }
            DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(pe);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, pe);
            DiffuseLikelihood likelihood = pe.likelihood(scalingfactor);
            int collapsing = pe.getEndDiffusePosition();
            CanonicalMatrix M = CanonicalMatrix.make(collapsing, ssf.getDiffuseDim());
            ssf.diffuseEffects(M);
            int j = 0;
            for (int i = 0; i < collapsing; ++i) {
                if (!data.isMissing(i)) {
                    if (i > j) {
                        M.row(j).copy(M.row(i));
                    }
                    j++;
                }
            }
            Householder hous = new Householder();
            hous.decompose(M.extract(0, j, 0, M.getColumnsCount()));
            double mc = 2 * LogSign.of(hous.rdiagonal(true)).getValue();
            return MarginalLikelihood.builder(likelihood.dim(), likelihood.getD())
                    .concentratedScalingFactor(scalingfactor)
                    .diffuseCorrection(likelihood.getDiffuseCorrection())
                    .legacy(false)
                    .logDeterminant(likelihood.logDeterminant())
                    .ssqErr(likelihood.ssq())
                    .residuals(pe.errors(true, true))
                    .marginalCorrection(mc)
                    .build();
        }
    }

    private static class CLLComputer implements IConcentratedLikelihoodComputer<DiffuseConcentratedLikelihood> {

        private final boolean sqr, fast, scaling;

        private CLLComputer(boolean sqr, boolean fast, boolean scaling) {
            this.sqr = sqr;
            this.fast = fast;
            this.scaling = scaling;
        }

        @Override
        public DiffuseConcentratedLikelihood compute(SsfRegressionModel model) {
            ISsfData y = model.getY();
            int n = y.length();
            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(true);
            pe.prepare(model.getSsf(), n);
            DkFilter filter = filteringResults(model.getSsf(), y, pe);
            DiffuseLikelihood ll = pe.likelihood(scaling);
            DoubleSeq yl = pe.errors(true, true);
            int nl = yl.length();
            CanonicalMatrix xl = xl(model, filter, nl);
            if (xl == null) {
                return DiffuseConcentratedLikelihood.builder(ll.dim(), ll.getD())
                        .ssqErr(ll.ssq())
                        .logDeterminant(ll.logDeterminant())
                        .logDiffuseDeterminant(ll.getDiffuseCorrection())
                        .residuals(yl)
                        .scalingFactor(scaling)
                        .build();
            } else {
                Householder qr = new Householder();
                qr.decompose(xl);
                if (qr.rank() == 0) {
                    return DiffuseConcentratedLikelihood.builder(ll.dim(), ll.getD())
                            .ssqErr(ll.ssq())
                            .logDeterminant(ll.logDeterminant())
                            .logDiffuseDeterminant(ll.getDiffuseCorrection())
                            .residuals(yl)
                            .scalingFactor(scaling)
                            .build();
                } else {
                    int rank = qr.rank();
                    DataBlock b = DataBlock.make(rank);
                    DataBlock res = DataBlock.make(nl - rank);
                    qr.leastSquares(yl, b, res);
                    double ssqerr = res.ssq();
                    CanonicalMatrix u = UpperTriangularMatrix.inverse(qr.r(true));
                    int[] unused = qr.unused();
                    // expand the results, if need be
                    b = expand(b, unused);
                    u = expand(u, unused);
                    // initializing the results...
                    int nobs = ll.dim();
                    int d = ll.getD();
                    int[] idiffuse = model.getDiffuseElements();
                    double ldet = ll.logDeterminant(), dcorr = ll.getDiffuseCorrection();
                    if (idiffuse != null) {
                        DoubleSeq rdiag = qr.rdiagonal(true);
                        double lregdet = 0;
                        int ndc = 0;
                        for (int i = 0; i < idiffuse.length; ++i) {
                            if (isUsed(idiffuse[i], unused)) {
                                lregdet += Math.log(Math.abs(rdiag
                                        .get(idiffuse[i])));
                                ++ndc;
                            }
                        }
                        lregdet *= 2;
                        dcorr += lregdet;
                        d += ndc;
                    }
                    CanonicalMatrix bvar = SymmetricMatrix.UUt(u);
                    return DiffuseConcentratedLikelihood.builder(nobs, d)
                            .ssqErr(ssqerr)
                            .logDeterminant(ldet)
                            .logDiffuseDeterminant(dcorr)
                            .residuals(res)
                            .coefficients(b)
                            .unscaledCovariance(bvar)
                            .scalingFactor(scaling)
                            .build();
                }
            }
        }

        private DataBlock expand(DataBlock x, int[] unused) {
            if (unused == null) {
                return x;
            }
            double[] bc = new double[x.length() + unused.length];
            for (int i = 0, j = 0, k = 0; i < bc.length; ++i) {
                if (k < unused.length && i == unused[k]) {
                    ++k;
                } else {
                    bc[i] = x.get(j);
                    ++j;
                }
            }
            return DataBlock.of(bc);
        }

        private CanonicalMatrix expand(CanonicalMatrix v, int[] unused) {
            if (unused == null) {
                return v;
            }
            int nx = v.getColumnsCount() + unused.length;
            CanonicalMatrix bvar = CanonicalMatrix.square(nx);
            for (int i = 0, j = 0, k = 0; i < nx; ++i) {
                if (k < unused.length && i == unused[k]) {
                    ++k;
                } else {
                    for (int ci = 0, cj = 0, ck = 0; ci <= i; ++ci) {
                        if (ck < unused.length && ci == unused[ck]) {
                            ++ck;
                        } else {
                            double d = v.get(j, cj);
                            bvar.set(i, ci, d);
                            bvar.set(ci, i, d);
                            ++cj;
                        }
                    }
                    ++j;
                }
            }
            return bvar;
        }

        private DkFilter filteringResults(ISsf ssf, ISsfData data, DiffusePredictionErrorDecomposition pe) {
            if (sqr) {
                DefaultDiffuseSquareRootFilteringResults fr = DefaultDiffuseSquareRootFilteringResults.light();
                fr.prepare(ssf, 0, data.length());
                CompositeDiffuseSquareRootFilteringResults dr = new CompositeDiffuseSquareRootFilteringResults(fr, pe);
                DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(dr);
                if (fast) {
                    CkmsDiffuseInitializer ff = new CkmsDiffuseInitializer(initializer);
                    CkmsFilter ffilter = new CkmsFilter(ff);
                    ffilter.process(ssf, data, dr);
                    ResultsRange range = new ResultsRange(0, data.length());
                    return new DkFilter(ssf, fr, range);
                } else {
                    OrdinaryFilter filter = new OrdinaryFilter(initializer);
                    filter.process(ssf, data, dr);
                    ResultsRange range = new ResultsRange(0, data.length());
                    return new DkFilter(ssf, fr, range);
                }
            } else {
                DefaultDiffuseFilteringResults fr = DefaultDiffuseFilteringResults.light();
                fr.prepare(ssf, 0, data.length());
                CompositeDiffuseFilteringResults dr = new CompositeDiffuseFilteringResults(fr, pe);
                DurbinKoopmanInitializer initializer = new DurbinKoopmanInitializer(dr);
                if (fast) {
                    CkmsDiffuseInitializer ff = new CkmsDiffuseInitializer(initializer);
                    CkmsFilter ffilter = new CkmsFilter(ff);
                    ffilter.process(ssf, data, dr);
                    ResultsRange range = new ResultsRange(0, data.length());
                    return new DkFilter(ssf, fr, range);
                } else {
                    OrdinaryFilter filter = new OrdinaryFilter(initializer);
                    filter.process(ssf, data, dr);
                    ResultsRange range = new ResultsRange(0, data.length());
                    return new DkFilter(ssf, fr, range);
                }
            }
        }

        private CanonicalMatrix xl(SsfRegressionModel model, DkFilter lp, int nl) {
            FastMatrix x = model.getX();
            if (x == null) {
                return null;
            }
            CanonicalMatrix xl = CanonicalMatrix.make(nl, x.getColumnsCount());
            DataBlockIterator lcols = xl.columnsIterator();
            DataBlockIterator cols = x.columnsIterator();
            while (cols.hasNext() && lcols.hasNext()) {
                lp.apply(cols.next(), lcols.next());
            }
            return xl;
        }

        private static boolean isUsed(final int i, final int[] unused) {
            if (unused == null)
                return true;
            for (int j = 0; j < unused.length; ++j) {
                if (unused[j] == i) {
                    return false;
                }
            }
            return true;
        }
    }

 
}

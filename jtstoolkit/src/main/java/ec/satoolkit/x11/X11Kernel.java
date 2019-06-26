/*
 * Copyright 2013 National Bank of Belgium
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
package ec.satoolkit.x11;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.ISeriesDecomposer;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 * The task of an X11Kernel is the execution of the X11 algorithm, in
 * collaboration with the modules provided by an X11Toolkit.
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class X11Kernel implements ISeriesDecomposer {

    public static final String A = "a-tables", B = "b-tables", C = "c-tables",
            D = "d-tables", E = "e-tables", F = "f-tables";
    public static final String A1 = "a1", A1a = "a1a", A1b = "a1b",
            A6 = "a6", A7 = "a7",
            A8 = "a8", A8t = "a8t",
            A8s = "a8s", A8i = "a8i", A9 = "a9", A9u = "a9u", A9sa = "a9sa", A9ser = "a9ser";
    public static final String[] ALL_A = {A1, A1a, A1b, A6, A7, A8, A8t, A8s, A8i, A9, A9sa, A9u, A9ser};
    public static final String B1 = "b1", B2 = "b2", B3 = "b3",
            B3TEST = "b3-seasonalityTest", B4 = "b4", B5 = "b5", B6 = "b6",
            B7 = "b7", B7_IC = "b7-IC ratio", B8 = "b8", B9 = "b9",
            B10 = "b10", B11 = "b11", B12 = "b12", B13 = "b13", B14 = "b14",
            B15 = "b15", B16 = "b16", B17 = "b17", B18 = "b18", B19 = "b19",
            B20 = "b20";
    public static final String[] ALL_B = {B1, B2, B3, B4, B5, B6, B7, B8, B9,
        B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20};
    public static final String C1 = "c1", C2 = "c2", C3 = "c3",
            C3TEST = "c3-seasonalityTest", C4 = "c4", C5 = "c5", C6 = "c6",
            C7 = "c7", C7_IC = "c7-IC ratio", C8 = "c8", C9 = "c9",
            C10 = "c10", C11 = "c11", C12 = "c12", C13 = "c13", C14 = "c14",
            C15 = "c15", C16 = "c16", C17 = "c17", C18 = "c18", C19 = "c19",
            C20 = "c20";
    public static final String[] ALL_C = {C1, C2, C3, C4, C5, C6, C7, C8, C9, C10,
        C11, C12, C13, C14, C15, C16, C17, C18, C19, C20};
    public static final String D1 = "d1", D2 = "d2", D3 = "d3",
            D3TEST = "d3-seasonalityTest", D4 = "d4", D5 = "d5", D6 = "d6",
            D7 = "d7", D7_IC = "d7-IC ratio", D8 = "d8", D9 = "d9",
            D10 = "d10", D10a = "d10a", D10b = "d10b", D11 = "d11", D11a = "d11a", D12 = "d12", D12a = "d12a", D12_IC = "d12-IC ratio",
            D13 = "d13", D14 = "d14", D15 = "d15", D16 = "d16", D16a = "d16a", D16b = "d16b", D17 = "d17",
            D18 = "d18", D19 = "d19", D20 = "d20", D9_RMS = "finalRMS",
            D9_RMSROUND = "rmsRounds", D9_SLEN = "slen",
            D10L = "d10_lin", D11L = "d11_lin", D12L = "d12_lin", D13L = "d13_lin",
            D10aL = "d10a_lin", D11aL = "d11a_lin", D12aL = "d12a_lin", D13aL = "d13a_lin",
            D9_DEFAULT = "s3x5default", D9_FILTER = "d9filter", D12_FILTER = "d12filter", D12_TLEN = "tlen", D9_FILTER_COMPOSIT = "d9filtercomposit";
    public static final String[] ALL_D = {D1, D2, D3, D4, D5, D6, D7, D8, D9,
        D10, D10a, D10b, D11, D11a, D12, D12a, D13, D14, D15, D16, D16a, D16b, D18, D19, D20};
    public static final String E1 = "e1", E2 = "e2", E3 = "e3", E11 = "e11";
    public static final String[] ALL_E = {E1, E2, E3, E11};
    private IX11Toolkit toolkit;
    private TsData refSeries, refCorrection;
    //private TsData correctionFactors;
    private boolean uscbLike = true;
    private IExtremeValuesCorrector ecorr;

    /**
     *
     * @param s
     *
     * @return
     */
    @Override
    public X11Results process(final TsData s) {
        if (toolkit == null) {
            toolkit = X11Toolkit.create(new X11Specification());
        }
        InformationSet info = new InformationSet();
        X11Context context = toolkit.getContext();
        context.check(s);
        DecompositionMode mode = toolkit.getContext().getMode();
        // build the modules
        info.subSet(A).set(A1, s);

        stepA(info);
        stepB(info);
        stepC(info);
        stepD(info);
        stepE(info);
        stepF(info);

        X11Results results = new X11Results(mode, info);
        return results;
    }

    /**
     * @return the toolkit
     */
    public IX11Toolkit getToolkit() {
        return toolkit;
    }

    /**
     * @param toolkit the toolkit to set
     */
    public void setToolkit(IX11Toolkit toolkit) {
        this.toolkit = toolkit;
    }

    private void stepA(InformationSet info) {
        if (toolkit.getPreprocessor() != null) {
            toolkit.getPreprocessor().preprocess(info);
        } else {
            TsData a1 = info.subSet(A).get(A1, TsData.class);
            info.subSet(B).set(B1, a1);
        }
    }

    private void stepB(InformationSet info) {
        InformationSet btables = info.subSet(B);

        TsData b1 = btables.get(B1, TsData.class);
        refSeries=toolkit.getContext().isLogAdditive() ? b1.log() : b1;
        // build the modules
        // b2
        TsData b2 = toolkit.getTrendCycleComputer().doInitialFiltering(
                X11Step.B, refSeries, info);

        // b3
        TsData b3 = toolkit.getContext().op(refSeries, b2);

        // b4
        TsData b4a = toolkit.getSeasonalComputer().doInitialFiltering(
                X11Step.B, b3, info);
        TsData b4anorm = toolkit.getSeasonalNormalizer().normalize(b4a, null);
        TsData b4d;
        if (toolkit.getContext().isPseudoAdditive()) {
            b4d = b3.clone();
            b4d.apply(b4anorm, (x, y) -> x - y + 1);
        } else {
            b4d = toolkit.getContext().op(b3, b4anorm);
        }

        ecorr = toolkit.getExtremeValuesCorrector();
//        ecorr.setForecasthorizont(toolkit.getContext().getForecastHorizon());
        if (ecorr instanceof CochranDependentExtremeValuesCorrector) {
            ((CochranDependentExtremeValuesCorrector) ecorr).testCochran(b4d);
        }

        ecorr.analyse(b4d);

        TsData b4 = ecorr.computeCorrections(b3);
        TsData b4g = ecorr.applyCorrections(b3, b4);

        TsData b5a = toolkit.getSeasonalComputer().doInitialFiltering(
                X11Step.B, b4g, info);
        TsData b5 = toolkit.getSeasonalNormalizer().normalize(b5a,
                refSeries.getDomain());

        TsData b6;
        if (toolkit.getContext().isPseudoAdditive()) {
            b6 = toolkit.getContext().pseudoOp(refSeries, b2, b5);
        } else {
            b6 = toolkit.getContext().op(refSeries, b5);
        }

        TsData b7 = toolkit.getTrendCycleComputer().doFinalFiltering(X11Step.B,
                b6, info);
        if (toolkit.getContext().isMultiplicative() || toolkit.getContext().isPseudoAdditive()) {
            toolkit.getUtilities().checkPositivity(b7);
        }
        TsData b8 = toolkit.getContext().op(refSeries, b7);

        TsData b9a = toolkit.getSeasonalComputer().doFinalFiltering(X11Step.B,
                b8, info);
        TsData b9c = toolkit.getSeasonalNormalizer().normalize(b9a, null);
        TsData b9d;
        if (toolkit.getContext().isPseudoAdditive()) {
            b9d = b8.clone();
            b9d.apply(b9c, (x, y) -> x - y + 1);
        } else {
            b9d = toolkit.getContext().op(b8, b9c);
        }

        ecorr.analyse(b9d);
        TsData b9 = ecorr.computeCorrections(b8);
        TsData b9g = ecorr.applyCorrections(b8, b9);

        TsData b10a = toolkit.getSeasonalComputer().doFinalFiltering(X11Step.B,
                b9g, info);
        TsData b10 = toolkit.getSeasonalNormalizer().normalize(b10a, null);

        TsData b11;
        if (toolkit.getContext().isPseudoAdditive()) {
            b11 = refSeries.minus(b7.times(b10.minus(1)));
        } else {
            b11 = toolkit.getContext().op(refSeries, b10);
        }
        TsData b13 = toolkit.getContext().op(b11, b7);

        TsData next = b13;
        /*
         * X11Series B16 = null; if (m_params.isTradingDayRegression()) {
         * CalendarEffect ce = new CalendarEffect(m_params.getFrequency()); if
         * (m_params.hasAprioriTD())
         * ce.setAprioriCoefficients(m_params.getAprioriTD());
         *
         * ce.setMultiplicativeModel(m_params.isMultiplicative()); X11Series
         * B16bis = ce.computeTradingDayEffect(B13); m_res.m_tdreg =
         * ce.getTDRegression();
         *
         * m_res.add("B14", ce.getTable("B14"),
         * "Irregular component values excluded from Trading Day Regression");
         *
         * m_res.add("B16bis", B16bis,
         * "Irregular component corrected for trading day effects"); next =
         * B16bis;
         *
         * B16 = ce.getTable("B16"); m_res.add("B16", B16,
         * "Adjustment Coefficients for trading day effects from the regression"
         * ); }
         */
        if (ecorr instanceof CochranDependentExtremeValuesCorrector) {
            ((CochranDependentExtremeValuesCorrector) ecorr).testCochran(next);
        }
        ecorr.analyse(next);
        TsData b17 = ecorr.getObservationWeights();
        TsData b20 = ecorr.getCorrectionFactors();
        refCorrection=b20;

        // Correct log-additive series
        if (toolkit.getContext().isLogAdditive()){
            b2.applyOnFinite(x->Math.exp(x));
            b3.applyOnFinite(x->Math.exp(x));
            b4.applyOnFinite(x->Math.exp(x));
            b5.applyOnFinite(x->Math.exp(x));
            b6.applyOnFinite(x->Math.exp(x));
            b7.applyOnFinite(x->Math.exp(x));
            b8.applyOnFinite(x->Math.exp(x));
            b9.applyOnFinite(x->Math.exp(x));
            b10.applyOnFinite(x->Math.exp(x));
            b11.applyOnFinite(x->Math.exp(x));
            b13.applyOnFinite(x->Math.exp(x));
            b20=b20.exp();
        }
        
        btables.set(B2, b2);
        btables.set(B3, b3);
        // btables.set(B3TEST, b3test);
        btables.set(B4, b4);
        btables.set(B5, b5);
        btables.set(B6, b6);
        btables.set(B7, b7);
        btables.set(B8, b8);
        btables.set(B9, b9);
        btables.set(B10, b10);
        btables.set(B11, b11);
        btables.set(B13, b13);
        btables.set(B17, b17);
        btables.set(B20, b20);

    }

    private void stepC(InformationSet info) {
        InformationSet ctables = info.subSet(C);
        TsData c1;
        if (toolkit.getContext().isPseudoAdditive()) {
            TsData b7 = info.subSet(B).get(B7, TsData.class); // trend
            TsData b13 = info.subSet(B).get(B13, TsData.class); // irregular
            c1 = refSeries.plus(b7.times(b13.div(refCorrection).minus(b13)));
        } else {
            c1 = toolkit.getContext().op(refSeries, refCorrection);
        }
        TsData c2 = toolkit.getTrendCycleComputer().doInitialFiltering(
                X11Step.B, c1, info);

        // c4
        TsData c4 = toolkit.getContext().op(c1, c2);

        // c5
        TsData c5a = toolkit.getSeasonalComputer().doInitialFiltering(
                X11Step.C, c4, info);
        TsData c5 = toolkit.getSeasonalNormalizer().normalize(c5a,
                c1.getDomain());
        TsData c6;
        if (toolkit.getContext().isPseudoAdditive()) {
            c6 = toolkit.getContext().pseudoOp(c1, c2, c5);
        } else {
            c6 = toolkit.getContext().op(c1, c5);
        }
        TsData c7 = toolkit.getTrendCycleComputer().doFinalFiltering(X11Step.C,
                c6, info);
        if (toolkit.getContext().isMultiplicative() || toolkit.getContext().isPseudoAdditive()) {
            toolkit.getUtilities().checkPositivity(c7);
        }
        TsData c9 = toolkit.getContext().op(c1, c7);
        TsData c10a = toolkit.getSeasonalComputer().doFinalFiltering(X11Step.C,
                c9, info);
        TsData c10 = toolkit.getSeasonalNormalizer().normalize(c10a, null);
        TsData c11;
        if (toolkit.getContext().isPseudoAdditive()) {
            c11 = refSeries.minus(c7.times(c10.minus(1)));
        } else {
            c11 = toolkit.getContext().op(refSeries, c10);
        }
        TsData c13 = toolkit.getContext().op(c11, c7);

        TsData next = c13;
        /*
         * X11Series B16 = null; if (m_params.isTradingDayRegression()) {
         * CalendarEffect ce = new CalendarEffect(m_params.getFrequency()); if
         * (m_params.hasAprioriTD())
         * ce.setAprioriCoefficients(m_params.getAprioriTD());
         *
         * ce.setMultiplicativeModel(m_params.isMultiplicative()); X11Series
         * B16bis = ce.computeTradingDayEffect(B13); m_res.m_tdreg =
         * ce.getTDRegression();
         *
         * m_res.add("B14", ce.getTable("B14"),
         * "Irregular component values excluded from Trading Day Regression");
         *
         * m_res.add("B16bis", B16bis,
         * "Irregular component corrected for trading day effects"); next =
         * B16bis;
         *
         * B16 = ce.getTable("B16"); m_res.add("B16", B16,
         * "Adjustment Coefficients for trading day effects from the regression"
         * ); }
         */

        IExtremeValuesCorrector ecorr = toolkit.getExtremeValuesCorrector();
        ecorr.analyse(next);
        TsData c17 = ecorr.getObservationWeights();
        TsData c20 = ecorr.getCorrectionFactors();
        refCorrection=c20;
        
        if (toolkit.getContext().isLogAdditive()){
            c1.applyOnFinite(x->Math.exp(x));
            c2.applyOnFinite(x->Math.exp(x));
            c4.applyOnFinite(x->Math.exp(x));
            c5.applyOnFinite(x->Math.exp(x));
            c6.applyOnFinite(x->Math.exp(x));
            c7.applyOnFinite(x->Math.exp(x));
            c9.applyOnFinite(x->Math.exp(x));
            c10.applyOnFinite(x->Math.exp(x));
            c11.applyOnFinite(x->Math.exp(x));
            c13.applyOnFinite(x->Math.exp(x));
            c20=c20.exp();
        }

        ctables.set(C1, c1);
        ctables.set(C2, c2);
        ctables.set(C4, c4);
        ctables.set(C5, c5);
        ctables.set(C6, c6);
        ctables.set(C7, c7);
        ctables.set(C9, c9);
        ctables.set(C10, c10);
        ctables.set(C11, c11);
        ctables.set(C13, c13);
        ctables.set(C17, c17);
        ctables.set(C20, c20);
    }

    private void stepD(InformationSet info) {
        InformationSet dtables = info.subSet(D);
        TsDomain sdomain = toolkit.getContext().getEstimationDomain();
        TsData d1;
        if (toolkit.getContext().isPseudoAdditive()) {
            TsData c7 = info.subSet(C).get(C7, TsData.class); // trend
            TsData c13 = info.subSet(C).get(C13, TsData.class); // irregular
            d1 = refSeries.plus(c7.times(c13.div(refCorrection).minus(c13)));
        } else {
            d1 = toolkit.getContext().op(refSeries, refCorrection);
        }
        // d2
        TsData d2 = toolkit.getTrendCycleComputer().doInitialFiltering(
                X11Step.D, d1, info);

        // d4
        TsData d4 = toolkit.getContext().op(d1, d2);

        // d5
        TsData d5a = toolkit.getSeasonalComputer().doInitialFiltering(
                X11Step.D, d4, info);
//        System.out.println("d5a_alt");
//        System.out.println(d5a);
        TsData d5 = toolkit.getSeasonalNormalizer().normalize(d5a,
                d1.getDomain());
        TsData d6;
        if (toolkit.getContext().isPseudoAdditive()) {
            d6 = toolkit.getContext().pseudoOp(d1, d2, d5);
        } else {
            d6 = toolkit.getContext().op(d1, d5);
        }

        TsData d7 = toolkit.getTrendCycleComputer().doFinalFiltering(X11Step.D,
                d6, info);
        if (toolkit.getContext().isMultiplicative() || toolkit.getContext().isPseudoAdditive()) {
            toolkit.getUtilities().checkPositivity(d7);
        }
        TsData d8a = toolkit.getContext().op(d1, d7);
        TsData d8 = toolkit.getContext().invOp(d8a, refCorrection);
// probably a mistake in case of pseudoadditive decomposition. To See with Brian

//        TsData d8 = toolkit.getContext().op(refSeries, d7);
        TsData d9, d10;
        if (ecorr instanceof PeriodSpecificExtremeValuesCorrector) {
            d9 = ecorr.computeCorrections(d8.drop(0, toolkit.getContext().getForecastHorizon()));
            d9.extend(0, toolkit.getContext().getForecastHorizon());

            TsData d9g = ecorr.applyCorrections(d8, d9);
            TsData d10a = toolkit.getSeasonalComputer().doFinalFiltering(X11Step.D,
                    d9g, info);
            d10 = toolkit.getSeasonalNormalizer().normalize(d10a, null);
        } else {
            TsData d9bis = toolkit.getContext().op(d1, d7);
            d9 = toolkit.getUtilities().differences(d9bis, d8); //
            TsData d10bis = toolkit.getSeasonalComputer().doFinalFiltering(
                    X11Step.D, d9bis, info);
            d10 = toolkit.getSeasonalNormalizer().normalize(d10bis, null);
        }
        TsData d11bis;
        if (toolkit.getContext().isPseudoAdditive()) {
            d11bis = d1.minus(d7.times(d10.minus(1)));
        } else {
            d11bis = toolkit.getContext().op(d1, d10);
        }
        boolean[] valid = toolkit.getContext().getValidDecomposition();
        if (valid != null) {
            TsData tmp = d11bis.fittoDomain(sdomain);
            for (int i = 0; i < valid.length; ++i) {
                if (valid[i] && tmp.get(i) <= 0) {
                    valid[i] = false;
                }
            }
        }

        TsData d12 = toolkit.getTrendCycleComputer().doFinalFiltering(
                X11Step.D, d11bis, info);
        ICRatioComputer.writeICR(toolkit.getContext(), d11bis, info);
        if (toolkit.getContext().isMultiplicative() || toolkit.getContext().isPseudoAdditive()) {
            toolkit.getUtilities().checkPositivity(d12);
        }

        TsData d10b, d11;
        if (toolkit.getContext().isPseudoAdditive()) {
            d10b = d12.times(d10.minus(1));
            d11 = refSeries.minus(d10b);
        } else {
            d10b = null;
            d11 = toolkit.getContext().op(refSeries, d10);
        }

        TsData d13 = toolkit.getContext().op(d11, d12);

        if (toolkit.getContext().getMode() == DecompositionMode.LogAdditive) {
            // we transform all the current series
            d1.applyOnFinite(x -> Math.exp(x));
            d2.applyOnFinite(x -> Math.exp(x));
            d4.applyOnFinite(x -> Math.exp(x));
            d5.applyOnFinite(x -> Math.exp(x));
            d6.applyOnFinite(x -> Math.exp(x));
            d7.applyOnFinite(x -> Math.exp(x));
            d8.applyOnFinite(x -> Math.exp(x));
            d9.applyOnFinite(x -> Math.exp(x));
            d10.applyOnFinite(x -> Math.exp(x));
            d11.applyOnFinite(x -> Math.exp(x));
            d12.applyOnFinite(x -> Math.exp(x));
            d13.applyOnFinite(x -> Math.exp(x));
            TsData b1 = info.subSet(B).get(B1, TsData.class); // (m_params.isTradingDayRegression()
            toolkit.getContext().setMode(DecompositionMode.Multiplicative);
            if (uscbLike) {
                TsData c13 = info.subSet(C).get(C13, TsData.class);
                d12 = toolkit.getUtilities().correctTrendBias(d12, d10, c13, toolkit.getBiasCorrection());
            } else {
                d12 = toolkit.getUtilities().correctTrendBias(d12, d10, d13);
            }
            toolkit.getUtilities().checkPositivity(d12);
            d11 = toolkit.getContext().op(b1, d10);
            d13 = toolkit.getContext().op(d11, d12); 
        }

        dtables.set(D1, d1.fittoDomain(sdomain));
        dtables.set(D2, d2.fittoDomain(sdomain));
        dtables.set(D4, d4.fittoDomain(sdomain));
        dtables.set(D5, d5.fittoDomain(sdomain));
        dtables.set(D6, d6.fittoDomain(sdomain));
        dtables.set(D7, d7.fittoDomain(sdomain));
        dtables.set(D8, d8.fittoDomain(sdomain));
        dtables.set(D9, d9.fittoDomain(sdomain));

        if (d10b != null) {
            dtables.set(D10b, d10b.fittoDomain(sdomain));
        }
        dtables.set(D10L, d10.fittoDomain(sdomain));
        dtables.set(D11L, d11.fittoDomain(sdomain));
        dtables.set(D12L, d12.fittoDomain(sdomain));
        dtables.set(D13L, d13.fittoDomain(sdomain));

        // add pre-adjustment
        InformationSet atables = info.subSet(A);
        TsData a1 = atables.get(A1, TsData.class);
        TsData a8t = atables.get(A8t, TsData.class);
        TsData a8i = atables.get(A8i, TsData.class);
        TsData a8s = atables.get(A8s, TsData.class);

        // add ps to d10
        TsData d10c = toolkit.getContext().invOp(d10, a8s);
        dtables.set(D10, d10c.fittoDomain(sdomain));//

        // add pt to trend
        TsData d12c = toolkit.getContext().invOp(d12, a8t);

        // add pi to irregular
        TsData d13c = toolkit.getContext().invOp(d13, a8i);

        // add pt, pi to d11
        TsData d11c = toolkit.getContext().invOp(d11, a8t);
        d11c = toolkit.getContext().invOp(d11c, a8i);
        //   d11c = toolkit.getContext().invOp(d11c, a8s);
        TsData a9sa = atables.get(A9sa, TsData.class);

        d11c = toolkit.getContext().invOp(d11c, a9sa);

        //  TsData d16 = toolkit.getContext().op(a1, d11c);
        TsData d16;
        if (toolkit.getContext().isPseudoAdditive()) {
            d16 = a1.div(d12).minus(d13).plus(1);
        } else {
            d16 = toolkit.getContext().op(a1, d11c);
        }

        dtables.set(D11, d11c.fittoDomain(sdomain));
        dtables.set(D12, d12c.fittoDomain(sdomain));
        dtables.set(D13, d13c.fittoDomain(sdomain));
        dtables.set(D16, d16);
        dtables.set(D18, toolkit.getContext().op(d16, d10c));

        int nf = toolkit.getContext().getForecastHorizon();
        if (nf > 0) {
            TsData a1a = atables.get(A1a, TsData.class);
            TsData d16a;
            if (toolkit.getContext().isPseudoAdditive()) {
                d16a = a1a.div(d12).minus(d13).plus(1);
            } else {
                d16a = toolkit.getContext().op(a1a, d11c);
            }
            TsDomain fdomain = new TsDomain(sdomain.getEnd(), nf);
            dtables.set(D10a, d10c.fittoDomain(fdomain));
            dtables.set(D10aL, d10.fittoDomain(fdomain));
            dtables.set(D11a, d11c.fittoDomain(fdomain));
            dtables.set(D11aL, d11.fittoDomain(fdomain));
            dtables.set(D12a, d12c.fittoDomain(fdomain));
            dtables.set(D12aL, d12.fittoDomain(fdomain));
            dtables.set(D16a, d16a);
        } else {
            int freq = toolkit.getContext().getFrequency();
            TsDomain fdomain = new TsDomain(sdomain.getEnd(), freq);
            TsData d10a = new TsData(fdomain);
            for (int i = 0, k = sdomain.getLength() - freq; i < freq; ++i, ++k) {
                d10a.set(i, (d10.get(k) * 3 - d10.get(k - freq)) / 2);
            }
            dtables.set(D10a, d10a);
            dtables.set(D10aL, d10a);
            // TsData a8s = atables.get(A8s, TsData.class);
            TsData a6 = atables.get(A6, TsData.class);
            TsData a7 = atables.get(A7, TsData.class);
            TsData d16a = toolkit.getContext().invOp(d10a, a6);
            d16a = toolkit.getContext().invOp(d16a, a7);
            d16a = toolkit.getContext().invOp(d16a, a8s);
            dtables.set(D16a, d16a);
        }

        int nb = toolkit.getContext().getBackcastHorizon();
        //backcast is only calculated if there is a backcast horizon
        if (nb > 0) {
            TsDomain bdomain = new TsDomain(sdomain.getStart().minus(nb), nb);
            TsData a1b = atables.get(A1b, TsData.class);
            TsData d16b = toolkit.getContext().op(a1b, d11c);
            dtables.set(D16b, d16b);
            dtables.set(D10b, d10c.fittoDomain(bdomain));
        }
    }

    void stepE(InformationSet info) {
        InformationSet atables = info.subSet(A);
        TsData a1 = atables.get(A1, TsData.class);
//        TsData a6 = atables.get(A8, TsData.class);
//        TsData a7 = atables.get(A8, TsData.class);
        TsData a8i = atables.get(A8i, TsData.class);
        InformationSet ctables = info.subSet(C);
//        TsData c16 = ctables.get(C16, TsData.class);
        TsData c17 = ctables.get(C17, TsData.class);
        InformationSet dtables = info.subSet(D);
        TsData d16 = dtables.get(D16, TsData.class);
        TsData d11 = dtables.get(D11, TsData.class);
        TsData d12 = dtables.get(D12, TsData.class);
        TsData d13l = dtables.get(D13L, TsData.class);
        TsData d13 = dtables.get(D13, TsData.class);

        // remove pre-specified outliers
        TsData a1c = toolkit.getContext().op(a1, a8i);
        TsData d11c = toolkit.getContext().op(d11, a8i);

        TsData tmp = toolkit.getContext().op(a1, d13);
//        tmp = toolkit.getContext().invOp(tmp, c16);
        TsData e1 = toolkit.getUtilities().correctSeries(a1c, c17, tmp);
        TsData e2 = toolkit.getUtilities().correctSeries(d11c, c17, d12);
        TsData e3 = toolkit.getUtilities().correctSeries(d13l, c17,
                toolkit.getContext().getMean());
        TsData e11 = toolkit.getUtilities().correctSeries(d11c, c17,
                TsData.add(d12, TsData.subtract(a1c, e1)));

        InformationSet etables = info.subSet(E);
        etables.set(E1, e1);
        etables.set(E2, e2);
        etables.set(E3, e3);
        etables.set(E11, e11);
    }

    private void stepF(InformationSet info) {
    }
    
}

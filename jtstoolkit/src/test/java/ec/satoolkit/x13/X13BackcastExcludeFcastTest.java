/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x13;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x11.CalendarSigma;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.X11Specification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.algorithm.SequentialProcessing;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.x13.ArimaSpec;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.modelling.arima.x13.TradingDaysSpec;
import ec.tstoolkit.modelling.arima.x13.TransformSpec;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.NameManager;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import utilities.CompareTsData;

/**
 *
 * @author Christiane Hofer
 */
public class X13BackcastExcludeFcastTest {

    @Test
    public void TradingDays_userdefined_td_backcast() throws IOException {
//Same Results as x13 ARIMA Seats 1.1 Build 39 
        context = makeContext();
        X13Specification x13spec = makeX13Spec(false);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, context);
        CompositeResults comprest = processing.process(tsdserExam10);

        double d10b[] = {-6.091708219, -11.11560693, -0.552948537, -0.222080593, -1.53958497, -6.132280846, -2.013534191, -5.376317255, -3.798530553, 4.828793079, 7.837287384, 24.29283896};
        TsData tsd10b = new TsData(TsFrequency.Monthly, 1985, 0, d10b, false);
        Assert.assertTrue("D10b is wrong", CompareTsData.compareTS(tsd10b, comprest.getData("d-tables.d10b", TsData.class), 0.0000001));

        double d10[] = {-6.296806158, -11.07278326, -0.165566974, -0.479319824, -1.627075214, -6.134859523, -2.178123095, -5.333872073, -3.648737825, 4.605628631, 7.819756556, 24.72825346, -6.445770272, -11.14235182, 0.234023975, -0.779371921, -1.710583416, -6.244391766, -2.385493493, -5.277144221, -3.544092423, 4.499053068, 7.835869942, 25.33972915, -6.575461396, -11.21907984, 0.4233173, -0.989095008, -1.855749207, -6.411633226, -2.475137707, -5.423867236, -3.457238023, 4.513911784, 7.902730426, 26.14139685, -6.840474247, -11.30450227, 0.484502007, -1.051415644, -2.082830103, -6.582780328, -2.426675115, -5.731517716, -3.286603708, 4.513872477, 8.252193563, 26.60247235, -7.263010138, -11.35556051, 0.704339961, -1.134553054, -2.347410281, -6.627486382, -2.429241792, -6.117284144, -2.972884187, 4.371084548, 8.601207401, 27.07194715, -7.675192995, -11.4724768, 1.206755972, -1.357050505, -2.710780062, -6.635734951, -2.485512778, -6.356884201, -2.690479806, 4.222704807, 9.056094615, 27.29612902, -8.052171053, -11.58940447, 1.909778483, -1.802261065, -3.004798883, -6.585298275, -2.624745479, -6.443789778, -2.357572533, 4.043208126, 9.012696984, 27.84589473, -8.299137261, -11.72681532, 2.306741162, -2.040750608, -3.223005479, -6.623214808, -2.65457905, -6.482322044, -2.180408902, 3.99144953, 8.79969896, 28.46287415, -8.502865836, -11.88598347, 2.538202884, -2.16423359, -3.323602251, -6.672957203, -2.631328133, -6.628881146, -2.043248865, 4.014831012, 8.29047776, 29.36289886, -8.623262338, -12.02129091, 2.550755181, -2.108985622, -3.517925749, -6.718782875, -2.524339155, -6.935610315, -1.949097641, 4.135565245, 8.007613307, 30.06778719};
        TsData tsd10 = new TsData(TsFrequency.Monthly, 1986, 0, d10, false);
        Assert.assertTrue("D10 is wrong", CompareTsData.compareTS(tsd10, comprest.getData("d-tables.d10", TsData.class), 0.0000001));
    }

    @Test
    public void TradingDays_userdefined_td_backcast_excludefcast() throws IOException {
//Cant't compare the results with X13 because there the combination of excludefcast=yes and backcast is not possible
        context = makeContext();
        X13Specification x13spec = makeX13Spec(true);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, context);
        CompositeResults comprest = processing.process(tsdserExam10);

        double d10b[] = {-6.138356459, -11.1614952, -0.598338469, -0.267361084, -1.585279858, -6.168233405, -1.506855848, -5.424991418, -3.847794948, 4.780045024, 7.790288333, 24.24652798};
        TsData tsd10b = new TsData(TsFrequency.Monthly, 1985, 0, d10b, false);
        Assert.assertTrue("D10b is wrong", CompareTsData.compareTS(tsd10b, comprest.getData("d-tables.d10b", TsData.class), 0.0000001));

        double d10[] = {-6.341574844, -11.1153506, -0.208259143, -0.522684656, -1.671561826, -6.161402305, -1.711975539, -5.380735771, -3.695274199, 4.560293518, 7.776602558, 24.68515022, -6.487325387, -11.18198513, 0.194648985, -0.818601809, -1.750166995, -6.257979623, -1.970093824, -5.323361467, -3.611731983, 4.455321239, 7.785500466, 25.2905954, -6.513978163, -11.26197577, 0.380174466, -1.036944823, -1.908461417, -6.438573854, -2.119318499, -5.480954521, -3.565492928, 4.580300617, 7.835198238, 26.07522815, -6.671236029, -11.36731295, 0.429168435, -1.111613778, -2.16360545, -6.624047581, -2.120362625, -5.803306656, -3.435942191, 4.702282527, 8.177542702, 26.52712816, -6.991744735, -11.44219328, 0.633151318, -1.21163111, -2.463385946, -6.700297796, -2.181580601, -6.225226338, -3.159340496, 4.657551951, 8.499620705, 27.26438741, -7.43824069, -11.60096081, 1.102418391, -1.461481215, -2.865153164, -6.73102934, -2.261712851, -6.490113022, -2.902543019, 4.484030714, 8.935404268, 27.76424185, -7.818427684, -11.73616426, 1.788046745, -1.925531061, -3.181902603, -6.699061111, -2.541655608, -6.595178033, -2.589920573, 4.309931745, 8.870745649, 28.5899068, -8.047355304, -11.89304471, 2.167837372, -2.182523055, -3.426334358, -6.74558554, -2.693462652, -6.630726704, -2.414142842, 4.301380994, 8.666892007, 29.21727686, -8.201141573, -12.05368208, 2.402911246, -2.302574952, -3.532767132, -6.800118383, -2.779804979, -6.778549155, -2.285839342, 4.37572275, 8.141446074, 30.18144284, -8.276429782, -12.20495489, 2.404990627, -2.257316831, -3.746776945, -6.85748565, -2.664492976, -7.077054127, -2.191945023, 4.564173024, 7.857667725, 30.7428973};
        TsData tsd10 = new TsData(TsFrequency.Monthly, 1986, 0, d10, false);
        Assert.assertTrue("D10 is wrong", CompareTsData.compareTS(tsd10, comprest.getData("d-tables.d10", TsData.class), 0.0000001));

    }

    X13Specification makeX13Spec(boolean excludefcast) {
        //fix ArimaSpec
        ArimaSpec arimaSpec = new ArimaSpec();
        arimaSpec.setP(0);
        arimaSpec.setD(1);
        arimaSpec.setQ(1);
        arimaSpec.setBP(0);
        arimaSpec.setBD(1);
        arimaSpec.setBQ(1);
        arimaSpec.setMean(false);
        Parameter[] paraTheta = Parameter.create(1);
        paraTheta[0].setType(ParameterType.Fixed);
        paraTheta[0].setValue(-0.6626);
        arimaSpec.setTheta(paraTheta);
        Parameter[] paraBTheta = Parameter.create(1);
        paraBTheta[0].setType(ParameterType.Fixed);
        paraBTheta[0].setValue(-0.6458);
        arimaSpec.setBTheta(paraBTheta);

        RegArimaSpecification regArimaSpecification = new RegArimaSpecification();
        regArimaSpecification.setArima(arimaSpec);
        TransformSpec tr = new TransformSpec();
        tr.setFunction(DefaultTransformationType.None);
        regArimaSpecification.setTransform(tr);
        TradingDaysSpec tdspec = new TradingDaysSpec();
        String[] variablesNames = new String[1];
        variablesNames[0] = "Vars-1.x_c";
        tdspec.setUserVariables(variablesNames);
        regArimaSpecification.getRegression().setTradingDays(tdspec);

        X11Specification x11spec = new X11Specification();
        x11spec.setMode(DecompositionMode.Additive);
        x11spec.setSeasonalFilter(SeasonalFilterOption.S3X3);
        SeasonalFilterOption sf[] = new SeasonalFilterOption[12];
        for (int i = 0; i < 10; i++) {
            sf[i] = SeasonalFilterOption.S3X9;
        }
        sf[10] = SeasonalFilterOption.S3X5;
        sf[11] = SeasonalFilterOption.S3X5;

        x11spec.setSeasonalFilters(sf);
        x11spec.setHendersonFilterLength(23);
        x11spec.setLowerSigma(2.0);
        x11spec.setUpperSigma(3.0);
        x11spec.setForecastHorizon(-1);
        x11spec.setBackcastHorizon(-1);
        x11spec.setCalendarSigma(CalendarSigma.All);
        x11spec.setExcludefcst(excludefcast);
        X13Specification x13Spec = new X13Specification(regArimaSpecification, x11spec);
        return x13Spec;
    }

    private static ProcessingContext context;
    private static final double[] serExam10 = {68.9, 62.7, 73.5, 73.6, 73.7, 67.4, 73.5, 70.9, 70.2, 80.6, 84.1, 96.9, 69.6, 64.0, 73.0, 76.8, 75.5, 70.3, 74.4, 69.0, 73.9, 81.5, 81.4, 102.6, 70.9, 66.0, 74.1, 80.3, 74.2, 72.4, 79.3, 72.3, 75.1, 85.6, 86.7, 107.0, 71.9, 70.3, 85.0, 77.1, 78.8, 75.8, 78.1, 75.8, 79.4, 83.9, 91.0, 110.9, 74.9, 72.0, 85.4, 80.4, 81.5, 80.1, 79.4, 80.9, 82.2, 88.3, 95.8, 114.3, 80.6, 76.8, 92.3, 87.4, 90.3, 84.3, 90.0, 90.6, 90.2, 99.9, 106.1, 122.0, 91.7, 85.0, 100.4, 96.2, 95.8, 92.3, 97.6, 93.2, 92.9, 107.1, 110.1, 125.5, 94.2, 89.3, 96.9, 99.7, 94.8, 92.2, 98.8, 89.8, 97.8, 107.7, 107.7, 134.5, 88.8, 85.5, 102.6, 101.6, 92.5, 93.6, 99.5, 91.8, 98.8, 102.9, 109.9, 130.5, 89.0, 86.9, 106.9, 93.0, 95.2, 94.0, 93.3, 93.4, 98.7, 100.5, 105.1, 129.8};
    private static final TsData tsdserExam10 = new TsData(TsFrequency.Monthly, 1986, 0, serExam10, false);

    private static final ProcessingContext makeContext() {
        double[] cal = {-0.413043478, -0.02173913, 0.543478261, -0.282608696, -0.413043478, 0.695652174, -0.413043478, -0.413043478, 0.695652174, -0.413043478, -0.282608696, 0.543478261, -0.413043478, -0.02173913, 0.543478261, -0.282608696, -0.413043478, 0.695652174, -0.413043478, 0.586956522, -0.304347826, -0.413043478, 0.717391304, -0.456521739, -0.413043478, -0.02173913, 0.543478261, -0.282608696, 0.586956522, -0.304347826, -0.413043478, 0.586956522, -0.304347826, -0.413043478, 0.717391304, -0.456521739, 0.586956522, -0.02173913, -0.456521739, -0.282608696, 0.586956522, -0.304347826, 0.586956522, -0.413043478, -0.304347826, 0.586956522, -0.282608696, -0.456521739, 0.586956522, -0.02173913, -0.456521739, 0.717391304, -0.413043478, -0.304347826, 0.586956522, -0.413043478, -0.304347826, 0.586956522, -0.282608696, 0.543478261, -0.413043478, -0.02173913, -0.456521739, 0.717391304, -0.413043478, -0.304347826, 0.586956522, -0.413043478, 0.695652174, -0.413043478, -0.282608696, 0.543478261, -0.413043478, -0.02173913, 0.543478261, -0.282608696, -0.413043478, 0.695652174, -0.413043478, -0.413043478, 0.695652174, -0.413043478, -0.282608696, 0.543478261, -0.413043478, -0.02173913, 0.543478261, -0.282608696, 0.586956522, -0.304347826, -0.413043478, 0.586956522, -0.304347826, -0.413043478, 0.717391304, -0.456521739, 0.586956522, -0.02173913, -0.456521739, -0.282608696, 0.586956522, -0.304347826, -0.413043478, 0.586956522, -0.304347826, 0.586956522, -0.282608696, -0.456521739, 0.586956522, -0.02173913, -0.456521739, -0.282608696, 0.586956522, -0.304347826, 0.586956522, -0.413043478, -0.304347826, 0.586956522, -0.282608696, -0.456521739, 0.586956522, -0.02173913, -0.456521739, 0.717391304, -0.413043478, -0.304347826, 0.586956522, -0.413043478, -0.304347826, 0.586956522, -0.282608696, 0.543478261, -0.413043478, -0.02173913, 0.543478261, -0.282608696, -0.413043478, 0.695652174, -0.413043478, -0.413043478, 0.695652174, -0.413043478, -0.282608696, 0.543478261, -0.413043478, -0.02173913, 0.543478261, -0.282608696, -0.413043478, 0.695652174, -0.413043478, 0.586956522, -0.304347826, -0.413043478, 0.717391304, -0.456521739, -0.413043478, -0.02173913, 0.543478261, -0.282608696, 0.586956522, -0.304347826, -0.413043478, 0.586956522, -0.304347826, -0.413043478, 0.717391304, -0.456521739, 0.586956522, -0.02173913, -0.456521739, -0.282608696, 0.586956522, -0.304347826, -0.413043478, 0.586956522, -0.304347826, 0.586956522, -0.282608696, -0.456521739, 0.586956522, -0.02173913, -0.456521739, 0.717391304, -0.413043478, -0.304347826, 0.586956522, -0.413043478, -0.304347826, 0.586956522, -0.282608696, 0.543478261, -0.413043478, -0.02173913, -0.456521739, 0.717391304, -0.413043478, -0.304347826, 0.586956522, -0.413043478, 0.695652174, -0.413043478, -0.282608696, 0.543478261, -0.413043478, -0.02173913, 0.543478261, -0.282608696, -0.413043478, 0.695652174, -0.413043478, -0.413043478, 0.695652174, -0.413043478, -0.282608696, 0.543478261, -0.413043478, -0.02173913, 0.543478261, -0.282608696, -0.413043478, 0.695652174, -0.413043478, 0.586956522, -0.304347826, -0.413043478, 0.717391304, -0.456521739, -0.413043478, 0.97826087, -0.456521739, -0.282608696, 0.586956522, -0.304347826, -0.413043478, 0.586956522, -0.304347826, 0.586956522, -0.282608696, -0.456521739, 0.586956522, -0.02173913, -0.456521739, -0.282608696, 0.586956522, -0.304347826, 0.586956522, -0.413043478, -0.304347826, 0.586956522, -0.282608696, -0.456521739, 0.586956522, -0.02173913, -0.456521739, 0.717391304, -0.413043478, -0.304347826, 0.586956522, -0.413043478, -0.304347826, 0.586956522, -0.282608696, 0.543478261, -0.413043478, -0.02173913, -0.456521739, 0.717391304, -0.413043478, -0.304347826, 0.586956522, -0.413043478, 0.695652174, -0.413043478, -0.282608696, 0.543478261, -0.413043478, -0.02173913, 0.543478261, -0.282608696, -0.413043478, 0.695652174, -0.413043478, 0.586956522, -0.304347826, -0.413043478, 0.717391304, -0.456521739, -0.413043478, -0.02173913, 0.543478261, -0.282608696, 0.586956522, -0.304347826, -0.413043478, 0.586956522, -0.304347826, -0.413043478, 0.717391304, -0.456521739, 0.586956522, -0.02173913, -0.456521739, -0.282608696, 0.586956522, -0.304347826, -0.413043478, 0.586956522, -0.304347826, 0.586956522, -0.282608696, -0.456521739, 0.586956522, -0.02173913, -0.456521739, -0.282608696, 0.586956522, -0.304347826, 0.586956522, -0.413043478, -0.304347826, 0.586956522, -0.282608696, -0.456521739, 0.586956522, -0.02173913, -0.456521739, 0.717391304, -0.413043478, -0.304347826, 0.586956522, -0.413043478, 0.695652174, -0.413043478, -0.282608696, 0.543478261, -0.413043478, -0.02173913, 0.543478261, -0.282608696, -0.413043478, 0.695652174, -0.413043478, -0.413043478, 0.695652174, -0.413043478, -0.282608696, 0.543478261, -0.413043478, -0.02173913, 0.543478261, -0.282608696, -0.413043478, 0.695652174, -0.413043478, 0.586956522, -0.304347826, -0.413043478, 0.717391304, -0.456521739, -0.413043478, -0.02173913, 0.543478261, -0.282608696, 0.586956522, -0.304347826, -0.413043478, 0.586956522, -0.304347826, -0.413043478, 0.717391304, -0.456521739, 0.586956522, -0.02173913, -0.456521739, -0.282608696, 0.586956522, -0.304347826, 0.586956522, -0.413043478, -0.304347826, 0.586956522, -0.282608696, -0.456521739, 0.586956522, -0.02173913, -0.456521739, 0.717391304, -0.413043478, -0.304347826, 0.586956522, -0.413043478, -0.304347826, 0.586956522, -0.282608696, 0.543478261, -0.413043478, -0.02173913, -0.456521739, 0.717391304, -0.413043478, -0.304347826, 0.586956522, -0.413043478, 0.695652174, -0.413043478, -0.282608696, 0.543478261, -0.413043478, -0.02173913, 0.543478261, -0.282608696, -0.413043478, 0.695652174, -0.413043478, -0.413043478, 0.695652174, -0.413043478, -0.282608696, 0.543478261, -0.413043478, -0.02173913, 0.543478261, -0.282608696, 0.586956522, -0.304347826, -0.413043478, 0.586956522, -0.304347826, -0.413043478, 0.717391304, -0.456521739, 0.586956522, -0.02173913, -0.456521739, -0.282608696, 0.586956522, -0.304347826, -0.413043478, 0.586956522, -0.304347826, 0.586956522, -0.282608696, -0.456521739, 0.586956522, -0.02173913, -0.456521739, -0.282608696, 0.586956522, -0.304347826, 0.586956522, -0.413043478, -0.304347826, 0.586956522, -0.282608696, -0.456521739, 0.586956522, -0.02173913, -0.456521739, 0.717391304, -0.413043478, -0.304347826, 0.586956522, -0.413043478, -0.304347826, 0.586956522, -0.282608696, 0.543478261, -0.413043478, -0.02173913, 0.543478261, -0.282608696, -0.413043478, 0.695652174, -0.413043478, -0.413043478, 0.695652174, -0.413043478, -0.282608696, 0.543478261, -0.413043478, -0.02173913, 0.543478261, -0.282608696, -0.413043478, 0.695652174, -0.413043478, 0.586956522, -0.304347826, -0.413043478, 0.717391304, -0.456521739, -0.413043478, -0.02173913, 0.543478261, -0.282608696, 0.586956522, -0.304347826, -0.413043478, 0.586956522, -0.304347826, -0.413043478, 0.717391304, -0.456521739, 0.586956522, -0.02173913, -0.456521739, -0.282608696, 0.586956522, -0.304347826, -0.413043478, 0.586956522, -0.304347826, 0.586956522, -0.282608696, -0.456521739, 0.586956522, -0.02173913, -0.456521739, 0.717391304, -0.413043478, -0.304347826, 0.586956522, -0.413043478, -0.304347826, 0.586956522, -0.282608696, 0.543478261, -0.413043478, -0.02173913, -0.456521739, 0.717391304, -0.413043478, -0.304347826, 0.586956522, -0.413043478, 0.695652174, -0.413043478, -0.282608696, 0.543478261, -0.413043478, -0.02173913, 0.543478261, -0.282608696, -0.413043478, 0.695652174, -0.413043478, -0.413043478, 0.695652174, -0.413043478, -0.282608696, 0.543478261};
        TsData tsdCal = new TsData(TsFrequency.Monthly, 1985, 0, cal, false);
        TsVariable tsCal = new TsVariable("aF", tsdCal);
        if (context == null) {
            context = new ProcessingContext();
            NameManager<TsVariables> activeMgr = context.getTsVariableManagers();
            TsVariables mgr = new TsVariables();
            mgr.set("x_c", tsCal); //ok
            activeMgr.set("Vars-1", mgr);//ok
            activeMgr.resetDirty();
            ProcessingContext.setActiveContext(context);
        }

        return context;
    }
}

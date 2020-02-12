/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x12;

import jdplus.regsarima.ami.ExactOutliersDetector;
import demetra.data.Data;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.RegArimaUtility;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaSpecification;
import jdplus.regsarima.internal.HannanRissanenInitializer;
import demetra.timeseries.TsPeriod;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import java.util.List;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Ignore;
import demetra.data.DoubleSeq;
import static demetra.x12.OutliersDetectionModule.EPS;
import jdplus.modelling.regression.AdditiveOutlierFactory;
import jdplus.modelling.regression.LevelShiftFactory;
import jdplus.modelling.regression.TransitoryChangeFactory;
import jdplus.regarima.outlier.ExactSingleOutlierDetector;
import jdplus.regarima.outlier.RobustStandardDeviationComputer;
import jdplus.regarima.outlier.SingleOutlierDetector;
import jdplus.sarima.estimation.SarimaMapping;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class OutliersDetectionModuleTest {

    public OutliersDetectionModuleTest() {
    }

    static SingleOutlierDetector<SarimaModel> defaultOutlierDetector(int period) {
        SingleOutlierDetector sod = new ExactSingleOutlierDetector(RobustStandardDeviationComputer.mad(false),
                null, X12Utility.mlComputer());
        sod.setOutlierFactories(AdditiveOutlierFactory.FACTORY,
                LevelShiftFactory.FACTORY_ZEROENDED,
                new TransitoryChangeFactory(EPS));
        return sod;
    }
    
    @Test
    public void testProd() {
        TsPeriod start = TsPeriod.monthly(1967, 1);
        SarimaSpecification spec =  SarimaSpecification.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec).setDefault().build();

        ExactOutliersDetector od = ExactOutliersDetector.builder()
                .singleOutlierDetector(defaultOutlierDetector(12))
                .criticalValue(3)
                .processor(RegArimaUtility.processor(SarimaMapping.of(spec), true, 1e-7))
                .build();
        RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder().y(DoubleSeq.copyOf(Data.PROD)).arima(sarima).build();
        od.prepare(regarima.getObservationsCount());
        od.process(regarima, SarimaMapping.of(spec));
        int[][] outliers = od.getOutliers();
        for (int i = 0; i < outliers.length; ++i) {
            int[] cur = outliers[i];
            System.out.println(od.getFactory(cur[1]).getCode() + '-' + start.plus(cur[0]).display());
        }
        assertTrue(outliers.length == 4);
    }

    @Test
    @Ignore
    public void testProdWn() {
        TsPeriod start = TsPeriod.monthly(1967, 1);
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.setBd(1);
        spec.setD(1);
        SarimaModel sarima = SarimaModel.builder(spec).setDefault().build();
        System.out.println("WN");
        HannanRissanenInitializer hr = HannanRissanenInitializer.builder().build();
        ExactOutliersDetector od = ExactOutliersDetector.builder()
                .singleOutlierDetector(defaultOutlierDetector(12))
                .criticalValue(3)
                .processor(RegArimaUtility.processor(SarimaMapping.of(spec), true, 1e-7))
                .build();
        RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder().y(DoubleSeq.copyOf(Data.PROD)).arima(sarima).build();
        od.prepare(regarima.getObservationsCount());
        od.process(regarima, SarimaMapping.of(spec));
        int[][] outliers = od.getOutliers();
        for (int i = 0; i < outliers.length; ++i) {
            int[] cur = outliers[i];
            System.out.println(od.getFactory(cur[1]).getCode() + '-' + start.plus(cur[0]).display());
        }
    }

    @Test
    public void testProdLegacy() {

        ec.tstoolkit.modelling.arima.x13.OutliersDetector od = new ec.tstoolkit.modelling.arima.x13.OutliersDetector();
        od.setDefault();
        od.setCriticalValue(3);
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, Data.PROD, true);
        ec.tstoolkit.modelling.arima.ModelDescription desc = new ec.tstoolkit.modelling.arima.ModelDescription(s, null);
        ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();

        desc.setAirline(true);
        context.description = desc;
        context.hasseas = true;
        od.process(context);
        List<IOutlierVariable> outliers = context.description.getOutliers();
        int n = outliers.size();
//        System.out.println("Legacy");
//        for (IOutlierVariable o : outliers) {
//            System.out.println(o.getName());
//        }
    }

    @Test
    @Ignore
    public void stressTestProd() {
        System.out.println("JD3");
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 200; ++i) {
        SarimaSpecification spec =  SarimaSpecification.airline(12);
            SarimaModel sarima = SarimaModel.builder(spec).setDefault().build();
            ExactOutliersDetector od = ExactOutliersDetector.builder()
                    .singleOutlierDetector(defaultOutlierDetector(12))
                    .criticalValue(3)
                    .processor(RegArimaUtility.processor(SarimaMapping.of(spec), true, 1e-7))
                    .build();
            RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder().y(DoubleSeq.copyOf(Data.PROD)).arima(sarima).build();
            od.prepare(regarima.getObservationsCount());
            od.process(regarima, SarimaMapping.of(spec));
            int[][] outliers = od.getOutliers();
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
    @Ignore
    public void stressTestProdLegacy() {

        System.out.println("Legacy");
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 200; ++i) {
            ec.tstoolkit.modelling.arima.x13.OutliersDetector od = new ec.tstoolkit.modelling.arima.x13.OutliersDetector();
            od.setDefault();
            od.setCriticalValue(3);
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, Data.PROD, true);
            ec.tstoolkit.modelling.arima.ModelDescription desc = new ec.tstoolkit.modelling.arima.ModelDescription(s, null);
            ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();

            desc.setAirline(true);
            context.description = desc;
            context.hasseas = true;
            od.process(context);
            List<IOutlierVariable> outliers = context.description.getOutliers();
            int n = outliers.size();
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
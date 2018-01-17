/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.r;

import demetra.data.Data;
import demetra.data.DataBlockStorage;
import demetra.data.DoubleSequence;
import demetra.maths.MatrixType;
import static demetra.r.TimeVaryingRegressionTest.FURNITURE;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.SsfData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsDataToolkit;
import static demetra.timeseries.simplets.TsDataToolkit.log;
import demetra.ucarima.ModelDecomposer;
import demetra.ucarima.SeasonalSelector;
import demetra.ucarima.TrendCycleSelector;
import demetra.ucarima.UcarimaModel;
import demetra.ucarima.ssf.SsfUcarima;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TradingDaysTestsTest {

    public TradingDaysTestsTest() {
    }

    @Test
    public void testTD() {
        TsData s = log(TsData.of(TsPeriod.monthly(1992, 1), DoubleSequence.ofInternal(Data.ABS_RETAIL)));
        TimeVaryingRegression.Results regarima = TimeVaryingRegression.regarima(s, "TD7", "Default", 1e-7);
        TsData rtd = regarima.getData("tdeffect", TsData.class);

        UcarimaModel ucm = ucmAirline(regarima.getArima());
        ucm = ucm.simplify();
        SsfUcarima ssf = SsfUcarima.of(ucm);
        SsfData data = new SsfData(s.values());
        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);
        TsData i1 = TsData.of(s.getStart(), ds.item(ssf.getComponentPosition(2)));
        
        data = new SsfData(TsDataToolkit.subtract(s, rtd).values());
        ds = DkToolkit.fastSmooth(ssf, data);
        TsData i2 = TsData.of(s.getStart(), ds.item(ssf.getComponentPosition(2)));
       
//        System.out.println(TradingDaysTests.ftest(i1, true, 0));
//        System.out.println(TradingDaysTests.ftest(i1, false, 0));
//        System.out.println(TradingDaysTests.ftest(i1, true, 8));
//        System.out.println(TradingDaysTests.ftest(i1, false, 8));
//        System.out.println(TradingDaysTests.ftest(i2, true, 0));
//        System.out.println(TradingDaysTests.ftest(i2, false, 0));
//        System.out.println(TradingDaysTests.ftest(i2, true, 8));
//        System.out.println(TradingDaysTests.ftest(i2, false, 8));
    }

    public static UcarimaModel ucmAirline(double th, double bth) {
        SarimaSpecification spec = new SarimaSpecification();
        spec.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec)
                .theta(1, th)
                .btheta(1, bth)
                .build();

        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(12);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(sarima);
        ucm = ucm.setVarianceMax(-1, false);
        return ucm;
    }

    public static UcarimaModel ucmAirline(SarimaModel sarima) {

        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(12);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(sarima);
        ucm = ucm.setVarianceMax(-1, false);
        return ucm;
    }

}
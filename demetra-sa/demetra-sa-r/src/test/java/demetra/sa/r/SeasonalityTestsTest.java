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
package demetra.sa.r;

import demetra.data.Data;
import demetra.stats.TestResult;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SeasonalityTestsTest {
    
    public SeasonalityTestsTest() {
    }

    @Test
    public void testFTest() {
        TestResult test = SeasonalityTests.fTest(Data.ABS_RETAIL.clone(), 12, true, 0);
//        System.out.println(test);
        assertTrue(test.getPvalue() <.01);
    }
    
    @Test
    public void testQsTest() {
        TestResult test = SeasonalityTests.qsTest(Data.ABS_RETAIL.clone(), 12, 0);
//        System.out.println(test);
        assertTrue(test.getPvalue() < .01);
    }
    
    @Test
    public void testPeriodicQsTest() {
        TestResult test = SeasonalityTests.periodicQsTest(Data.ABS_RETAIL.clone(), new double[]{17, 1});
//        System.out.println(test);
        assertTrue(test.getPvalue() >.01);
    }

}
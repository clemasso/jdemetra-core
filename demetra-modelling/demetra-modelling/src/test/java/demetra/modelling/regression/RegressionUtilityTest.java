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
package demetra.modelling.regression;

import demetra.maths.matrices.Matrix;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class RegressionUtilityTest {
    
    public RegressionUtilityTest() {
    }

    @Test
    public void testVarious() {
        List<ITsVariable<TsDomain>> vars=new ArrayList<>();
        ITsVariable ls1=new LevelShift(LocalDateTime.now(), true);
        ITsVariable ls2=new LevelShift(LocalDateTime.now(), false);
        ITsVariable c=new Constant();
        ITsVariable s=new PeriodicContrasts(7);
        
        TsDomain domain=TsDomain.of(TsPeriod.daily(2017, 7, 1), 90);
        Matrix M=RegressionUtility.data(domain, ls1, ls2, c, s);
//        System.out.println(M);
    }
}
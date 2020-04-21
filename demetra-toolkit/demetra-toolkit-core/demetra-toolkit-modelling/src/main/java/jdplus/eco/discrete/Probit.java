/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.eco.discrete;

import demetra.stats.ProbabilityType;
import jdplus.dstats.Normal;

/**
 *
 * @author Jean Palate
 */
public class Probit implements ICumulativeDistributionFunction {

    @Override
    public double f(double x) {
        return N.getProbability(x, ProbabilityType.Lower);
    }

    @Override
    public double df(double x) {
        return Math.exp(-.5 * x * x) / SQRT2PI;
    }

    @Override
    public double d2f(double x) {
        return -x / SQRT2PI * Math.exp(-.5 * x * x);
    }
    
    private static final Normal N = new Normal();
    private static final  double SQRT2PI = 2.5066282746310005;
}

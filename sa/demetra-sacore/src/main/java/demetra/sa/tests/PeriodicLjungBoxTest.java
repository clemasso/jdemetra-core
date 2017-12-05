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
package demetra.sa.tests;

import demetra.ar.IAutoRegressiveEstimation;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.dstats.Chi2;
import demetra.stats.AutoCovariances;
import demetra.stats.StatException;
import demetra.stats.tests.StatisticalTest;
import demetra.stats.tests.TestType;
import demetra.utilities.IntList;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class PeriodicLjungBoxTest implements IBuilder<StatisticalTest> {

    private int[] lags;
    private int nhp;
    private int sign;

    private final IntToDoubleFunction autoCorrelations;
    private final int n;

    public PeriodicLjungBoxTest(DoubleSequence sample, int nar) {
        if (nar > 0) {
            IAutoRegressiveEstimation burg = IAutoRegressiveEstimation.burg();
            burg.estimate(sample, nar);
            this.autoCorrelations = AutoCovariances.autoCorrelationFunction(burg.residuals(), 0);
        } else {
            this.autoCorrelations = AutoCovariances.autoCorrelationFunction(sample, 0);
        }
        this.n = sample.length();
    }

    /**
     *
     * @param nhp
     * @return
     */
    public PeriodicLjungBoxTest hyperParametersCount(int nhp) {
        this.nhp = nhp;
        return this;
    }

    public PeriodicLjungBoxTest useNegativeAutocorrelations() {
        sign = -1;
        return this;
    }

    public PeriodicLjungBoxTest usePositiveAutocorrelations() {
        sign = 1;
        return this;
    }

    public PeriodicLjungBoxTest useAllAutocorrelations() {
        sign = 0;
        return this;
    }

    public PeriodicLjungBoxTest lags(final int[] value) {
        lags = value;
        return this;
    }

    public PeriodicLjungBoxTest lags(final double period, final int nperiods) {
        lags = new int[nperiods];
        for (int i = 1; i <= nperiods; ++i) {
            double ip = period * i + .5;
            lags[i - 1] = (int) ip;
        }
        return this;
    }

    public int[] getLags() {
        return lags;
    }

    @Override
    public StatisticalTest build() {
        if (lags == null) {
            throw new StatException("Invalid lags in LjungBox test");
        }

        double res = 0.0;
        for (int i = 0; i < lags.length; i++) {
            double ai = autoCorrelations.applyAsDouble(lags[i]);
            if (sign == 0 || (sign == 1 && ai > 0) || (sign == -1 && ai < 0)) {
                res += ai * ai / (n - i);
            }
        }
        double val = res * n * (n + 2);
        Chi2 chi = new Chi2(lags.length > nhp ? lags.length : lags.length - nhp);
        return new StatisticalTest(chi, val, TestType.Upper, true);
    }
}

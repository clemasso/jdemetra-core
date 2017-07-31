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
package demetra.arima.estimation;

import demetra.arima.IArimaModel;
import demetra.arima.internal.MaLjungBoxFilter;
import demetra.data.DataBlock;
import demetra.data.DataWindow;
import demetra.design.Development;
import demetra.maths.polynomials.Polynomial;
import demetra.data.DoubleSequence;

/**
 * Computes the forecasts of an Arima model using the approach followed in X12/X13.
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FastArimaForecasts implements IArimaForecasts {

    private IArimaModel arima;
    private Polynomial ar, ma;
    private double mean;

    /**
     *
     * @param model
     */
    public FastArimaForecasts() {
    }

    @Override
    public boolean prepare(IArimaModel model, boolean bmean) {
        if (bmean) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        arima = model;
        ar = arima.getAR().asPolynomial();
        ma = arima.getMA().asPolynomial();
        mean = 0;
        return true;
    }

    @Override
    public boolean prepare(IArimaModel model, double mu) {
        arima = model;
        ar = arima.getAR().asPolynomial();
        ma = arima.getMA().asPolynomial();
        if (model.getStationaryAROrder() > 0) {
            Polynomial c = model.getStationaryAR().asPolynomial();
            mean = mu * c.evaluateAt(1);
        } else {
            mean = mu;
        }
        return true;
    }

    /**
     *
     * @param data
     * @param nf
     * @return
     */
    @Override
    public DoubleSequence forecasts(DoubleSequence data, int nf) {
        try {
            DataBlock res = residuals(data);
            // residuals i correspond to t=i+p-q
            double[] fcasts = new double[nf];
            int p = ar.getDegree();
            double[] y = new double[p];
            // copy the last obs, in reverse order.
            int last = data.length() - 1;
            for (int i = 0; i < p; ++i) {
                y[i] = data.get(last - i);
            }
            // copy the last residuals in reverse order
            int q = ma.getDegree();
            double[] e = new double[q];
            // copy the last obs, in reverse order.
            last = res.length() - 1;
            for (int i = 0; i < q; ++i) {
                e[i] = res.get(last - i);
            }
            for (int i = 0; i < nf; ++i) {
                double s = mean;
                for (int j = 0; j < p; ++j) {
                    s -= ar.get(j + 1) * y[j];
                }
                for (int j = i; j < q; ++j) {
                    s += ma.get(j + 1) * e[j - i];
                }
                for (int j = p - 1; j > 0; --j) {
                    y[j] = y[j - 1];
                }
                if (p > 0) {
                    y[0] = s;
                }
                fcasts[i] = s;
            }
            return DoubleSequence.ofInternal(fcasts);
        } catch (Exception err) {
            return null;
        }
    }

    // computes the residuals;
    private DataBlock residuals(DoubleSequence data) {
        DataBlock w = DataBlock.copyOf(data);
        try {
            // step 1. AR filter w, if necessary
            DataBlock z = w;

            int p = ar.getDegree();
            int q = ma.getDegree();
            if (p > 0) {
                z = DataBlock.make(w.length() - p);
                arima.getAR().apply(w, z);
            }
            if (mean != 0) {
                z.sub(mean);
            }
            // filter z (pure ma part)
            if (q > 0) {
                MaLjungBoxFilter malb = new MaLjungBoxFilter();
                int nwl = malb.prepare((IArimaModel) arima.stationaryTransformation().getStationaryModel(), z.length());
                DataBlock wl = DataBlock.make(nwl);
                malb.filter(z, wl);
                return wl;
            } else {
                return z;
            }
        } catch (Exception err) {
            return w;
        }
    }
}

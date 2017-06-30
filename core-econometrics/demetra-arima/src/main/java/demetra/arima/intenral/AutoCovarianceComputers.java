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
package demetra.arima.intenral;

import demetra.arima.ArimaException;
import demetra.arima.AutoCovarianceFunction;
import demetra.data.DataBlock;
import demetra.linearsystem.ILinearSystemSolver;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.maths.matrices.Matrix;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.RationalFunction;

/**
 *
 * @author Jean Palate
 */
public class AutoCovarianceComputers {

    public static final AutoCovarianceFunction.Computer DEFAULT = defaultComputer(ILinearSystemSolver.robustSolver());
    public static final AutoCovarianceFunction.SymmetricComputer DEFAULT_SYMETRIC = defaultSymmetricComputer(ILinearSystemSolver.robustSolver());

    public static AutoCovarianceFunction.Computer defaultComputer(ILinearSystemSolver solver) {
        return (Polynomial ar, Polynomial ma, int rank) -> {
             int p = ar.length();
            int q = ma.length();
            int r0 = Math.max(p, q);
            if (rank < r0) {
                rank = r0;
            }
            int k0 = r0;
            double[] c = new double[rank + 1];
            RationalFunction rfe = RationalFunction.of(ma, ar);
            double[] cr = rfe.coefficients(q);

            Matrix M = Matrix.square(r0);
            DataBlock x = DataBlock.ofInternal(c, 0, r0);
            for (int i = 0; i < q; ++i) {
                double s = 0;
                for (int j = i; j < q; ++j) {
                    s += ma.get(j) * cr[j - i];
                }
                x.set(i, s);
            }

            for (int i = 0; i < r0; ++i) {
                for (int j = 0; j < p; ++j) {
                    double w = ar.get(j);
                    if (w != 0) {
                        M.add(i, i < j ? j - i : i - j, w);
                    }
                }
            }
            try {
            } catch (Exception err) {
                throw new ArimaException(ArimaException.NONSTATIONARY);
            }

            for (int r = r0; r <= rank; ++r) {
                double s = 0;
                for (int j = 1; j < p; ++j) {
                    s += ar.get(j) * c[r - j];
                }
                c[r] = -s;
            }
            return c;
        };
    }

    public static AutoCovarianceFunction.SymmetricComputer defaultSymmetricComputer(ILinearSystemSolver solver) {
        return (Polynomial ar, SymmetricFilter sma, int rank) -> {
            return null;
//        int p = ar.getDegree() + 1;
//        int q = sma == null ? 0 : sma.getDegree() + 1 ;
//        int r0 = Math.max(p, q);
//        if (rank < r0) {
//            rank = r0;
//        }
//        double[] c;
//        if (p == 1) {
//            // pure moving average...
//            c = sma.asPolynomial().toArray();
//        } else {
//                c = new double[rank + 1];
//                BackFilter g = dsym ? sma_.decompose(new BackFilter(ar)) : sma_.decompose2(new BackFilter(ar));
//                double[] tmp = new RationalFunction(g.getPolynomial(), ar).coefficients(rank + 1);
//
//                if (var_ != 1) {
//                    ac[0] = 2 * tmp[0] * var_;
//                    for (int i = 1; i < tmp.length; ++i) {
//                        ac[i] = tmp[i] * var_;
//                    }
//                } else {
//                    System.arraycopy(tmp, 0, ac, 0, tmp.length);
//                    ac[0] *= 2;
//                }
//
//            }
//            if (rank < ac.length) {
//                return;
//            }
//
//            int k0 = ac.length;
//            double[] tmp = new double[rank];
//            for (int u = 0; u < k0; ++u) {
//                tmp[u] = ac[u];
//            }
//            ac = tmp;
//
//            // after the initialization process
//            for (int r = k0; r < rank; ++r) {
//                double s = 0;
//                for (int x = 1; x < p; ++x) {
//                    s += ar.get(x) * ac[r - x];
//                }
//                ac[r] = -s;
//            }
//        }
        };
    }

}


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
package demetra.arima;

import demetra.arima.intenral.AutoCovarianceComputers;
import demetra.design.Development;
import demetra.design.Immutable;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.maths.matrices.Matrix;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.RationalFunction;
import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The auto-covariance function provides the auto-covariance of any stationary
 * Linear model for any lags. The auto-covariances are computed recursively. The
 * object stores previously computed values for faster processing. Example: in
 * the case of the Arima model (1-.5B)y=(1+.8B)e, var(e)=4, The auto-covariance
 * function is created as follows: AutoCovarianceFunction acf=new
 * AutoCovarianceFunction( new double[]{1, -.5}, new double[]{1, +.8}, 4); the
 * variance is retrieved by acf.get(0); the auto-covariance of rank 7 is
 * retrieved by acf.get(7);
 *
 * @author Jean Palate
 */
@Immutable
@Development(status = Development.Status.Alpha)
public class AutoCovarianceFunction {

    @FunctionalInterface
    public static interface Computer {

        /**
         * Computes the auto-covariances for the given arima model for lags [0, rank[
         *
         * @param ar
         * @param ma
         * @param rank
         * @return
         */
        double[] ac(Polynomial ar, Polynomial ma, int rank);
    }

    private static final AtomicReference<Computer> DEF_COMPUTER = new AtomicReference<>(AutoCovarianceComputers.DEFAULT);

    @FunctionalInterface
    public static interface SymmetricComputer {

        /**
         * Computes the auto-covariances for the given symmetric filter for lags [0, rank[
         *
         * @param ar
         * @param sma
         * @param rank
         * @return
         */
        double[] ac(Polynomial ar, SymmetricFilter sma, int rank);
    }

    private static final AtomicReference<SymmetricComputer> DEF_SYMCOMPUTER = new AtomicReference<>();

    public static void setDefautComputer(Computer computer) {
        DEF_COMPUTER.set(computer);
    }

    private static final int BLOCK = 36;
    private final Polynomial ar, ma;
    private final SymmetricFilter sma;
    private volatile double[] ac;
    private final double ivar;

    public AutoCovarianceFunction(final Polynomial ma, final Polynomial ar, final double var) {
        this.ma = ma;
        this.ar = ar;
        this.sma = null;
        this.ivar = var;
    }

    public AutoCovarianceFunction(final SymmetricFilter sma, final Polynomial ar) {
        this.sma = sma;
        this.ar = ar;
        this.ma = null;
        this.ivar = 1;
    }

    /**
     * Gets all the auto-covariances up to a given rank.
     *
     * @param n The number of requested auto-covariances
     * @return An array of n values, going from the variance up to the
     * auto-covariance of rank(n-1).
     */
    public double[] values(final int n) {
        prepare(n);
        double[] a = new double[n];
        int nmax = Math.min(n, ac.length);
        System.arraycopy(ac, 0, a, 0, nmax);
        return a;
    }

    public double get(final int k) {
        prepare(k + 1);
        if (k >= ac.length) {
            return 0;
        } else {
            return ac[k];
        }
    }

    /**
     * Computes the auto-covariances up to the given rank (included).
     *
     * @param rank The rank to be computed.
     */
    public void prepare(int rank) {
        if (rank == 0) {
            rank = BLOCK;
        } else {
            int r = rank % BLOCK;
            if (r != 0) {
                rank += BLOCK - r;
            }
        }
        double[] acov = ac;
        if (acov == null || acov.length <= rank) {
            synchronized (this) {
                acov = ac;
                if (acov == null || acov.length <= rank) {
                    ac = ac(acov, rank);
                }
            }
        }
    }

    // ac is only used in the synchronized block
    private double[] ac(double[] acov, int rank) {
        if (acov == null) {
            acov = DEF_COMPUTER.get().ac(ar, ma, rank);
            if (ivar != 1) {
                for (int i = 0; i <= acov.length; ++i) {
                    acov[i] *= ivar;
                }
            }
        }
        if (acov.length <= rank) {
            double[] tmp=new double[rank+1];
            System.arraycopy(acov, 0, tmp, 0, acov.length);
            int p=ar.length();
            for (int r = acov.length; r <= rank; ++r) {
                double s = 0;
                for (int j = 1; j < p; ++j) {
                    s += ar.get(j) * tmp[r - j];
                }
                tmp[r] = -s;
            }
            acov=tmp;
        }
        return acov;
    }
//
//    /**
//     * Creates the auto-covariance function for a model identified by its moving
//     * average polynomial, its auto-regressive polynomial and its innovation
//     * variance.
//     *
//     * @param ma The values of the moving average polynomial
//     * @param ar The values of the auto-regressive polynomial
//     * @param var The innovation variance.
//     */
//    public AutoCovarianceFunction(final Polynomial ma, final Polynomial ar, final double var) {
//        this.ma = ma;
//        this.ar = ar;
//        var_ = var;
//    }
//
//    /**
//     * Creates the auto-covariance function for a model identified by its moving
//     * average symmetric filter (= var*Q(B)*Q(F) = auto-covariance filter of the
//     * ma part of the model) and its auto-regressive polynomial.
//     *
//     * @param sma The symmetric moving average filter
//     * @param ar The stationary auto-regressive polynomial
//     */
//    public AutoCovarianceFunction(final SymmetricFilter sma, final BackFilter ar) {
//        sma_ = sma;
//        this.ar = ar.getPolynomial();
//        var_ = 1;
//        method_ = Method.SymmetricFilterDecomposition;
//    }
//
//    /**
//     * Gets the method in use.
//     *
//     * @return The current method (Default2 is the default).
//     */
//    public Method getMethod() {
//        return method_;
//    }
//
//    /**
//     * Sets the computation method
//     *
//     * @param method The new computation method.
//     */
//    public void setMethod(Method method) {
//        if (ma == null && (method == Method.Default || method == Method.Default2)) {
//            throw new ArimaException("Invalid acf method");
//        }
//        method_ = method;
//        ac = null;
//    }
//
//    /**
//     * Gets all the auto-covariances up to a given rank.
//     *
//     * @param n The number of requested auto-covariances
//     * @return An array of n values, going from the variance up to the
//     * auto-covariance of rank(n-1).
//     */
//    public double[] values(final int n) {
//        prepare(n);
//        double[] a = new double[n];
//        int nmax = Math.min(n, ac.length);
//        System.arraycopy(ac, 0, a, 0, nmax);
//        return a;
//    }
//
//    /**
//     * Gets a specific auto-covariance.
//     *
//     * @param k The rank of the auto-covariance (0 for variance).
//     * @return The auto-covariance of rank k.
//     */
//    public double get(final int k) {
//        prepare(k + 1);
//        if (k >= ac.length) {
//            return 0;
//        } else {
//            return ac[k];
//        }
//    }
//
//    /**
//     * Gets the last rank with an auto-covariance different from 0
//     *
//     * @return The rank of the last non-null auto-covariance. -1 if the
//     * auto-covariance function is unbounded.
//     */
//    public int getBound() {
//        if (!hasBound()) {
//            return -1;
//        }
//        return ma.getDegree() + 1;
//    }
//
//    /**
//     * Checks that the auto-covariance is bounded. 
//     * @return True if the auto-covariance function is bounded, which means that
//     * the auto-regressive polynomial is 1; false otherwise.
//     */
//    public boolean hasBound() {
//        return ar.getDegree() + 1 == 1;
//    }
//
//    /**
//     * Computes the auto-covariances up to the given rank (included).
//     * @param rank The rank to be computed.
//     */
//    public void prepare(int rank) {
//        if (rank == 0) {
//            rank = BLOCK;
//        } else {
//            int r = rank % BLOCK;
//            if (r != 0) {
//                rank += BLOCK - r;
//            }
//        }
//        if (ac != null && ac.length > rank) {
//            return;
//        }
//
//        switch (method_) {
//            case Default:
//                computeDefault(rank);
//                break;
//            case Default2:
//                computeDefault2(rank);
//                break;
//            case SymmetricFilterDecomposition:
//                computeSymmetric(rank, true);
//                break;
//            case SymmetricFilterDecomposition2:
//                computeSymmetric(rank, false);
//                break;
//        }
//    }
//
//
//    private void computeSymmetric(int rank, boolean dsym) {
//        int p = ar.getDegree() + 1;
//        int q = sma_ != null ? sma_.getDegree() + 1 : ma.getDegree() + 1;
//        int r0 = Math.max(p, q);
//        if (rank < r0) {
//            rank = r0;
//        }
//        if (p == 1) {
//            // pure moving average...
//            if (sma_ == null) {
//                sma_ = SymmetricFilter.convolution(new BackFilter(ma));
//            }
//            ac = sma_.getCoefficients();
//            new DataBlock(ac).mul(var_);
//        } else {
//            if (ac == null) {
//                ac = new double[rank + 1];
//                if (sma_ == null) {
//                    sma_ = SymmetricFilter.createFromFilter(new BackFilter(ma));
//                }
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
//    }
}

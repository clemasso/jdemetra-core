/*
 * Copyright 2016 National Bank ofFunction Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions ofFunction the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy ofFunction the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.maths.matrices;

import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.Matrix;
import demetra.data.DoubleSequence;
import demetra.data.Doubles;

/**
 *
 * @author Jean Palate
 */
public class MatrixComparator {

    public static double distance(final Matrix m, final ec.tstoolkit.maths.matrices.Matrix o) {
        final int nrows = m.getRowsCount();
        if (o.getRowsCount() != nrows) {
            return Double.MAX_VALUE;
        }
        DoubleSequence delta = DoubleSequence.onMapping(o.getColumnsCount() * o.getRowsCount(),
                i -> m.get(i % nrows, i / nrows) - o.get(i % nrows, i / nrows));
        return Doubles.normInf(delta);
    }

    public static double distance(final Matrix m, final Matrix o) {
        final int nrows = m.getRowsCount();
        if (o.getRowsCount() != nrows) {
            return Double.MAX_VALUE;
        }
        DoubleSequence delta = DoubleSequence.onMapping(o.getColumnsCount() * o.getRowsCount(),
                i -> m.get(i % nrows, i / nrows) - o.get(i % nrows, i / nrows));
        return Doubles.normInf(delta);
    }

    public static ec.tstoolkit.maths.matrices.Matrix toLegacy(Matrix M) {
        int nrows = M.getRowsCount(), ncols = M.getColumnsCount();
        ec.tstoolkit.maths.matrices.Matrix O = new ec.tstoolkit.maths.matrices.Matrix(nrows, ncols);
        for (int c = 0; c < ncols; ++c) {
            for (int r = 0; r < nrows; ++r) {
                O.set(r, c, M.get(r, c));
            }
        }
        return O;
    }

    public static Matrix fromLegacy(ec.tstoolkit.maths.matrices.Matrix M) {
        int nrows = M.getRowsCount(), ncols = M.getColumnsCount();
        Matrix N = Matrix.make(nrows, ncols);
        for (int c = 0; c < ncols; ++c) {
            for (int r = 0; r < nrows; ++r) {
                N.set(r, c, M.get(r, c));
            }
        }
        return N;
    }
}
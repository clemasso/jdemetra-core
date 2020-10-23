/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.linearsystem;

import java.util.Random;
import jdplus.data.DataBlock;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.decomposition.HouseholderWithPivoting;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class QRLinearSystemSolverTest {
    
    public QRLinearSystemSolverTest() {
    }

    @Test
    public void testRandom() {
        
        Random rnd=new Random(0);
        int n=10;
        Matrix M=Matrix.square(n);
        M.set((i, j)->rnd.nextDouble());
        DataBlock x=DataBlock.make(n);
        x.set(rnd::nextDouble);
        DataBlock y=x.deepClone();
        QRLinearSystemSolver solver = QRLinearSystemSolver.builder().decomposer(A->new HouseholderWithPivoting().decompose(A,0)).build();
        solver.solve(M, x);
        System.out.println(x);
        DataBlock z=DataBlock.make(n);
        z.product(M.rowsIterator(), x);
        System.out.println(z);
        System.out.println();
        System.out.println(y);
        System.out.println();
        System.out.println(M);
        assertTrue(z.distance(y)<1e-7);
    }
    
}
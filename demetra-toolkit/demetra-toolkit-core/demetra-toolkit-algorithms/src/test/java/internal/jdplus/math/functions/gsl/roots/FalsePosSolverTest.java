/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internal.jdplus.math.functions.gsl.roots;

import internal.jdplus.math.functions.gsl.roots.FalsePosSolver;
import org.junit.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FalsePosSolverTest {

    @Test
    public void test() {
        SolverTestUtils.testAll("False position", FalsePosSolver::new);
    }
}
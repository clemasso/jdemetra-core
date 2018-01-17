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
package demetra.r;

import demetra.data.DoubleSequence;
import demetra.maths.linearfilters.FiniteFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class LocalPolynomialFilters {
    public double[] filter(double[] data, int horizon, int degree, String kernel, String endpoints, double ic){
        // Creates the filters
        IntToDoubleFunction weights = weights(horizon, kernel);
        SymmetricFilter filter = demetra.maths.linearfilters.LocalPolynomialFilters.of(horizon, degree, weights);
        FiniteFilter[] afilters;
        if (endpoints.equals("DAF")){
            afilters=new FiniteFilter[horizon];
            for (int i=0; i<afilters.length; ++i){
                afilters[i]=demetra.maths.linearfilters.LocalPolynomialFilters.directAsymmetricFilter(horizon, i, degree, weights);
            }
        }else{
            int u=0;
            switch (endpoints){
                case "LC": u=0;break;
                case "QL": u=1;break;
                case "CQ": u=2;break;
            }
            afilters=new FiniteFilter[horizon];
            for (int i=0; i<afilters.length; ++i){
                afilters[i]=demetra.maths.linearfilters.LocalPolynomialFilters.asymmetricFilter(filter, i, u, new double[]{ic}, null);
            }
        }
        
        DoubleSequence rslt = demetra.maths.linearfilters.LocalPolynomialFilters.filter(DoubleSequence.ofInternal(data)
                , filter, afilters);
        return rslt.toArray();
    }
    
    IntToDoubleFunction weights(int horizon, String filter){
        switch (filter){
            case "Uniform": return demetra.maths.linearfilters.DiscreteKernels.uniform();
            case "Biweight": return demetra.maths.linearfilters.DiscreteKernels.biweight(horizon);
            case "Triweight": return demetra.maths.linearfilters.DiscreteKernels.triweight(horizon);
            case "Tricube": return demetra.maths.linearfilters.DiscreteKernels.tricube(horizon);
            case "Triangular": return demetra.maths.linearfilters.DiscreteKernels.triangular(horizon);
            case "Parabolic": return demetra.maths.linearfilters.DiscreteKernels.parabolic(horizon);
            case "Gaussian": return demetra.maths.linearfilters.DiscreteKernels.gaussian(4*horizon);
            default: return demetra.maths.linearfilters.DiscreteKernels.henderson(horizon);
        }
    }
    

    
}
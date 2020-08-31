/*
* Copyright 2019 National Bank of Belgium
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
package demetra.regarima;

import demetra.design.Development;
import demetra.design.LombokWorkaround;
import demetra.modelling.ChangeOfRegimeSpec;
import demetra.modelling.RegressionTestSpec;
import demetra.util.Validatable;

/**
 *
 * @author Jean Palate, Mats Maggi
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class EasterSpec implements Validatable<EasterSpec> {

    private static final EasterSpec DEFAULT = EasterSpec.builder().build();

    public final static int DEF_EASTERDUR = 8;

    public static enum Type {
        Unused, Easter, JulianEaster, SCEaster
    }

    private ChangeOfRegimeSpec changeOfRegime;
    private boolean automatic;
    private int duration;
    private RegressionTestSpec test;
    private Type type;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .type(Type.Unused)
                .duration(DEF_EASTERDUR)
                .test(RegressionTestSpec.None);
    }

    @Override
    public EasterSpec validate() throws IllegalArgumentException {
        if (duration <= 0 || duration > 25) {
            throw new IllegalArgumentException("Should be in [1,25]");
        }
        return this;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }
    
    public boolean isUsed() {
        return type != Type.Unused;
    }
    
    public static EasterSpec none(){
        return DEFAULT;
    }

    public static class Builder implements Validatable.Builder<EasterSpec> {

        public Builder easterSpec(boolean preTest) {
            return easterSpec(preTest, false);
        }

        public Builder easterSpec(boolean preTest, boolean julian) {
            this.test = RegressionTestSpec.Add;
            this.type = julian ? Type.JulianEaster : Type.Easter;
            this.duration = DEF_EASTERDUR;
            this.test = preTest ? RegressionTestSpec.Add : RegressionTestSpec.None;
            return this;
        }
    }

}
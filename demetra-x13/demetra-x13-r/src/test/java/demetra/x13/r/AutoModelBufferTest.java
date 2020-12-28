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
package demetra.x13.r;

import demetra.regarima.AutoModelSpec;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class AutoModelBufferTest {

    public AutoModelBufferTest() {
    }

    @Test
    public void testDefault() {
        AutoModelSpec spec = AutoModelSpec.DEFAULT_ENABLED;
        AutoModelBuffer buffer = AutoModelBuffer.of(spec);
        AutoModelSpec nspec = buffer.build();
        assertTrue(spec.equals(nspec));
    }

}

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
package demetra.sa;

import demetra.processing.ProcDiagnostic;
import demetra.processing.ProcQuality;
import demetra.timeseries.regression.ModellingContext;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public final class SaItem {

    @lombok.NonNull
    String name;

    @lombok.NonNull
    SaDefinition definition;

    @lombok.Singular("meta")
    @lombok.EqualsAndHashCode.Exclude
    Map<String, String> meta;

    /**
     * Operational. Importance of this estimation
     */
    @lombok.EqualsAndHashCode.Exclude
    int priority;

    /**
     * All information available after processing.
     * SA processors must be able to generate full estimations starting from
     * definitions
     */
    @lombok.experimental.NonFinal
    @lombok.EqualsAndHashCode.Exclude
    private volatile SaEstimation estimation;

    @lombok.experimental.NonFinal
    @lombok.EqualsAndHashCode.Exclude
    private volatile ProcQuality quality;

    @lombok.experimental.NonFinal
    @lombok.EqualsAndHashCode.Exclude
    private volatile boolean processed;

    public SaItem withPriority(int priority) {
        return new SaItem(name, definition, meta, priority, estimation, quality, processed);
    }

    public SaItem withName(String name) {
        return new SaItem(name, definition, meta, priority, estimation, quality, processed);
    }

    public SaItem withInformations(Map<String, String> info) {
        return new SaItem(name, definition, Collections.unmodifiableMap(info), priority, estimation, quality, processed);
    }

    public void accept() {
        if (!processed) {
            return;
        }
        this.quality = ProcQuality.Accepted;
    }

    /**
     * Process this item.The Processing is always executed, even if the item
 has already been
 estimated. To avoid re-estimation, use getEstimation (which is not
 verbose by default)
     *
     * @param context Context could be null (if unused)
     * @param verbose
     * @return
     */
    public boolean process(ModellingContext context, boolean verbose) {
        synchronized (this) {
            estimation = SaManager.process(definition, context, verbose);
            processed = true;
            // update quality
            quality = estimation == null ? ProcQuality.Undefined : ProcDiagnostic.summary(estimation.getDiagnostics());
        }
        return estimation != null;
    }

    /**
     * Gets the current estimation (Processing should be controlled by isProcessed).
     *
     * @param context
     * @return The current estimation
     */
    public SaEstimation getEstimation() {
        return estimation;
    }

    
    /**
     * Remove the results (useful in case of memory problems), but keep
     * the quality
     */
    public void reset() {
        SaEstimation e = estimation;
        if (e != null) {
            synchronized (this) {
                estimation = null;
                processed = false;
            }
        }
    }

    public SaDocument asDocument() {
        SaEstimation e = getEstimation();
        if (e == null) {
            return new SaDocument(name, definition.getTs(), definition.activeSpecification(),
                    null, null, quality);
        } else {
            return new SaDocument(name, definition.getTs(), definition.activeSpecification(),
                    e.getResults(), e.getDiagnostics(), quality);
        }
    }

}

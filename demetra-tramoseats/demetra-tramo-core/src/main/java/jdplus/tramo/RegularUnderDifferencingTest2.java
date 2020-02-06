/*
 * Copyright 2013-2014 National Bank of Belgium
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
package jdplus.tramo;

import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regsarima.regular.RegArimaModelling;
import demetra.arima.SarimaOrders;
import jdplus.stats.AutoCovariances;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
class RegularUnderDifferencingTest2 extends ModelController {

    @Override
    ProcessingResult process(RegArimaModelling modelling, TramoProcessor.Context context) {
       ModelDescription desc = modelling.getDescription();
        if (desc.getAnnualFrequency() <= 2) {
            return ProcessingResult.Unprocessed;
        }
        ModelStatistics stats = ModelStatistics.of(modelling.build());
        if (stats.getLjungBoxPvalue() >= .005) {
            return ProcessingResult.Unchanged;
        }
        if (!needProcessing(modelling)) {
            return ProcessingResult.Unchanged;
        }
        RegArimaModelling ncontext = buildNewModel(modelling);
        ModelEstimation nmodel = ncontext.build();
        if (nmodel == null) {
            return ProcessingResult.Failed;
        }
        ModelComparator cmp = ModelComparator.builder()
                .build();
        if (cmp.compare(nmodel, modelling.build()) < 0) {
//            setReferenceModel(smodel);
            transferInformation(ncontext, modelling);
            return ProcessingResult.Changed;
        } else {
            return ProcessingResult.Unchanged;
        }
    }

    private boolean needProcessing(RegArimaModelling context) {
        DoubleSeq y = context.getEstimation().getConcentratedLikelihood().e();
        int npos0 = 0;
        int imax = Math.min(24, y.length() - 1);
        double[] ac = AutoCovariances.autoCovariancesWithZeroMean(y, imax);
        for (int i = 0; i < 12; ++i) {
            if (ac[i] > 0) {
                ++npos0;
            }
        }
        int npos1 = npos0;
        for (int i = 12; i < ac.length; ++i) {
            if (ac[i] > 0) {
                ++npos1;
            }
        }
        return npos0 >= context.getDescription().getAnnualFrequency() || npos0 >= 9 || npos1 >= 17;
    }

    private RegArimaModelling buildNewModel(RegArimaModelling modelling) {
        ModelDescription desc = modelling.getDescription();
        ModelDescription ndesc = ModelDescription.copyOf(desc);
        SarimaOrders spec = desc.specification();
        if (spec.getD() == 2) {
            if (spec.getP() == 3) {
                return null;
            }
            spec.setP(spec.getP() + 1);
            ndesc.setSpecification(spec);
            ndesc.setMean(true);
        } else {
            if (spec.getQ() == 3) {
                return null;
            }
            spec.setQ(spec.getQ() + 1);
            spec.setD(spec.getD() + 1);
            ndesc.setSpecification(spec);
            ndesc.setMean(false);
        }
        RegArimaModelling ncontext = RegArimaModelling.of(ndesc);
        // estimate the new model
        if (!estimate(ncontext, true)) {
            return null;
        }
        return ncontext;
    }

}

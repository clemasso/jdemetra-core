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
package demetra.timeseries.regression;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.timeseries.ITimeDomain;
import demetra.timeseries.ITimePeriod;
import java.time.Period;
import java.util.List;

/**
 * Basic interface for regression variable. The variable may be a single
 * regression variable or a variables group, which have to be considered
 * together.
 *
 * @author Jean Palate
 * @param <E>
 */
@Development(status = Development.Status.Release)
public interface ITsVariable<E extends ITimePeriod> {


    /**
     * Returns in a buffer the data corresponding to a given domain
     *
     * @param domain The time domain for which the data have to be rendered.
     * @param data The buffers that will contain the data.
     * @since 1.5.2
     */
    void data(ITimeDomain<E> domain, List<DataBlock> data);

    /**
     * Returns the supported time span. When it is defined, the definition
     * domain should have the definition Period.
     *
     * @return The supported time span is returned or null if the variable is
     * able to support any time span.
     */
    ITimeDomain<E> getDefinitionDomain();

    /**
     * Returns the supported frequency.
     *
     * @return The supported frequency is returned or TsFrequency.Undefined if
     * the variable is able to support any frequency
     */
    Period getDefinitionPeriod();

    /**
     * Description of this variable
     *
     * @param context Domain of definition of the variable. Could be null
     * @return Short description of this variable. Should never be null.
     */
    String getDescription(Period context);
    /**
     * Dimension (number of actual regression variables) of this variable
     * (group).
     *
     * @return The number of variables provided by this (group of)regression
     * variable(s). 1 in most cases.
     */
    int getDim();

    /**
     * Description of a variable of this variable group.
     *
     * @param idx. The index of the variable. Must belong to [0, getDim()[.
     * @param context Context of the variable. Could be null
     * @return The description of the considered regression variable. When
     * getDim() = 1, getDescription and getItemDescription(0) will often return
     * the same description.
     */
    String getItemDescription(int idx, Period context);

    /**
     * Checks that this regression variable may be used for a given domain. The
     * exact meaning of this condition is left to the implementor. Usually, a
     * variable is not significant if it is 0 (or a constant) on the whole
     * domain.
     *
     * @param domain The considered domain. It must be in the definition domain
     * or compatible with the definition frequency when they are defined.
     * @return True if the variable is significant on the given domain, false
     * otherwise
     */
    boolean isSignificant(ITimeDomain<E> domain);
    
    String getName();
    
    ITsVariable<E> rename(String name);

}

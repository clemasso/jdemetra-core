/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.calendars;

import demetra.timeseries.TsDomain;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
public interface ICalendar {

    /**
     * Gets the days corresponding to holidays
     *
     * @param domain
     * @return The (weighted) number of holidays for each period of the domain.
     * The different arrays correspond to Mondays...Sundays
     */
    Matrix holidays(TsDomain domain);
}

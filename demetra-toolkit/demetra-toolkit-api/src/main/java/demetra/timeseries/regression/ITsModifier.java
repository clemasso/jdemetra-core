/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.regression;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface ITsModifier extends ITsVariable{
    ITsVariable root();
}
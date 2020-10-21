/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.timeseries;

import demetra.time.IsoConverter;
import demetra.time.IsoIntervalConverter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
@lombok.Value(staticConstructor = "of")
public class Day implements TimeSeriesInterval<Period> {

    @lombok.NonNull
    LocalDate day;

    @Override
    public LocalDateTime start() {
        return day.atStartOfDay();
    }

    @Override
    public LocalDateTime end() {
        return day.plusDays(1).atStartOfDay();
    }

    @Override
    public boolean contains(LocalDateTime element) {
        return element.toLocalDate().equals(day);
    }

    @Override
    public Period getDuration() {
        return Period.ofDays(1);
    }

    @Override
    public String toString() {
        return toISO8601();
    }

    @Override
    public String toISO8601() {
        return CONVERTER.format(this).toString();
    }

    @NonNull
    public static Day parse(@NonNull CharSequence text) throws DateTimeParseException {
        return CONVERTER.parse(text);
    }

    private static Day make(LocalDateTime start, Period duration) {
        return Day.of(start.toLocalDate());
    }

    private static final IsoIntervalConverter<Day> CONVERTER
            = new IsoIntervalConverter.StartDuration<>(IsoConverter.LOCAL_DATE_TIME, IsoConverter.PERIOD, Day::make);
}

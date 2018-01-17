/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.data;

import demetra.utilities.functions.DoubleBiPredicate;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;
import java.util.function.IntFunction;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

/**
 * Support class of Sequence.
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class Sequences {

    static final class SequenceIterator<E> implements Iterator<E> {

        private final Sequence<E> seq;
        private int cur = 0;

        SequenceIterator(Sequence<E> seq) {
            this.seq = seq;
        }

        @Override
        public boolean hasNext() {
            return cur < seq.length();
        }

        @Override
        public E next() {
            if (hasNext()) {
                return seq.get(cur++);
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    <E> void forEach(Sequence<E> seq, Consumer<? super E> action) {
        for (int i = 0; i < seq.length(); i++) {
            action.accept(seq.get(i));
        }
    }

    <E> E[] toArray(Sequence<E> seq, IntFunction<E[]> generator) {
        E[] result = generator.apply(seq.length());
        for (int i = 0; i < result.length; i++) {
            result[i] = seq.get(i);
        }
        return result;
    }

    static final class DoubleIterator implements PrimitiveIterator.OfDouble {

        private final DoubleSequence seq;
        private int cur = 0;

        DoubleIterator(DoubleSequence seq) {
            this.seq = seq;
        }

        @Override
        public boolean hasNext() {
            return cur < seq.length();
        }

        @Override
        public double nextDouble() {
            if (hasNext()) {
                return seq.get(cur++);
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void forEachRemaining(DoubleConsumer block) {
            for (; cur < seq.length(); cur++) {
                block.accept(seq.get(cur));
            }
        }
    }

    void forEach(DoubleSequence seq, DoubleConsumer action) {
        for (int i = 0; i < seq.length(); i++) {
            action.accept(seq.get(i));
        }
    }

    Spliterator.OfDouble spliterator(DoubleSequence seq) {
        return Spliterators.spliterator(new DoubleIterator(seq), seq.length(), Spliterator.ORDERED);
    }

    DoubleStream stream(DoubleSequence seq) {
        return StreamSupport.doubleStream(spliterator(seq), false);
    }

    void copyTo(DoubleSequence seq, double[] buffer, int offset) {
        int n = seq.length();
        DoubleReader reader = seq.reader();
        for (int i = 0; i < n; ++i) {
            buffer[offset + i] = reader.next();
        }
    }

    double[] toArray(DoubleSequence seq) {
        double[] result = new double[seq.length()];
        DoubleReader reader = seq.reader();
        for (int i = 0; i < result.length; ++i) {
            result[i] = reader.next();
        }
        return result;
    }

    boolean allMatch(DoubleSequence seq, DoublePredicate pred) {
        int n = seq.length();
        DoubleReader reader = seq.reader();
        for (int i = 0; i < n; ++i) {
            if (!pred.test(reader.next())) {
                return false;
            }
        }
        return true;
    }

    boolean allMatch(DoubleSequence seq1, DoubleSequence seq2, DoubleBiPredicate pred) {
        int n = seq1.length();
        DoubleReader reader1 = seq1.reader();
        DoubleReader reader2 = seq2.reader();
        for (int i = 0; i < n; ++i) {
            if (!pred.test(reader1.next(), reader2.next())) {
                return false;
            }
        }
        return true;
    }

    boolean anyMatch(DoubleSequence seq, DoublePredicate pred) {
        int n = seq.length();
        DoubleReader reader = seq.reader();
        for (int i = 0; i < n; ++i) {
            if (pred.test(reader.next())) {
                return true;
            }
        }
        return false;
    }

    int firstIndexOf(DoubleSequence seq, DoublePredicate pred) {
        int n = seq.length();
        DoubleReader reader = seq.reader();
        for (int i = 0; i < n; ++i) {
            if (pred.test(reader.next())) {
                return i;
            }
        }
        return n;
    }

    int lastIndexOf(DoubleSequence seq, DoublePredicate pred) {
        int n = seq.length();
        for (int i = n - 1; i >= 0; --i) {
            if (pred.test(seq.get(i))) {
                return i;
            }
        }
        return -1;
    }

    double reduce(DoubleSequence seq, double initial, DoubleBinaryOperator fn) {
        double cur = initial;
        int n = seq.length();
        DoubleReader reader = seq.reader();
        for (int i = 0; i < n; ++i) {
            cur = fn.applyAsDouble(cur, reader.next());
        }
        return cur;
    }

    int count(DoubleSequence seq, DoublePredicate pred) {
        int n = seq.length();
        int c = 0;
        DoubleReader reader = seq.reader();
        for (int i = n - 1; i >= 0; --i) {
            if (pred.test(reader.next()))  {
                ++c;
            }
        }
        return c;
    }

}
package org.codehaus.prometheus.processors;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import java.util.Iterator;

/**
 * The ThrowingIterator is an iterator that iterates of a set of items. If
 * the current item is an instanceof of a RuntimeException, it is thrown,
 * else the item is returned (so checked exceptions are also returned).
 *
 * @author Peter Veentjer.
 */
public class ThrowingIterator implements Iterator {
    private final Iterator it;

    public ThrowingIterator(Object... items) {
        it = unmodifiableList(asList(items)).iterator();
    }

    public boolean hasNext() {
        return it.hasNext();
    }

    public Object next() {
        Object item = it.next();
        if (item instanceof RuntimeException)
            throw (RuntimeException) item;
        return item;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
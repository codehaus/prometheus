package org.codehaus.prometheus.processors.standardprocessor;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import java.util.List;

/**
 * Default implementation ofthe {@link StopPolicy}. It uses a set of classes to check if
 * it should stop.
 *
 * @author Peter Veentjer.
 */
public class DefaultStopPolicy implements StopPolicy {

    private final List<Class> classList;

    /**
     * Creates a new DefaultStopPolicy with the given set of classes.
     *
     * @param classes the list of classes
     * @throws NullPointerException if classes is null.
     */
    public DefaultStopPolicy(Class... classes) {
        if (classes == null) throw new NullPointerException();
        classList = unmodifiableList(asList(classes));
    }

    public List<Class> getClassList() {
        return classList;
    }

    public boolean shouldStop(Object item) {
        if (item == null) throw new NullPointerException();

        Class itemClass = item.getClass();
        for (Class stopClass : classList) {
            if (stopClass.isAssignableFrom(itemClass))
                return true;
        }

        return false;
    }
}

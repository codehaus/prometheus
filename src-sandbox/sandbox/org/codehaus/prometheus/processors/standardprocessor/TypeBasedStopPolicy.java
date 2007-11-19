package org.codehaus.prometheus.processors.standardprocessor;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation ofthe {@link StopPolicy}. It uses a set of classes to check if
 * it should stop.  If it receive an object that is a subtype of on item of the typeset
 * it returns false, true otherwise.
 *
 * @author Peter Veentjer.
 */
public class TypeBasedStopPolicy implements StopPolicy {

    private final Set<Class> classes;

    /**
     * Creates a new TypeBasedStopPolicy with the given set of classes.
     *
     * @param classes the list of classes
     * @throws NullPointerException if classes is null.
     */
    public TypeBasedStopPolicy(Class... classes) {
        this(new HashSet(asList(classes)));
    }

    /**
     *
     * @param classes
     * @throws NullPointerException
     */
    public TypeBasedStopPolicy(Set<Class> classes){
        if (classes == null) throw new NullPointerException();
        this.classes = unmodifiableSet(classes);
    }

    public Set<Class> getClasses() {
        return classes;
    }

    public boolean shouldStop(Object item) {
        if (item == null) throw new NullPointerException();

        Class itemClass = item.getClass();
        for (Class stopClass : classes) {
            if (stopClass.isAssignableFrom(itemClass))
                return true;
        }

        return false;
    }
}

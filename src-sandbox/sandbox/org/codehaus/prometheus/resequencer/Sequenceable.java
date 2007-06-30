package org.codehaus.prometheus.resequencer;

/**
 * todo: better name.
 * <p/>
 * Using an annotation also would be an option I think..
 *
 * @author Peter Veentjer.
 */
public interface Sequenceable {

    /**
     * Returns the index of this Sequenceable.
     *
     * @return
     */
    long getIndex();
}

/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.resourcepool;

/**
 * todo:
 * make remark about LendableReference.
 *
 * @author Peter Veentjer.
 */
public interface ResourcePool<E> {

    void takeback(E e);
}

/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.channels.closeable;

import org.codehaus.prometheus.channels.OutputChannel;
import org.codehaus.prometheus.channels.WaitingOutputChannel;

/**
 *
 */
public class CloseableOutputChannel<E> extends WaitingOutputChannel<E> {

    public CloseableOutputChannel(OutputChannel<E> target) {
        super(target, null);
    }
}

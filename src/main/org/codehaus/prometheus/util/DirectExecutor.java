/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import java.util.concurrent.Executor;

/**
 * The DirectExecutor is an {@link Executor} that executes the task on the Thread that offers the
 * task. The DirectExecutor is very useful for unittesting because no extra threads are introduced.
 *
 * @author Peter Veentjer.
 */
public class DirectExecutor implements Executor {

	public void execute(Runnable command) {
		if(command == null)throw new NullPointerException();
		command.run();
	}
}

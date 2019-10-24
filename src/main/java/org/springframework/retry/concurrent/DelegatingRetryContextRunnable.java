/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.retry.concurrent;

import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.util.Assert;

/**
 * <p>
 * Wraps a delegate {@link Runnable} with logic for setting up a {@link RetryContext}
 * before invoking the delegate {@link Runnable} and then removing the
 * {@link RetryContext} after the delegate has completed.
 * </p>
 * <p>
 * If there is a {@link RetryContext} that already exists, it will be restored after the
 * {@link #run()} method is invoked.
 * </p>
 * Inspired by org.springframework.security.concurrent package.
 *
 * @author Rob Winch
 * @author Aleksandr Shamukov
 */
public final class DelegatingRetryContextRunnable implements Runnable {

	private final Runnable delegate;

	/**
	 * The {@link RetryContext} that the delegate {@link Runnable} will be ran as.
	 */
	private final RetryContext delegateRetryContext;

	/**
	 * The {@link RetryContext} that was on the {@link RetrySynchronizationManager} prior
	 * to being set to the delegateRetryContext.
	 */
	private RetryContext originalRetryContext;

	/**
	 * Creates a new {@link DelegatingRetryContextRunnable} with a specific
	 * {@link RetryContext}.
	 * @param delegate the delegate {@link Runnable} to run with the specified
	 * {@link RetryContext}. Cannot be null.
	 * @param retryContext the {@link RetryContext} to establish for the delegate
	 * {@link Runnable}. Cannot be null.
	 */
	public DelegatingRetryContextRunnable(Runnable delegate, RetryContext retryContext) {
		Assert.notNull(delegate, "delegate cannot be null");
		Assert.notNull(retryContext, "retryContext cannot be null");
		this.delegate = delegate;
		this.delegateRetryContext = retryContext;
	}

	/**
	 * Creates a new {@link DelegatingRetryContextRunnable} with the {@link RetryContext}
	 * from the {@link RetrySynchronizationManager}.
	 * @param delegate the delegate {@link Runnable} to run under the current
	 * {@link RetryContext}. Cannot be null.
	 */
	public DelegatingRetryContextRunnable(Runnable delegate) {
		this(delegate, RetrySynchronizationManager.getContext());
	}

	@Override
	public void run() {
		this.originalRetryContext = RetrySynchronizationManager.getContext();

		try {
			RetrySynchronizationManager.register(delegateRetryContext);
			delegate.run();
		}
		finally {
			RetrySynchronizationManager.register(originalRetryContext);
			this.originalRetryContext = null;
		}
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	/**
	 * Factory method for creating a {@link DelegatingRetryContextRunnable}.
	 * @param delegate the original {@link Runnable} that will be delegated to after
	 * establishing a {@link RetryContext} on the {@link RetrySynchronizationManager}.
	 * Cannot have null.
	 * @param retryContext the {@link RetryContext} to establish before invoking the
	 * delegate {@link Runnable}. If null, the current {@link RetryContext} from the
	 * {@link RetrySynchronizationManager} will be used.
	 * @return
	 */
	public static Runnable create(Runnable delegate, RetryContext retryContext) {
		Assert.notNull(delegate, "delegate cannot be  null");
		return retryContext == null ? new DelegatingRetryContextRunnable(delegate)
				: new DelegatingRetryContextRunnable(delegate, retryContext);
	}

}

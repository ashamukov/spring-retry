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

import java.util.concurrent.Callable;

/**
 * <p>
 * Wraps a delegate {@link Callable} with logic for setting up a {@link RetryContext}
 * before invoking the delegate {@link Callable} and then removing the
 * {@link RetryContext} after the delegate has completed.
 * </p>
 * <p>
 * If there is a {@link RetryContext} that already exists, it will be restored after the
 * {@link #call()} method is invoked.
 * </p>
 * Inspired by org.springframework.security.concurrent package.
 *
 * @author Rob Winch
 * @author Aleksandr Shamukov
 */
public final class DelegatingRetryContextCallable<V> implements Callable<V> {

	private final Callable<V> delegate;

	/**
	 * The {@link RetryContext} that the delegate {@link Callable} will be ran as.
	 */
	private final RetryContext delegateRetryContext;

	/**
	 * The {@link RetryContext} that was on the {@link RetrySynchronizationManager} prior
	 * to being set to the delegateRetryContext.
	 */
	private RetryContext originalRetryContext;

	/**
	 * Creates a new {@link DelegatingRetryContextCallable} with a specific
	 * {@link RetryContext}.
	 * @param delegate the delegate {@link DelegatingRetryContextCallable} to run with the
	 * specified {@link RetryContext}. Cannot be null.
	 * @param retryContext the {@link RetryContext} to establish for the delegate
	 * {@link Callable}. Cannot be null.
	 */
	public DelegatingRetryContextCallable(Callable<V> delegate, RetryContext retryContext) {
		Assert.notNull(delegate, "delegate cannot be null");
		Assert.notNull(retryContext, "retryContext cannot be null");
		this.delegate = delegate;
		this.delegateRetryContext = retryContext;
	}

	/**
	 * Creates a new {@link DelegatingRetryContextCallable} with the {@link RetryContext}
	 * from the {@link RetrySynchronizationManager}.
	 * @param delegate the delegate {@link Callable} to run under the current
	 * {@link RetryContext}. Cannot be null.
	 */
	public DelegatingRetryContextCallable(Callable<V> delegate) {
		this(delegate, RetrySynchronizationManager.getContext());
	}

	@Override
	public V call() throws Exception {
		this.originalRetryContext = RetrySynchronizationManager.getContext();

		try {
			RetrySynchronizationManager.register(delegateRetryContext);
			return delegate.call();
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
	 * Creates a {@link DelegatingRetryContextCallable} and with the given
	 * {@link Callable} and {@link RetryContext}, but if the retryContext is null will
	 * defaults to the current {@link RetryContext} on the
	 * {@link RetrySynchronizationManager}
	 * @param delegate the delegate {@link DelegatingRetryContextCallable} to run with the
	 * specified {@link RetryContext}. Cannot be null.
	 * @param retryContext the {@link RetryContext} to establish for the delegate
	 * {@link Callable}. If null, defaults to
	 * {@link RetrySynchronizationManager#getContext()}
	 * @return
	 */
	public static <V> Callable<V> create(Callable<V> delegate, RetryContext retryContext) {
		return retryContext == null ? new DelegatingRetryContextCallable<>(delegate)
				: new DelegatingRetryContextCallable<>(delegate, retryContext);
	}

}

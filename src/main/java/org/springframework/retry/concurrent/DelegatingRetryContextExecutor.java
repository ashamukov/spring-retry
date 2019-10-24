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

import java.util.concurrent.Executor;

/**
 * An {@link Executor} which wraps each {@link Runnable} in a
 * {@link DelegatingRetryContextRunnable}.
 * Inspired by org.springframework.security.concurrent package.
 *
 * @author Rob Winch
 * @author Aleksandr Shamukov
 */
public class DelegatingRetryContextExecutor extends AbstractDelegatingRetryContextSupport implements Executor {

	private final Executor delegate;

	/**
	 * Creates a new {@link DelegatingRetryContextExecutor} that uses the specified
	 * {@link RetryContext}.
	 * @param delegateExecutor the {@link Executor} to delegate to. Cannot be null.
	 * @param retryContext the {@link RetryContext} to use for each
	 * {@link DelegatingRetryContextRunnable} or null to default to the current
	 * {@link RetryContext}
	 */
	public DelegatingRetryContextExecutor(Executor delegateExecutor, RetryContext retryContext) {
		super(retryContext);
		Assert.notNull(delegateExecutor, "delegateExecutor cannot be null");
		this.delegate = delegateExecutor;
	}

	/**
	 * Creates a new {@link DelegatingRetryContextExecutor} that uses the current
	 * {@link RetryContext} from the {@link RetrySynchronizationManager} at the time the
	 * task is submitted.
	 * @param delegate the {@link Executor} to delegate to. Cannot be null.
	 */
	public DelegatingRetryContextExecutor(Executor delegate) {
		this(delegate, null);
	}

	public final void execute(Runnable task) {
		task = wrap(task);
		delegate.execute(task);
	}

	protected final Executor getDelegateExecutor() {
		return delegate;
	}

}
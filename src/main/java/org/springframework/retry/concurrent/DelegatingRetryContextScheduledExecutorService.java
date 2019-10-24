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

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * An {@link ScheduledExecutorService} which wraps each {@link Runnable} in a
 * {@link DelegatingRetryContextRunnable} and each {@link Callable} in a
 * {@link DelegatingRetryContextCallable}.
 * Inspired by org.springframework.security.concurrent package.
 *
 * @author Rob Winch
 * @author Aleksandr Shamukov
 */
public final class DelegatingRetryContextScheduledExecutorService extends DelegatingRetryContextExecutorService
		implements ScheduledExecutorService {

	/**
	 * Creates a new {@link DelegatingRetryContextScheduledExecutorService} that uses the
	 * specified {@link RetryContext}.
	 * @param delegateScheduledExecutorService the {@link ScheduledExecutorService} to
	 * delegate to. Cannot be null.
	 * @param retryContext the {@link RetryContext} to use for each
	 * {@link DelegatingRetryContextRunnable} and each
	 * {@link DelegatingRetryContextCallable}.
	 */
	public DelegatingRetryContextScheduledExecutorService(ScheduledExecutorService delegateScheduledExecutorService,
			RetryContext retryContext) {
		super(delegateScheduledExecutorService, retryContext);
	}

	/**
	 * Creates a new {@link DelegatingRetryContextScheduledExecutorService} that uses the
	 * current {@link RetryContext} from the {@link RetrySynchronizationManager}.
	 * @param delegate the {@link ScheduledExecutorService} to delegate to. Cannot be
	 * null.
	 */
	public DelegatingRetryContextScheduledExecutorService(ScheduledExecutorService delegate) {
		this(delegate, null);
	}

	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		command = wrap(command);
		return getDelegate().schedule(command, delay, unit);
	}

	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		callable = wrap(callable);
		return getDelegate().schedule(callable, delay, unit);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		command = wrap(command);
		return getDelegate().scheduleAtFixedRate(command, initialDelay, period, unit);
	}

	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		command = wrap(command);
		return getDelegate().scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}

	private ScheduledExecutorService getDelegate() {
		return (ScheduledExecutorService) getDelegateExecutor();
	}

}

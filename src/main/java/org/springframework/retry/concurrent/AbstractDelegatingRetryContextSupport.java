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

import java.util.concurrent.Callable;

import org.springframework.retry.RetryContext;

/**
 * An internal support class that wraps {@link Callable} with
 * {@link DelegatingRetryContextCallable} and {@link Runnable} with
 * {@link DelegatingRetryContextRunnable}
 * Inspired by org.springframework.security.concurrent package.
 *
 * @author Rob Winch
 * @author Aleksandr Shamukov
 */
abstract class AbstractDelegatingRetryContextSupport {

	private final RetryContext retryContext;

	/**
	 * Creates a new {@link AbstractDelegatingRetryContextSupport} that uses the specified
	 * {@link RetryContext}.
	 * @param retryContext the {@link RetryContext} to use for each
	 * {@link DelegatingRetryContextRunnable} and each
	 * {@link DelegatingRetryContextCallable} or null to default to the current
	 * {@link RetryContext}.
	 */
	AbstractDelegatingRetryContextSupport(RetryContext retryContext) {
		this.retryContext = retryContext;
	}

	protected final Runnable wrap(Runnable delegate) {
		return DelegatingRetryContextRunnable.create(delegate, retryContext);
	}

	protected final <T> Callable<T> wrap(Callable<T> delegate) {
		return DelegatingRetryContextCallable.create(delegate, retryContext);
	}

}

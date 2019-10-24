/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.retry.support;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.springframework.retry.concurrent.DelegatingRetryContextExecutorService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncReschedulingTestTool {

	public static void main(String[] args) {

		// Enable tracing
		Logger root = Logger.getRootLogger();
		root.removeAllAppenders();
		root.addAppender(new ConsoleAppender(new PatternLayout("%r [%t] %p %c{1} %x - %m%n")));
		Logger.getRootLogger().setLevel(Level.TRACE);

		ScheduledExecutorService reschedulingExecutor = Executors.newScheduledThreadPool(3,
				r -> new Thread(r, "rescheduler-thread"));

		RetryTemplate template = RetryTemplate.builder().maxAttempts(5).fixedBackoff(1000)
				.asyncRetry(reschedulingExecutor).build();

		AtomicInteger cnt = new AtomicInteger();

		// Client can manually wrap it's executor by DelegatingRetryContextExecutorService
		// to preserve RetryContext (and features, that rely on it)
		ExecutorService secretUsersExecutor = new DelegatingRetryContextExecutorService(
				Executors.newFixedThreadPool(1, r -> new Thread(r, "user-thread")));

		CompletableFuture<String> cf = template.execute(ctx -> CompletableFuture.supplyAsync(() -> {

			try {
				Thread.sleep(1000); // work
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (cnt.incrementAndGet() > 3) {
				printThreadNameAndCtx("done at ");
				return "done";
			}
			else {
				printThreadNameAndCtx("fail at ");
				throw new RuntimeException("exc");
			}

		}, secretUsersExecutor));

		cf.thenRunAsync(() -> {
			System.out.println("User's downstream callback at " + Thread.currentThread().getName());
		});
	}

	private static void printThreadNameAndCtx(String comment) {
		System.out.println(comment + Thread.currentThread().getName() + ", ctx is "
				+ String.valueOf(RetrySynchronizationManager.getContext()));
	}

}

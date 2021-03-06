/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.impl.work.worker.distributed;

import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.lang.Assertion;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author pchretien
 */
final class WFuture<WR> implements Future<WR>, WorkResultHandler<WR> {
	private final AtomicBoolean done = new AtomicBoolean(false);
	private final CountDownLatch countDownLatch = new CountDownLatch(1);
	private Throwable myError;
	private WR myResult;
	private final WorkResultHandler<WR> redirect;

	WFuture(final WorkResultHandler<WR> redirect) {
		Assertion.checkNotNull(redirect);
		//-----
		this.redirect = redirect;
	}

	WFuture() {
		redirect = null;
	}

	@Override
	public void onDone(final WR result, final Throwable error) {
		Assertion.checkArgument(result == null ^ error == null, "result xor error is null");
		//-----
		//-----
		if (done.compareAndSet(false, true)) {
			myResult = result;
			myError = error;
			countDownLatch.countDown();
		}
		if (redirect != null) {
			redirect.onDone(result, error);
		}
	}

	@Override
	public void onStart() {
		if (redirect != null) {
			redirect.onStart();
		}
	}

	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		if (done.compareAndSet(false, true)) {
			myResult = null;
			myError = new CancellationException();
			countDownLatch.countDown();
			return true;
		}
		return false;
	}

	@Override
	public boolean isCancelled() {
		if (done.get()) {
			try {
				countDownLatch.await();
			} catch (final InterruptedException e) {
				throw new RuntimeException(e);
			}
			return myError instanceof CancellationException;
		}
		return false;
	}

	@Override
	public boolean isDone() {
		return done.get() && countDownLatch.getCount() == 0;
	}

	@Override
	public WR get() throws InterruptedException, ExecutionException {
		countDownLatch.await();
		if (myResult != null) {
			return myResult;
		}
		if (myError instanceof CancellationException) {
			throw (CancellationException) new CancellationException().initCause(myError);
		}
		throw new ExecutionException(myError);
	}

	@Override
	public WR get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (!countDownLatch.await(timeout, unit)) {
			throw new TimeoutException();
		}
		if (myResult != null) {
			return myResult;
		}
		if (myError instanceof CancellationException) {
			throw (CancellationException) new CancellationException().initCause(myError);
		}
		throw new ExecutionException(myError);
	}

	//	public static void rethrow(final ExecutionException e) throws IOException {
	//		final Throwable cause = e.getCause();
	//		if (cause instanceof IOException)
	//			throw (IOException) cause;
	//		if (cause instanceof Error)
	//			throw (Error) cause;
	//		if (cause instanceof RuntimeException)
	//			throw (RuntimeException) cause;
	//		throw new RuntimeException(cause);
	//	}
}

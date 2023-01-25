package io.github.chadhartman.dynamic.subpartition.router;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import io.github.chadhartman.dynamic.subpartition.router.model.Message;

/**
 * A message processing subpartition.
 */
@Dependent
public class Worker {

	private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

	@Inject
	Logger logger;

	private final int id = ID_GENERATOR.incrementAndGet();
	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
			1,
			1,
			Long.MAX_VALUE,
			TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<>()
	);

	private String currentKey;

	/**
	 * Submit a message for async processing.
	 * <p>
	 * Non-blocking call. Must be called in the same thread as {@link #hasKey(String)}
	 * and {@link #isVacant()}.
	 *
	 * @param message the message to process.
	 * @throws IllegalArgumentException when the worker is not {@link #isVacant()}
	 *                                  and is processing a different {@link Message#getKey()}.
	 */
	public void submit(Message message) {

		if (Objects.nonNull(currentKey) && !Objects.equals(currentKey, message.getKey())) {
			var desc = String.format(
					"Tried to submit incompatible key \"%s\" to worker %d when currently working on \"%s\"",
					message.getKey(),
					id,
					currentKey
			);
			throw new IllegalArgumentException(desc);
		}

		currentKey = message.getKey();
		executor.submit(() -> {
			message.run();
			logger.debugf("[worker_%d] Completed \"%s\"", id, message.getKey());
		});

		logger.debugf("[worker_%d] Submitted \"%s\"", id, message.getKey());
	}

	/**
	 * Determines whether this worker is processing the provided key.
	 * <p>
	 * Must be called in the same thread as {@link #isVacant()}
	 * and {@link #submit(Message)}.
	 *
	 * @param k an unsubmitted message's key.
	 * @return true if this worker is currently working on that key.
	 */
	public boolean hasKey(String k) {
		return Objects.equals(currentKey, k);
	}

	/**
	 * Determines whether this worker has nothing to process.
	 * <p>
	 * Must be called in the same thread as {@link #hasKey(String)}
	 * and {@link #submit(Message)}.
	 *
	 * @return true if it's executor is idle and has no queued work.
	 */
	public boolean isVacant() {

		var vacant = executor.getActiveCount() == 0 && executor.getQueue().isEmpty();
		if (vacant) {
			currentKey = null;
		}

		return vacant;
	}

	/**
	 * Shutdown the executor service allowing it to complete all of its work (thread-blocking call).
	 */
	public void shutdown() {
		List<Runnable> incompleted = Collections.emptyList();

		try {
			executor.shutdown();
			if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
				incompleted = executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			incompleted = executor.shutdownNow();
		}

		if (!incompleted.isEmpty()) {
			logger.errorf("%d messages were discarded.", incompleted.size());
		}
	}
}

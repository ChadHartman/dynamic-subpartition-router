package io.github.chadhartman.dynamic.subpartition.router;

import java.util.Random;

import javax.enterprise.context.ApplicationScoped;

import io.github.chadhartman.dynamic.subpartition.router.model.Message;

/**
 * Generated {@link Message} to demonstrate dynamic subpartition routing.
 */
@ApplicationScoped
public class MessageGenerator {

	private static final int SLEEP_MIN_MS = 100;
	private static final int SLEEP_MAX_MS = 1000;

	// All possible messages
	private static final String[] KEYS = {
			"alpha",
			"beta",
			"gamma",
			"delta",
			"epsilon",
			"theta",
			"eta",
			"iota"
	};

	private final Random random = new Random();

	/**
	 * Generate a message for the test case.
	 *
	 * @return a newly generated message.
	 */
	public Message generate() {
		var key = KEYS[random.nextInt(KEYS.length)];
		var processTime = SLEEP_MIN_MS + random.nextInt(SLEEP_MAX_MS - SLEEP_MIN_MS);
		return new Message(key, processTime);
	}
}

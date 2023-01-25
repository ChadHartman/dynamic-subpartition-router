package io.github.chadhartman.dynamic.subpartition.router;

import javax.inject.Inject;

import io.github.chadhartman.dynamic.subpartition.router.model.Message;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Application entry point demonstrating dynamic subpartition routing.
 */
@QuarkusMain
public class Main implements QuarkusApplication {

	@Inject
	MessageGenerator generator;

	@Inject
	MessageRouter router;

	@Override
	public int run(String... args) {

		for (var i = 0; i < 100; ++i) {
			submit(generator.generate());
		}

		return 0;
	}

	private void submit(Message message) {
		while (!router.route(message)) {
			// All threads are at capacity; will try again in 10 milliseconds.
			try {
				Thread.sleep(10);
			} catch (InterruptedException ignored) {
			}
		}
	}
}

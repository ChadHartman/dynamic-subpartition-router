package io.github.chadhartman.dynamic.subpartition.router;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.github.chadhartman.dynamic.subpartition.router.model.Message;
import io.quarkus.runtime.ShutdownEvent;

/**
 * Top-level routing component which should be utilized by a single message-consuming thread.
 */
@ApplicationScoped
public class MessageRouter {

	@Inject
	List<Worker> workers;

	/**
	 * Route a message to an appropriate subpartition.
	 *
	 * @param message the message to route.
	 * @return true if the message can be marked "read". If false; routing will need to be attempted again later.
	 */
	public boolean route(Message message) {
		return workers.stream()
				.filter(w -> w.hasKey(message.getKey()))
				.findFirst()
				.or(this::getVacant)
				.map(w -> {
					w.submit(message);
					return true;
				}).orElse(false);
	}

	void onShutdown(@Observes ShutdownEvent ignored) {
		workers.forEach(Worker::shutdown);
	}

	private Optional<Worker> getVacant() {
		return workers.stream()
				.filter(Worker::isVacant)
				.findFirst();
	}
}

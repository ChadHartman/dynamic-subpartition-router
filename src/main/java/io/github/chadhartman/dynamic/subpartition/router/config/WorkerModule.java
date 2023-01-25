package io.github.chadhartman.dynamic.subpartition.router.config;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.github.chadhartman.dynamic.subpartition.router.Worker;

/**
 * Module to generate the subpartitions.
 */
@Dependent
public class WorkerModule {

	@ConfigProperty(name = "worker_count", defaultValue = "5")
	int workerCount;

	@Inject
	Instance<Worker> worker;

	@ApplicationScoped
	public List<Worker> getWorkers() {

		var workers = new ArrayList<Worker>(workerCount);

		for (var i = 0; i < workerCount; ++i) {
			workers.add(worker.get());
		}

		return workers;
	}
}

package io.github.chadhartman.dynamic.subpartition.router.model;

/**
 * A runnable message entity.
 */
public class Message implements Runnable {

	private final String key;
	private final long processTimeMs;

	/**
	 * @param key           the key for this message
	 * @param processTimeMs the time it takes to process this message
	 */
	public Message(String key, long processTimeMs) {
		this.key = key;
		this.processTimeMs = processTimeMs;
	}

	/**
	 * @return this message's key.
	 */
	public String getKey() {
		return key;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(processTimeMs);
		} catch (InterruptedException ignored) {
		}
	}
}

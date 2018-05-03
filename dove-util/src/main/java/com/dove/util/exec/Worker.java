package com.dove.util.exec;

/**
 * A simple thread that waits for completion of a given Process.
 * 
 * @author Liang Zhinian - 
 * @since 1.0
 */
public class Worker extends Thread {
	private final Process process;

	private Integer exit;

	public Worker(Process process) {
		this.process = process;
	}

	public void run() {
		try {
			exit = process.waitFor();
		} catch (InterruptedException ignore) {
			return;
		}
	}

	public Integer getExit() {
		return exit;
	}
}

package com.dove.util.exec;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used to execute system commands
 * 
 * @author Liang Zhinian - 
 * @since 1.0
 */
public class Exec {

	protected static Logger log = LoggerFactory.getLogger(Exec.class);

	public static boolean isWindows() {
		boolean windows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
		return windows;
	}

	/**
	 * Executes the command by using the process builder.
	 * 
	 * @commandLine The command line to process
	 * @timeout The timeout in seconds
	 */
	public static void exec2(List<String> commandLine, File directory, int timeout) throws IOException {
		log.debug("Executing command: " + commandLine);
		ProcessBuilder pb = new ProcessBuilder();
		pb.redirectErrorStream(true);
		pb.command(commandLine);
		pb.directory(directory);

		Process process = pb.start();

		StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), log);
		outputGobbler.start();

		Worker worker = new Worker(process);
		worker.start();

		try {
			if (timeout > 0)
				worker.join(timeout * 1000);
			else
				worker.join();
			if (worker.getExit() == null)
				throw new TimeoutException();
		} catch (TimeoutException e) {
			log.error("Command timed out");
		} catch (InterruptedException ex) {
			worker.interrupt();
			Thread.currentThread().interrupt();
		} finally {
			try {
				process.destroy();
			} catch (Throwable t) {

			}
		}
	}

	/**
	 * Execute the command by using the Runtime.getRuntime().exec()
	 */
	public static int exec(List<String> commandLine) throws IOException {
		return exec(commandLine, null, null, -1);
	}

	/**
	 * Execute the command by using the Runtime.getRuntime().exec()
	 */
	public static int exec(final List<String> commandLine, String[] env, File dir, int timeout) throws IOException {
		int exit = 0;

		final Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[0]), env, dir);

		if (timeout > 0) {
			ExecutorService service = Executors.newSingleThreadExecutor();
			try {
				Callable<Integer> call = new CallableProcess(process);
				Future<Integer> future = service.submit(call);
				exit = future.get(timeout, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				process.destroy();
				String message = "Timeout command " + commandLine;
				log.warn(message);
			} catch (Exception e) {
				log.warn("Command failed to execute - " + commandLine);
				exit = 1;
			} finally {
				service.shutdown();
			}
		}

		StreamEater errEater = new StreamEater("err", process.getErrorStream());

		StreamEater outEater = new StreamEater("out", process.getInputStream());

		Thread a = new Thread(errEater);
		a.start();

		Thread b = new Thread(outEater);
		b.start();

		try {
			exit = process.waitFor();
		} catch (InterruptedException e) {

		}

		try {
			process.destroy();
		} catch (Throwable t) {

		}

		return exit;
	}

	public static String exec(String commandLine, String[] env, File dir) throws IOException {
		Process process = Runtime.getRuntime().exec(commandLine, env, dir != null ? dir : null);
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		StringBuffer out = new StringBuffer();
		String s;
		while ((s = reader.readLine()) != null)
			out.append(s);
		return out.toString();
	}

	public static int exec(final String commandLine, String[] env, File dir, Writer outputWriter, int timeout)
			throws IOException {
		int exit = 0;

		final Process process = Runtime.getRuntime().exec(commandLine, env, dir);

		if (timeout > 0) {
			ExecutorService service = Executors.newSingleThreadExecutor();
			try {
				Callable<Integer> call = new CallableProcess(process);
				Future<Integer> future = service.submit(call);
				exit = future.get(timeout, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				process.destroy();
				String message = "Timeout command " + commandLine;
				log.warn(message);
			} catch (Exception e) {
				log.warn("Command failed to execute - " + commandLine);
				exit = 1;
			} finally {
				service.shutdown();
			}
		}

		StreamEater errEater = new StreamEater("err", process.getErrorStream());

		StreamEater outEater = new StreamEater("out", process.getInputStream(), outputWriter);

		Thread a = new Thread(errEater);
		a.start();

		Thread b = new Thread(outEater);
		b.start();

		try {
			exit = process.waitFor();
		} catch (InterruptedException e) {

		}

		try {
			process.destroy();
		} catch (Throwable t) {

		}

		if (outputWriter != null) {
			outputWriter.flush();
		}

		return exit;
	}

	/**
	 * Execute the command by using the Runtime.exec
	 */
	public static int exec(final String commandLine, String[] env, File dir, int timeout) throws IOException {
		return exec(commandLine, env, dir, null, timeout);
	}

	static class CallableProcess implements Callable<Integer> {
		private Process p;

		public CallableProcess(Process process) {
			p = process;
		}

		public Integer call() throws Exception {
			return p.waitFor();
		}
	}
}
package ssw.mj.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ConcurrentTest extends CompilerTestCaseSupport {
	private static final int NUM_THREADS = 10;
	private static final int NUM_REPEATS = 50;

	private Thread[] threads;
	private List<Throwable> errors;

	private class TestRunnable implements Runnable {
		public void run() {
			try {
				for (int i = 0; i < NUM_REPEATS; i++) {
					SymbolTableTest t = new SymbolTableTest();
					t.setUp();
					t.scriptExample();
				}
			} catch (Throwable ex) {
				errors.add(ex);
			}
		}
	}

	@Before
	public void setUp() {
		errors = Collections.synchronizedList(new ArrayList<Throwable>());
		threads = new Thread[NUM_THREADS];
		for (int i = 0; i < NUM_THREADS; i++) {
			threads[i] = new Thread(new TestRunnable());
		}
	}

	@Test
	public void concurrentCompilation() throws InterruptedException {
		for (Thread thread : threads) {
			thread.start();
		}
		for (Thread thread : threads) {
			thread.join();
		}

		if (errors.size() > 0) {
			for (Throwable ex : errors) {
				System.err.println("** Exception during concurrent execution");
				ex.printStackTrace();
			}
			assertEquals("exceptions occured", 0, errors.size());
		}
	}
}

package ssw.mj.test;

import ssw.mj.codegen.Decoder;

public class Configuration {
	/**
	 * set to true to print expected and actual values of all output (errors,
	 * tokens, symbol table, code)
	 */
	public static final boolean PRINT_ALL_TESTCASE_OUTPUT = Boolean.getBoolean("microjava.testcaseOutput");

	/**
	 * Set to true to print debug information of the interpreter. Equal to
	 * "-debug" on the command line. <br>
	 * Remark:<br>
	 * This is a lot of output, some test cases might timeout, e.g.
	 * CodeGenerationTest.fib
	 */
	public static final boolean PRINT_INTERPRETER_DEBUG_OUTPUT = Boolean.getBoolean("microjava.interpreterOutput");

	/**
	 * Prints the decoded byte code before it executes the test case. Outside
	 * the test cases this can be done with {@link Decoder}.
	 */
	public static final boolean PRINT_DECODED_BYTE_CODE = Boolean.getBoolean("microjava.decodeBytecode");

	/**
	 * Determines the timeout after which a test case should fail automatically.
	 * Default: 10000 (= 10 seconds). The default should work for all test cases
	 * on most machines.<br>
	 * <em>Attention</em>: For most computers it is likely that there is an
	 * endless loop in the MicroJava compiler if a test fails for a timeout.
	 */
	public static final long DEFAULT_TIMEOUT = Long.getLong("microjava.timeout", 1000 * 10);
}

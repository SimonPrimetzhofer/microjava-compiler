package ssw.mj.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;

import ssw.mj.Errors;
import ssw.mj.Interpreter;
import ssw.mj.Token;
import ssw.mj.codegen.Decoder;
import ssw.mj.impl.ParserImpl;
import ssw.mj.impl.ScannerImpl;
import ssw.mj.symtab.Obj;
import ssw.mj.symtab.Scope;
import ssw.mj.symtab.Struct;
import ssw.mj.symtab.Tab;

/**
 * Base class for test cases with utility methods used by all tests.
 */
public abstract class CompilerTestCaseSupport {

	public static final String CR = "\r";
	public static final String LF = "\n";
	private List<String> expectedErrors;
	private List<String> expectedTokens;
	private List<String> expectedSymTab;
	private ScannerImpl scanner;
	protected ParserImpl parser;
	private List<String> runInputs = new ArrayList<String>();
	private List<String> expectedOutputs = new ArrayList<String>();

	@Rule
	public Timeout globalTimeout = Timeout.millis(Configuration.DEFAULT_TIMEOUT);

	@Before
	public void setUp() {
		// initialize expected compiler output
		expectedErrors = new ArrayList<String>();
		expectedTokens = new ArrayList<String>();
		expectedSymTab = new ArrayList<String>();

		if (Configuration.PRINT_ALL_TESTCASE_OUTPUT) {
			// print header for console output
			System.out.println("--------------------------------------------------");
		}
	}

	protected void initScanner(String s) {
		scanner = new ScannerImpl(new StringReader(s));
	}

	protected void init(String s) {
		initScanner(s);
		parser = new ParserImpl(scanner);
	}

	protected void initScannerFile(String s) {
		try {
			scanner = new ScannerImpl(new FileReader(new File("tests", s)));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	protected void initFile(String s) {
		initScannerFile(s);
		parser = new ParserImpl(scanner);
	}

	private List<String> splitString(String s) {
		StringTokenizer st = new StringTokenizer(s, "\n");
		List<String> result = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			result.add(st.nextToken());
		}
		return result;
	}

	private void print(String title, List<String> expected, List<String> actual) {
		if (Configuration.PRINT_ALL_TESTCASE_OUTPUT || !expected.equals(actual)) {
			System.out.println(title);
			System.out.format("  %-60s %s\n", "expected", "actual");
			int lines = Math.max(expected.size(), actual.size());
			for (int i = 0; i < lines; i++) {
				String expectedLine = (i < expected.size() ? expectedLine = expected.get(i) : "");
				String actualLine = (i < actual.size() ? actualLine = actual.get(i) : "");
				System.out.format("%s %-60s %s\n", (expectedLine.equals(actualLine) ? " " : "x"), expectedLine,
						actualLine);
			}
		}
	}

	protected void scanAndVerify() {
		List<String> actualTokens = new ArrayList<String>();
		// scan only the expected number of tokens to prevent endless loops
		for (int i = 0; i < getExpectedTokens().size(); i++) {
			actualTokens.add(scanner.next().toString());
		}

		print("Errors", expectedErrors, getActualErrors());
		print("Tokens", getExpectedTokens(), actualTokens);

		Assert.assertEquals("Errors", expectedErrors, getActualErrors());
		Assert.assertEquals("Tokens", getExpectedTokens(), actualTokens);
		Assert.assertTrue("Complete Input Scanned", scanner.next().toString().contains("end of file"));
	}

	protected void addExpectedRunVarArgs(String output, int... args) {
		StringBuffer sb = new StringBuffer();
		for (int arg : args) {
			sb.append(arg).append(' ');
		}
		addExpectedRun(sb.toString(), output);
	}

	protected void addExpectedRun(String output) {
		addExpectedRun("", output);
	}

	protected void addExpectedRun(String input, String output) {
		runInputs.add(input);
		expectedOutputs.add(output);
	}

	protected void parseAndVerify() {
		try {
			parser.parse();
			Assert.assertTrue("Complete input should be scanned", scanner.next().kind == Token.Kind.eof);
		} catch (Errors.PanicMode error) {
			// Ignore, nothing to do
		}

		// print all output before the first assertion to support debugging
		print("Errors", expectedErrors, getActualErrors());
		if (expectedSymTab.size() > 0) {
			print("Symbol Table", getExpectedSymTab(), getActualSymTab());
		}

		Assert.assertEquals("Errors", expectedErrors, getActualErrors());
		if (expectedSymTab.size() > 0) {
			Assert.assertEquals("Symbol Table", getExpectedSymTab(), getActualSymTab());
		}

		if (Configuration.PRINT_DECODED_BYTE_CODE) {
			System.out.println("------ Byte code --------------");
			System.out.println(new Decoder().decode(parser.code.buf, 0, parser.code.pc));
			System.out.println("-------------------------------");
		}

		for (int i = 0; i < runInputs.size(); i++) {
			Interpreter.BufferIO io = new Interpreter.BufferIO(runInputs.get(i));
			Interpreter inter = new Interpreter(parser.code.buf, parser.code.mainpc, parser.code.dataSize, io,
					Configuration.PRINT_INTERPRETER_DEBUG_OUTPUT);
			inter.run();
			String output = io.getOutput();
			Assert.assertEquals("Unexpected result when input is \"" + runInputs.get(i) + "\": ",
					expectedOutputs.get(i), output);
		}
	}

	private List<String> getActualErrors() {
		return splitString(scanner.errors.dump());
	}

	private List<String> getExpectedTokens() {
		return expectedTokens;
	}

	private List<String> getExpectedSymTab() {
		return expectedSymTab;
	}

	private List<String> getActualSymTab() {
		return splitString(dump(parser.tab));
	}

	protected void expectError(int line, int col, Errors.Message msg, Object... msgParams) {
		expectedErrors.add("-- line " + line + " col " + col + ": " + msg.format(msgParams));
	}

	protected void expectToken(Token.Kind kind, int line, int col) {
		expectedTokens.add("line " + line + ", col " + col + ", kind " + kind);
	}

	protected void expectToken(Token.Kind kind, int line, int col, String str) {
		expectedTokens.add("line " + line + ", col " + col + ", kind " + kind + ", str " + str);
	}

	protected void expectToken(Token.Kind kind, int line, int col, int val) {
		expectedTokens.add("line " + line + ", col " + col + ", kind " + kind + ", val " + val);
	}

	protected void expectToken(Token.Kind kind, int line, int col, char val) {
		expectedTokens.add("line " + line + ", col " + col + ", kind " + kind + ", val '" + val + "'");
	}

	protected void expectSymTab(String line) {
		expectedSymTab.add(line);
	}

	protected void expectSymTabUniverse() {
		// first part of the symbol table (universe) that is equal for all
		// programs
		expectSymTab("-- begin scope (0 variables) --");
		expectSymTab("Type int: int");
		expectSymTab("Type char: char");
		expectSymTab("Constant: class(0) null = 0");
		expectSymTab("Method: char chr(1)");
		expectSymTab("  Local Variable 0: int i");
		expectSymTab("Method: int ord(1)");
		expectSymTab("  Local Variable 0: char ch");
		expectSymTab("Method: int len(1)");
		expectSymTab("  Local Variable 0: void[] arr");
	}

	private static String dump(Tab tab) {
		StringBuilder sb = new StringBuilder();
		if (tab.curScope != null) {
			dump(tab.curScope, sb);
		}
		return sb.toString();
	}

	private static void dump(Scope scope, StringBuilder sb) {
		sb.append("-- begin scope (").append(scope.nVars()).append(" variables) --\n");
		if (!scope.locals().isEmpty()) {
			dump(scope.locals(), sb, "");
		}
		if (scope.outer() != null) {
			sb.append("\n");
			dump(scope.outer(), sb);
		}
	}

	private static void dump(Collection<Obj> objects, StringBuilder sb, String indent) {
		for (Obj obj : objects) {
			dump(obj, sb, indent);
		}
	}

	private static void dump(Obj obj, StringBuilder sb, String indent) {
		sb.append(indent);

		switch (obj.kind) {
		case Con:
			sb.append("Constant: ");
			if (obj.type != null) {
				dump(obj.type, sb, indent, false);
			}
			sb.append(" ").append(obj.name).append(" = ");
			if (obj.type == Tab.charType) {
				sb.append("'").append((char) obj.val).append("'");
			} else {
				sb.append(obj.val);
			}
			break;
		case Var:
			if (obj.level == 0) {
				sb.append("Global Variable ");
			} else {
				sb.append("Local Variable ");
			}
			sb.append(obj.adr).append(": ");
			if (obj.type != null) {
				dump(obj.type, sb, indent, false);
			}
			sb.append(" ").append(obj.name);
			break;
		case Type:
			sb.append("Type ").append(obj.name).append(": ");
			if (obj.type != null) {
				dump(obj.type, sb, indent + "  ", true);
			}
			break;
		case Meth:
			sb.append("Method: ");
			if (obj.type != null) {
				dump(obj.type, sb, indent, false);
			}
			sb.append(" ").append(obj.name).append("(").append(obj.nPars);
			sb.append(")");
			if(obj.hasVarArg) {
				sb.append(" hasVarArg");
			}
			break;
		case Prog:
			sb.append("Program ").append(obj.name).append(":");
			break;
		}

		if (obj.locals != null) {
			sb.append("\n");
			dump(obj.locals, sb, indent + "  ");
		}
		sb.append("\n");
	}

	private static void dump(Struct struct, StringBuilder sb, String indent, boolean dumpFields) {
		switch (struct.kind) {
		case None:
			sb.append("void");
			break;
		case Int:
			sb.append("int");
			break;
		case Char:
			sb.append("char");
			break;
		case Arr:
			if (struct.elemType != null) {
				dump(struct.elemType, sb, indent, dumpFields);
			}
			sb.append("[]");
			break;
		case Class:
			sb.append("class(").append(struct.nrFields()).append(")");
			if (dumpFields && struct.fields != null) {
				sb.append("\n");
				dump(struct.fields, sb, indent);
			}
			break;
		}
	}
}

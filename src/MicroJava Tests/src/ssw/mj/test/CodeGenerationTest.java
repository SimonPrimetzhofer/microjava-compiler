package ssw.mj.test;

import static ssw.mj.Errors.Message.*;

import org.junit.Test;

/**
 * Test cases for the examples from the lab sessions 8 and 9.
 */
public class CodeGenerationTest extends CompilerTestCaseSupport {

	/**
	 * Symbol table for all examples of this test class.
	 */
	private void expectSymTab() {
		expectSymTabUniverse();
		expectSymTab("Program A:");
		expectSymTab("  Constant: int max = 12");
		expectSymTab("  Global Variable 0: char c");
		expectSymTab("  Global Variable 1: int i");
		expectSymTab("  Type B: class(2)");
		expectSymTab("    Local Variable 0: int x");
		expectSymTab("    Local Variable 1: int y");
		expectSymTab("  Method: void main(0)");
		expectSymTab("    Local Variable 0: int[] iarr");
		expectSymTab("    Local Variable 1: class(2) b");
		expectSymTab("    Local Variable 2: int n");
	}

	private void expectSymTabWithSum() {
		expectSymTab();
		expectSymTab("    Local Variable 3: int sum");
	}

	@Test
	public void bsp11() {
		init("program A" + LF + //
				"  final int max = 12;" + LF + //
				"  char c; int i;" + LF + //
				"  class B { int x, y; }" + LF + //
				"{" + LF + //
				"  void main ()" + LF + //
				"    int[] iarr; B b; int n;" + LF + //
				"  {" + LF + //
				"    read(i); " + LF + //
				"    if (i <= n) n = 1;" + LF + //
				"    print(n); " + LF + //
				"  }" + LF + //
				"}");

		expectSymTab();
		addExpectedRun("0", "1");
		addExpectedRun("1", "0");
		parseAndVerify();
	}

	@Test
	public void bsp12() {
		init("program A" + LF + //
				"  final int max = 12;" + LF + //
				"  char c; int i;" + LF + //
				"  class B { int x, y; }" + LF + //
				"{" + LF + //
				"  void main ()" + LF + //
				"    int[] iarr; B b; int n;" + LF + //
				"  {" + LF + //
				"    read(i); " + LF + //
				"    n = 1; " + LF + //
				"    if (i <= n && n < 0) n = 2;" + LF + //
				"    print(n); " + LF + //
				"  }" + LF + //
				"}");

		expectSymTab();
		addExpectedRun("0", "1");
		addExpectedRun("2", "1");
		parseAndVerify();
	}

	@Test
	public void bsp13() {
		init("program A" + LF + //
				"  final int max = 12;" + LF + //
				"  char c; int i;" + LF + //
				"  class B { int x, y; }" + LF + //
				"{" + LF + //
				"  void main ()" + LF + //
				"    int[] iarr; B b; int n;" + LF + //
				"  {" + LF + //
				"    read(i); " + LF + //
				"    n = 1; " + LF + //
				"    if (i <= n || i < 10) n = 2;" + LF + //
				"    print(n); " + LF + //
				"  }" + LF + //
				"}");

		expectSymTab();
		addExpectedRun("0", "2");
		addExpectedRun("2", "2");
		addExpectedRun("20", "1");
		parseAndVerify();
	}

	@Test
	public void bsp14() {
		init("program A" + LF + //
				"  final int max = 12;" + LF + //
				"  char c; int i;" + LF + //
				"  class B { int x, y; }" + LF + //
				"{" + LF + //
				"  void main ()" + LF + //
				"    int[] iarr; B b; int n;" + LF + //
				"  {" + LF + //
				"    read(i); " + LF + //
				"    n = 1; " + LF + //
				"    if (i <= n || i < 10 && i > 5) n = 2;" + LF + //
				"    print(n); " + LF + //
				"  }" + LF + //
				"}");

		expectSymTab();
		addExpectedRun("0", "2");
		addExpectedRun("2", "1");
		addExpectedRun("6", "2");
		addExpectedRun("20", "1");
		parseAndVerify();
	}

	@Test
	public void bsp15() {
		init("program A" + LF + //
				"  final int max = 12;" + LF + //
				"  char c; int i;" + LF + //
				"  class B { int x, y; }" + LF + //
				"{" + LF + //
				"  void main ()" + LF + //
				"    int[] iarr; B b; int n;" + LF + //
				"  {" + LF + //
				"    read(n); " + LF + //
				"    while (i <= n) { i++; }" + LF + //
				"    print(i); " + LF + //
				"  }" + LF + //
				"}");

		expectSymTab();
		addExpectedRun("0", "1");
		addExpectedRun("-1", "0");
		addExpectedRun("1", "2");
		addExpectedRun("10", "11");
		parseAndVerify();
	}

	@Test
	public void bsp16() {
		init("program A" + LF + //
				"  final int max = 12;" + LF + //
				"  char c; int i;" + LF + //
				"  class B { int x, y; }" + LF + //
				"{" + LF + //
				"  void main ()" + LF + //
				"    int[] iarr; B b; int n;" + LF + //
				"  {" + LF + //
				"    read(i); " + LF + //
				"    if (i <= max) n = 1; else n = 2;" + LF + //
				"    print(n); " + LF + //
				"  }" + LF + //
				"}");

		expectSymTab();
		addExpectedRun("0", "1");
		addExpectedRun("13", "2");
		addExpectedRun("12", "1");
		addExpectedRun("-1", "1");
		addExpectedRun("-13", "1");
		parseAndVerify();
	}

	@Test
	public void bsp17() {
		init("program A" + LF + //
				"  final int max = 12;" + LF + //
				"  char c; int i;" + LF + //
				"  class B { int x, y; }" + LF + //
				"{" + LF + //
				"  void main ()" + LF + //
				"    int[] iarr; B b; int n; int sum;" + LF + //
				"  {" + LF + //
				"    read(n); " + LF + //
				"    sum = 0; " + LF + //
				"    while (i <= n) { sum += i; i++; }" + LF + //
				"    print(sum); " + LF + //
				"  }" + LF + //
				"}");

		expectSymTabWithSum();
		addExpectedRun("0", "0");
		addExpectedRun("-1", "0");
		addExpectedRun("1", "1");
		addExpectedRun("10", "55");
		parseAndVerify();
	}

	@Test
	public void bsp18() {
		init("program A" + LF + //
				"  final int max = 12;" + LF + //
				"  char c; int i;" + LF + //
				"  class B { int x, y; }" + LF + //
				"{" + LF + //
				"  void main ()" + LF + //
				"    int[] iarr; B b; int n; int sum;" + LF + //
				"  {" + LF + //
				"    read(n); " + LF + //
				"    sum = 0; " + LF + //
				"    i = 2;" + LF + //
				"    while (i <= n) { sum += i; i++; }" + LF + //
				"    print(sum); " + LF + //
				"  }" + LF + //
				"}");

		expectSymTabWithSum();
		addExpectedRun("0", "0");
		addExpectedRun("-1", "0");
		addExpectedRun("1", "0");
		addExpectedRun("10", "54");
		parseAndVerify();
	}

	@Test
	public void methodCall() {
		init("program A" + LF + // 1
				"{" + LF + // 2
				"  void bar() {" + LF + // 3
				"    print('b');" + LF + // 4
				"    print('a');" + LF + // 5
				"    print('r');" + LF + // 6
				"  }" + LF + // 7
				"  void foo() {" + LF + // 8
				"    print('f');" + LF + // 9
				"    print('o');" + LF + // 10
				"    print('o');" + LF + // 11
				"  }" + LF + // 12
				"  void main () {" + LF + // 13
				"    foo();" + LF + // 14
				"  }" + LF + // 15
				"}"); // 16

		addExpectedRun("", "foo");
		parseAndVerify();
	}

	@Test
	public void fib() {
		init("program A" + LF + //
				"{" + LF + //
				"  int fib(int n) {" + LF + //
				"     if (n <= 1) return 1; " + LF + //
				"     return fib(n-1) + fib(n-2); " + LF + //
				"  }" + LF + //
				"  void main ()" + LF + //
				"    int n;" + LF + //
				"  {" + LF + //
				"    read(n); " + LF + //
				"    print(fib(n)); " + LF + //
				"  }" + LF + //
				"}");

		addExpectedRun("-1", "1");
		addExpectedRun("0", "1");
		addExpectedRun("1", "1");
		addExpectedRun("2", "2");
		addExpectedRun("3", "3");
		addExpectedRun("4", "5");
		addExpectedRun("5", "8");
		addExpectedRun("6", "13");
		addExpectedRun("7", "21");
		addExpectedRun("8", "34");
		addExpectedRun("9", "55");
		addExpectedRun("10", "89");
		addExpectedRun("11", "144");
		addExpectedRun("22", "28657");
		parseAndVerify();
	}

	@Test
	public void fibDyn() {
		init("program A" + LF + //
				" int[] matrix; " + LF + //
				"{" + LF + //
				"  int fib(int n) int r; {" + LF + //
				"     if (n <= 1) return 1; " + LF + //
				"     if(matrix[n] != 0) return matrix[n]; " + LF + //
				"     r = fib(n-1) + fib(n-2); " + LF + //
				"     matrix[n] = r; " + LF + //
				"     return r; " + LF + //
				"  }" + LF + //
				"  void main ()" + LF + //
				"    int n;" + LF + //
				"  {" + LF + //
				"    matrix = new int[1000]; " + LF + //
				"    read(n); " + LF + //
				"    print(fib(n)); " + LF + //
				"  }" + LF + //
				"}");

		addExpectedRun("-1", "1");
		addExpectedRun("0", "1");
		addExpectedRun("1", "1");
		addExpectedRun("2", "2");
		addExpectedRun("3", "3");
		addExpectedRun("4", "5");
		addExpectedRun("5", "8");
		addExpectedRun("6", "13");
		addExpectedRun("7", "21");
		addExpectedRun("8", "34");
		addExpectedRun("9", "55");
		addExpectedRun("10", "89");
		addExpectedRun("11", "144");
		addExpectedRun("22", "28657");
		addExpectedRun("30", "1346269");
		addExpectedRun("40", "165580141");
		addExpectedRun("45", "1836311903");
		parseAndVerify();
	}

	@Test
	public void testElseIf() {
		init("program Test {" + LF + // 1
				"  void main() int i; {" + LF + // 2
				"    read(i);" + LF + // 3
				"    if (i == 1) print(9);" + LF + // 4
				"    else if (i == 2) print(8);" + LF + // 5
				"    else print(7);" + LF + // 6
				"  }" + LF + // 7
				"}");
		addExpectedRun("1", "9");
		addExpectedRun("2", "8");
		addExpectedRun("3", "7");
		addExpectedRun("4", "7");
		parseAndVerify();
	}

	@Test
	public void mainVar() {
		init("program Test" + LF + //
				"  int main;" + LF + //
				"{" + LF + //
				"}");
		expectError(4, 2, METH_NOT_FOUND, "main");
		parseAndVerify();
	}

	@Test
	public void mainNotVoid() {
		init("program Test {" + LF + //
				"  char main() { }" + LF + //
				"}");
		expectError(2, 15, MAIN_NOT_VOID);
		parseAndVerify();
	}

	@Test
	public void mainWithParams() {
		init("program Test {" + LF + //
				"  void main(int i) { }" + LF + //
				"}");
		expectError(2, 20, MAIN_WITH_PARAMS);
		parseAndVerify();
	}

	@Test
	public void noLoop() {
		init("program Test {" + LF + //
				"  void main() {" + LF + //
				"    break;" + LF + //
				"  }" + LF + //
				"}");
		expectError(3, 10, NO_LOOP);
		parseAndVerify();
	}

	@Test
	public void returnVoid() {
		init("program Test {" + LF + //
				"  void test() {" + LF + //
				"    return 5;" + LF + //
				"  }" + LF + //
				"  void main() {}" + LF + //
				"}");
		expectError(3, 12, RETURN_VOID);
		parseAndVerify();
	}

	@Test
	public void wrongReturnType() {
		init("program Test {" + LF + //
				"  int test() {" + LF + //
				"    return null;" + LF + //
				"  }" + LF + //
				"  void main() {}" + LF + //
				"}");
		expectError(3, 16, RETURN_TYPE);
		parseAndVerify();
	}

	@Test
	public void wrongReturnTypeArr() {
		init("program Test {" + LF + //
				"  int[] test() char[] ca; {" + LF + //
				"    return ca;" + LF + //
				"  }" + LF + //
				"  void main() {}" + LF + //
				"}");
		expectError(3, 14, RETURN_TYPE);
		parseAndVerify();
	}

	@Test
	public void wrongReturnClass() {
		init("program Test" + LF + //
				"  class C1 { }" + LF + //
				"  class C2 { }" + LF + //
				"{" + LF + //
				"  C1 test() C2 c2; {" + LF + //
				"    return c2;" + LF + //
				"  }" + LF + //
				"  void main() {}" + LF + //
				"}");
		expectError(6, 14, RETURN_TYPE);
		parseAndVerify();
	}

	@Test
	public void noReturnVal() {
		init("program Test {" + LF + //
				"  int test() {" + LF + //
				"    return;" + LF + //
				"  }" + LF + //
				"  void main() {}" + LF + //
				"}");
		expectError(3, 11, RETURN_NO_VAL);
		parseAndVerify();
	}

	@Test
	public void noMeth() {
		init("program Test {" + LF + //
				"  void main() int i; {" + LF + //
				"    i(10);" + LF + //
				"  }" + LF + //
				"}");
		expectError(3, 7, NO_METH);
		parseAndVerify();
	}

	@Test
	public void paramType() {
		init("program Test {" + LF + //
				"  void method(int x) { }" + LF + //
				"  void main() {" + LF + //
				"    method('a');" + LF + //
				"  }" + LF + //
				"}");
		expectError(4, 15, PARAM_TYPE);
		parseAndVerify();
	}

	@Test
	public void paramTypeArr() {
		init("program Test {" + LF + //
				"  void method(int[] x) { }" + LF + //
				"  void main() {" + LF + //
				"    method(new char[10]);" + LF + //
				"  }" + LF + //
				"}");
		expectError(4, 24, PARAM_TYPE);
		parseAndVerify();
	}

	@Test
	public void paramTypeClass() {
		init("program Test" + LF + //
				"  class C1 { }" + LF + //
				"  class C2 { }" + LF + //
				"{" + LF + //
				"  void method(C1 c1) { }" + LF + //
				"  void main() {" + LF + //
				"    method(new C2);" + LF + //
				"  }" + LF + //
				"}");
		expectError(7, 18, PARAM_TYPE);
		parseAndVerify();
	}

	@Test
	public void moreParams() {
		init("program Test {" + LF + //
				"  void method(int x, char c) { }" + LF + //
				"  void main() {" + LF + //
				"    method(1, 'a', 1);" + LF + //
				"  }" + LF + //
				"}");
		expectError(4, 21, MORE_ACTUAL_PARAMS);
		parseAndVerify();
	}

	@Test
	public void lessParams() {
		init("program Test {" + LF + //
				"  void method(int x, char c) { }" + LF + //
				"  void main() {" + LF + //
				"    method(1);" + LF + //
				"  }" + LF + //
				"}");
		expectError(4, 13, LESS_ACTUAL_PARAMS);
		parseAndVerify();
	}

	@Test
	public void incompTypesCond() {
		init("program Test {" + LF + //
				"  void main() int i; {  " + LF + //
				"    if (i > null) { }" + LF + //
				"  }" + LF + //
				"}");
		expectError(3, 17, INCOMP_TYPES);
		parseAndVerify();
	}

	@Test
	public void incompTypesCondArr() {
		init("program Test {" + LF + //
				"  void main() int[] ia; char[] ca; {  " + LF + //
				"    if (ia > ca) { }" + LF + //
				"  }" + LF + //
				"}");
		expectError(3, 16, INCOMP_TYPES);
		parseAndVerify();
	}

	@Test
	public void incompTypesCondClass() {
		init("program Test" + LF + //
				"  class C1 { }" + LF + //
				"{" + LF + //
				"  void main() C1 c1; int i; {  " + LF + //
				"    if (c1 > i) { };" + LF + //
				"  }" + LF + //
				"}");
		expectError(5, 15, INCOMP_TYPES);
		parseAndVerify();
	}

	@Test
	public void wrongEqCheck() {
		init("program Test {" + LF + //
				"  void main() int[] ia1, ia2; {" + LF + //
				"    if (ia1 > ia2) { }" + LF + //
				"  }" + LF + //
				"}");
		expectError(3, 18, EQ_CHECK);
		parseAndVerify();
	}

	@Test
	public void testSimpleBreak() {
		init("program Test {" + LF + //
				"  void main() {" + LF + //
				"    while(42 > 0) /* while(true) */" + LF + //
				"    {" + LF + //
				"      break;" + LF + //
				"    }" + LF + //
				"  }" + LF + //
				"}");
		parseAndVerify();
	}

	@Test
	public void testBreak() {
		init("program A" + LF + //
				"  int i;" + LF + //
				"{" + LF + //
				"  void main ()" + LF + //
				"    int n;" + LF + //
				"  {" + LF + //
				"    read(n); " + LF + //
				"    while (i <= n) { while(1 < 2) { if(1 == 1) { break; } } if(i == 5) break; i++; }"
				+ LF + //
				"    print(i); " + LF + //
				"  }" + LF + //
				"}");
		addExpectedRun("10", "5");
		parseAndVerify();
	}

	@Test
	public void testNestedBreak() {
		init("program Test {" + LF + //
				"  void main() " + LF + //
				"    int n, o;" + LF + //
				"  {" + LF + //
				"    o = 21;" + LF + //
				"    while(83 < 84)" + LF + //
				"    {" + LF + //
				"      while(167 < 168)" + LF + //
				"      {" + LF + //
				"        break;" + LF + //
				"      }" + LF + //
				"      break;" + LF + //
				"    }" + LF + //
				"  }" + LF + //
				"}");
		parseAndVerify();
	}

	@Test
	public void lenTest() {
		init("program A" + LF + //
				"  class A { int[] x; }" + LF + //
				"  class B { A a; }" + LF + //
				"  class C { B b; }" + LF + //
				"{" + LF + //
				"  void main ()" + LF + //
				"    C[] c;" + LF + //
				"  {" + LF + //
				"    c = new C[5];" + LF + //
				"    print(len(c));" + LF + //
				"  }" + LF + //
				"}");

		addExpectedRun("5");
		parseAndVerify();
	}

	@Test
	public void paramType2() {
		init("program Test {" + LF + //
				"  void method(int x, int y) { }" + LF + //
				"  void main() {" + LF + //
				"    method(1, 'a');" + LF + //
				"  }" + LF + //
				"}");
		expectError(4, 18, PARAM_TYPE);
		parseAndVerify();
	}

	@Test
	public void paramTypeArr2() {
		init("program Test {" + LF + //
				"  void method(int[] x, int y) { }" + LF + //
				"  void main() {" + LF + //
				"    method(new int[10], new char[10]);" + LF + //
				"  }" + LF + //
				"}");
		expectError(4, 37, PARAM_TYPE);
		parseAndVerify();
	}

	@Test
	public void paramTypeClass2() {
		init("program Test" + LF + //
				"  class C1 { }" + LF + //
				"  class C2 { }" + LF + //
				"{" + LF + //
				"  void method(C1 c1, C2 c2) { }" + LF + //
				"  void main() {" + LF + //
				"    method(new C1, new C1);" + LF + //
				"  }" + LF + //
				"}");
		expectError(7, 26, PARAM_TYPE);
		parseAndVerify();
	}

	@Test
	public void testRelops() {
		initFile("relops.mj");
		addExpectedRun("0", "!=,<,<=,");
		addExpectedRun("1", "==,<=,>=,");
		addExpectedRun("2", "!=,>,>=,");
		parseAndVerify();
	}

	@Test
	public void testAnimals() {
		initFile("animals.mj");
		addExpectedRun("0", "cat");
		addExpectedRun("1", "dog");
		addExpectedRun("2", "octopus");
		parseAndVerify();
	}

	@Test
	public void lessActualVarArgs() {
		init("program Test" + LF + // 1
				"{" + LF + // 2
				" void countFields(char[] arrs...) " + LF + // 3
				"   int fieldCount; int arrI; { " + LF + // 4
				"   while(arrI < len(arrs)) { " + LF + // 5
				"     fieldCount += len(arrs[arrI]); " + LF + // 6
				"     print(fieldCount); " + LF + // 7
				"     arrI++; " + LF + // 8
				"   } " + LF + // 9
				" }" + LF + // 10
				" void main() int a; char[] c; { " +  LF + // 11
				"  a = 1; " + LF + // 12
				"  c = new char[10]; " + LF + // 13
				"  countFields(#10 c, new char[a+1]); " + LF + // 14
				" }" + LF + // 16
				"}" + LF // 15
		);
		expectError(14, 35, LESS_ACTUAL_VARARGS);
		parseAndVerify();
	}

	@Test
	public void moreActualVarArgs() {
		init("program Test" + LF + // 1
				"{" + LF + // 2
				" void countFields(char[] arrs...) " + LF + // 3
				"   int fieldCount; int arrI; { " + LF + // 4
				"   while(arrI < len(arrs)) { " + LF + // 5
				"     fieldCount += len(arrs[arrI]); " + LF + // 6
				"     print(fieldCount); " + LF + // 7
				"     arrI++; " + LF + // 8
				"   } " + LF + // 9
				" }" + LF + // 10
				" void main() int a; char[] c; { " +  LF + // 11
				"  a = 1; " + LF + // 12
				"  c = new char[10]; " + LF + // 13
				"  countFields(#1 c, new char[a+1]); " + LF + // 14
				" }" + LF + // 15
				"}" + LF // 16
		);
		expectError(14, 34, MORE_ACTUAL_VARARGS);
		parseAndVerify();
	}
	
	@Test
	public void varArgMoreActParams() {
		init("program Test" + LF + // 1
				"{" + LF + // 2
				" void foo(int x, char ch2...) { while(x >= 0) { print(ch2[x]); x--; }  }" + LF + // 3
				" void main() { foo (3, 'm', 'w', 'r', 'm'); }" + LF + // 4
				" }" + LF // 5
		);
		expectError(4, 42, MORE_ACTUAL_PARAMS);
		parseAndVerify();
	}
	
	@Test
	public void noVarArgFunction() {
		init("program Test" + LF + // 1
				"{" + LF + // 2
				" void foo(int x) { while(x >= 0) { print(x); x--; }  }" + LF + // 3
				" void main() { foo (3 #4 'm', 'w', 'r', 'm'); }" + LF + // 4
				" }" + LF // 5
		);
		expectError(4, 44, INVALID_VARARG_CALL);
		parseAndVerify();
	}
	
	@Test
	public void accessZeroLenVarArg() {
		init("program Test" + LF + // 1
				"{" + LF + // 2
				" void foo(char chars...) { print(len(chars)); }" + LF + // 3
				" void main() { foo (); }" + LF + // 4
				" }" + LF // 5
		);
		addExpectedRun("0");
		parseAndVerify();
	}

	@Test
	public void varArgCorrect1() {
		init("program Test" + LF + // 1
				"{" + LF + // 2
				" void printTwoChars(int pos1, int pos2, char chars...) { " + LF + // 3
				"   print(chars[pos1]); " + LF + // 4
				"   print(chars[pos2]); " + LF + // 5
				" } " + LF + // 6
				" void main() { " +  LF + // 7
				"  printTwoChars(0, 2 #4 'm', 'w', 'r', 'm'); " + LF + // 8
				" }" + LF + // 9
				"}" + LF // 10
		);
		addExpectedRun("mr");
		parseAndVerify();
	}

	@Test
	public void varArgCorrect2() {
		init("program Test" + LF + // 1
				"{" + LF + // 2
				" void printTwoChars(int pos1, int pos2, char chars...) { " + LF + // 3
				"   print(chars[pos1]); " + LF + // 4
				"   print(chars[pos2]); " + LF + // 5
				" } " + LF + // 6
				" void main() char m; { " +  LF + // 7
				"  m = 'm'; " +  LF + // 8
				"  printTwoChars(0, 2 #4 m, 'w', 'r', 'm'); " + LF + // 9
				" }" + LF + // 10
				"}" + LF // 11
		);
		addExpectedRun("mr");
		parseAndVerify();
	}

	@Test
	public void varArgTypeMismatch() {
		init("program Test" + LF + // 1
				"{" + LF + // 2
				" void printTwoChars(int pos1, int pos2, char chars...) { " + LF + // 3
				"   print(chars[pos1]); " + LF + // 4
				"   print(chars[pos2]); " + LF + // 5
				" } " + LF + // 6
				" void main() { " +  LF + // 7
				"  printTwoChars(0, 2 #4 3, 'm', 'w', 'r', 'm'); " + LF + // 8
				" }" + LF + // 9
				"}" + LF // 10
		);
		expectError(8, 26, PARAM_TYPE);
		expectError(8, 46, MORE_ACTUAL_VARARGS);
		parseAndVerify();
	}

	@Test
	public void varArgTypeMismatch2() {
		init("program Test" + LF + // 1
				"{" + LF + // 2
				" void printTwoChars(int pos1, int pos2, char chars...) { " + LF + // 3
				"   print(chars[pos1]); " + LF + // 4
				"   print(chars[pos2]); " + LF + // 5
				" } " + LF + // 6
				" void main() { " +  LF + // 7
				"  printTwoChars(0, 'm' #4 'w', 'r', 'm'); " + LF + // 8
				" }" + LF + // 9
				"}" + LF // 10
		);
		expectError(8, 24, PARAM_TYPE);
		expectError(8, 40, LESS_ACTUAL_VARARGS);
		parseAndVerify();
	}

	@Test
	public void arrayVarArg() {
		init("program Test" + LF + // 1
				"{" + LF + // 2
				" void countFields(char[] arrs...) " + LF + // 3
				"   int fieldCount; int arrI; { " + LF + // 4
				"   while(arrI < len(arrs)) { " + LF + // 5
				"     fieldCount += len(arrs[arrI]); " + LF + // 6
				"     print(fieldCount); " + LF + // 7
				"     arrI++; " + LF + // 8
				"   } " + LF + // 9
				" }" + LF + // 10
				" void main() int a; char[] c; { " +  LF + // 11
				"  a = 1; " + LF + // 12
				"  c = new char[10]; " + LF + // 13
				"  countFields(#2 c, new char[a+1]); " + LF + // 14
				" }" + LF + // 15
				"}" + LF // 16
		);
		addExpectedRun("1012");
		parseAndVerify();
	}

	@Test
	public void arrayVarArgEmpty1() {
		init("program Test" + LF + // 1
				"{" + LF + // 2
				" void countFields(char[] arrs...) " + LF + // 3
				"   int fieldCount; int arrI; { " + LF + // 4
				"   while(arrI < len(arrs)) { " + LF + // 5
				"     fieldCount += len(arrs[arrI]); " + LF + // 6
				"     print(fieldCount); " + LF + // 7
				"     arrI++; " + LF + // 8
				"   } " + LF + // 9
				" }" + LF + // 10
				" void main() int a; char[] c; { " +  LF + // 11
				"  a = 1; " + LF + // 12
				"  c = new char[10]; " + LF + // 13
				"  countFields(); " + LF + // 14
				" }" + LF + // 15
				"}" + LF // 16
		);
		addExpectedRun("");
		parseAndVerify();
	}

	@Test
	public void arrayVarArgEmpty2() {
		init("program Test" + LF + // 1
				"{" + LF + // 2
				" void countFields(char[] arrs...) " + LF + // 3
				"   int fieldCount; int arrI; { " + LF + // 4
				"   while(arrI < len(arrs)) { " + LF + // 5
				"     fieldCount += len(arrs[arrI]); " + LF + // 6
				"     print(fieldCount); " + LF + // 7
				"     arrI++; " + LF + // 8
				"   } " + LF + // 9
				" }" + LF + // 10
				" void main() int a; char[] c; { " +  LF + // 11
				"  a = 1; " + LF + // 12
				"  c = new char[10]; " + LF + // 13
				"  countFields(#0); " + LF + // 14
				" }" + LF + // 15
				"}" + LF // 16
		);
		addExpectedRun("");
		parseAndVerify();
	}
}

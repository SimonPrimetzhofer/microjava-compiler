package ssw.mj.test;

import org.junit.Assert;
import org.junit.Test;
import ssw.mj.Errors.Message;

import static ssw.mj.Errors.Message.*;

/**
 * Test cases for the examples from the lab sessions 8 and 9.
 */
public class SimpleCodeGenerationTest extends CompilerTestCaseSupport {

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

    @Test
    public void undefNameMeth() {
        init("program Test {" + LF + //
                "  void main() {" + LF + //
                "    method();" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 11, NOT_FOUND, "method");
        parseAndVerify();
    }

    @Test
    public void forwardDeclErrorMissingMethod() {
        init("program Test" + LF + // 1
                "{" + LF + // 2
                "  void main() { foo(); }" + LF + // 3
                "  void foo() {}" + LF + // 4
                "}" + LF // 5
        );
        expectError(3, 20, Message.NOT_FOUND, "foo");

        parseAndVerify();
    }

    @Test
    public void undefNameVar() {
        init("program Test {" + LF + //
                "  void main() {" + LF + //
                "    var++;" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 8, NOT_FOUND, "var");
        parseAndVerify();
    }

    @Test
    public void noField() {
        init("program Test" + LF + //
                "  class C { }" + LF + //
                "{" + LF + //
                "  void main() C obj; {" + LF + //
                "    obj.field++;" + LF + //
                "  }" + LF + //
                "}");
        expectError(5, 14, NO_FIELD, "field");
        parseAndVerify();
    }

    @Test
    public void recoverDecl1() {
        init("program Test" + LF + //
                "  int i1, if" + LF + //
                "  in i2;" + LF + //
                "  final int i3 = 0;" + LF + //
                "{" + LF + //
                "  void main() {  " + LF + //
                "    if (i1 < i3);" + LF + //
                "    i2 = 0;" + LF + //
                "  }" + LF + //
                "}");

        expectError(2, 11, TOKEN_EXPECTED, "identifier");
        expectError(8, 8, NOT_FOUND, "i2");
        parseAndVerify();
    }

    @Test
    public void recoverDecl2() {
        init("program Test" + LF + //
                "  int i1, if" + LF + //
                "  in i2;" + LF + //
                "  int i3;" + LF + //
                "{" + LF + //
                "  void main() {  " + LF + //
                "    if (i1 < i3);" + LF + //
                "    i2 = 0;" + LF + //
                "  }" + LF + //
                "}");

        expectError(2, 11, TOKEN_EXPECTED, "identifier");
        expectError(8, 8, NOT_FOUND, "i2");
        parseAndVerify();
    }

    @Test
    public void bspEmpty() {
        init("program A" + LF + //
                "  final int max = 12;" + LF + //
                "  char c; int i;" + LF + //
                "  class B { int x, y; }" + LF + //
                "{" + LF + //
                "  void main ()" + LF + //
                "    int[] iarr; B b; int n;" + LF + //
                "  {" + LF + //
                "  }" + LF + //
                "}");

        expectSymTab();

        parseAndVerify();
    }

    @Test
    public void bsp01() {
        init("program A" + LF + //
                "  final int max = 12;" + LF + //
                "  char c; int i;" + LF + //
                "  class B { int x, y; }" + LF + //
                "{" + LF + //
                "  void main ()" + LF + //
                "    int[] iarr; B b; int n;" + LF + //
                "  {" + LF + //
                "    n = 3;" + LF + //
                "    print(n); " + LF + //
                "  }" + LF + //
                "}");

        expectSymTab();
        addExpectedRun("3");
        parseAndVerify();
    }

    @Test
    public void bsp01a() {
        init("program A" + LF + //
                "  final int max = 12;" + LF + //
                "  char c; int i;" + LF + //
                "  class B { int x, y; }" + LF + //
                "{" + LF + //
                "  void main ()" + LF + //
                "    int[] iarr; B b; int n;" + LF + //
                "  {" + LF + //
                "    n = -1 + 2;" + LF + //
                "    print(n); " + LF + //
                "  }" + LF + //
                "}");

        expectSymTab();
        addExpectedRun("1");
        parseAndVerify();
    }

    @Test
    public void bsp02() {
        init("program A" + LF + //
                "  final int max = 12;" + LF + //
                "  char c; int i;" + LF + //
                "  class B { int x, y; }" + LF + //
                "{" + LF + //
                "  void main ()" + LF + //
                "    int[] iarr; B b; int n;" + LF + //
                "  {" + LF + //
                "    i = 10;" + LF + //
                "    print(i);" + LF + //
                "  }" + LF + //
                "}");

        expectSymTab();
        addExpectedRun("10");
        parseAndVerify();
    }

    @Test
    public void bsp03() {
        init("program A" + LF + //
                "  final int max = 12;" + LF + //
                "  char c; int i;" + LF + //
                "  class B { int x, y; }" + LF + //
                "{" + LF + //
                "  void main ()" + LF + //
                "    int[] iarr; B b; int n;" + LF + //
                "  {" + LF + //
                "    i = 1;" + LF + //
                "    n = 3 + i;" + LF + //
                "    print(n);" + LF + //
                "  }" + LF + //
                "}");

        expectSymTab();
        addExpectedRun("4");
        parseAndVerify();
    }

    @Test
    public void bsp04() {
        init("program A" + LF + //
                "  final int max = 12;" + LF + //
                "  char c; int i;" + LF + //
                "  class B { int x, y; }" + LF + //
                "{" + LF + //
                "  void main ()" + LF + //
                "    int[] iarr; B b; int n;" + LF + //
                "  {" + LF + //
                "    i = 1;" + LF + //
                "    n = 3 + i * max - n;" + LF + //
                "    print(n);" + LF + //
                "  }" + LF + //
                "}");

        expectSymTab();
        addExpectedRun("15");
        parseAndVerify();
    }

    @Test
    public void bsp05() {
        init("program A" + LF + //
                "  final int max = 12;" + LF + //
                "  char c; int i;" + LF + //
                "  class B { int x, y; }" + LF + //
                "{" + LF + //
                "  void main ()" + LF + //
                "    int[] iarr; B b; int n;" + LF + //
                "  {" + LF + //
                "    iarr = new int[10];" + LF + //
                "    iarr[5] = 10;" + LF + //
                "    print(iarr[0]);" + LF + //
                "    print(iarr[5]);" + LF + //
                "  }" + LF + //
                "}");

        expectSymTab();

        addExpectedRun("010");
        parseAndVerify();
    }

    @Test
    public void bsp06() {
        init("program A" + LF + //
                "  final int max = 12;" + LF + //
                "  char c; int i;" + LF + //
                "  class B { int x, y; }" + LF + //
                "{" + LF + //
                "  void main ()" + LF + //
                "    int[] iarr; B b; int n;" + LF + //
                "  {" + LF + //
                "    iarr = new int[10];" + LF + //
                "    iarr[5] = 10;" + LF + //
                "    b = new B;" + LF + //
                "    b.y = iarr[5] * 3;" + LF + //
                "    print(b.y);" + LF + //
                "  }" + LF + //
                "}");

        expectSymTab();
        addExpectedRun("30");

        parseAndVerify();
    }

    @Test
    public void bsp07() {
        init("program A" + LF + //
                "  final int max = 12;" + LF + //
                "  char c; int i;" + LF + //
                "  class B { int x, y; }" + LF + //
                "{" + LF + //
                "  void main ()" + LF + //
                "    int[] iarr; B b; int n;" + LF + //
                "  {" + LF + //
                "    n--;" + LF + //
                "    print(n);" + LF + //
                "  }" + LF + //
                "}");

        expectSymTab();
        addExpectedRun("-1");
        parseAndVerify();
    }

    @Test
    public void bsp08() {
        init("program A" + LF + //
                "  final int max = 12;" + LF + //
                "  char c; int i;" + LF + //
                "  class B { int x, y; }" + LF + //
                "{" + LF + //
                "  void main ()" + LF + //
                "    int[] iarr; B b; int n;" + LF + //
                "  {" + LF + //
                "    i--;" + LF + //
                "    print(i);" + LF + //
                "  }" + LF + //
                "}");

        expectSymTab();
        addExpectedRun("-1");
        parseAndVerify();
    }

    @Test
    public void bsp09() {
        init("program A" + LF + //
                "  final int max = 12;" + LF + //
                "  char c; int i;" + LF + //
                "  class B { int x, y; }" + LF + //
                "{" + LF + //
                "  void main ()" + LF + //
                "    int[] iarr; B b; int n;" + LF + //
                "  {" + LF + //
                "    b = new B;" + LF + //
                "    b.y--;" + LF + //
                "    print(b.y);" + LF + //
                "  }" + LF + //
                "}");

        expectSymTab();
        addExpectedRun("-1");
        parseAndVerify();
    }

    @Test
    public void bsp10() {
        init("program A" + LF + //
                "  final int max = 12;" + LF + //
                "  char c; int i;" + LF + //
                "  class B { int x, y; }" + LF + //
                "{" + LF + //
                "  void main ()" + LF + //
                "    int[] iarr; B b; int n;" + LF + //
                "  {" + LF + //
                "    iarr = new int[10];" + LF + //
                "    iarr[0]--;" + LF + //
                "    print(iarr[0]);" + LF + //
                "  }" + LF + //
                "}");

        expectSymTab();
        addExpectedRun("-1");
        parseAndVerify();
    }

    // ---- Errors in Code.java
    @Test
    public void noVarMethod() {
        init("program Test {" + LF + //
                "  int method() { return 0; }" + LF + //
                "  void main() int i; {" + LF + //
                "    method = i;" + LF + //
                "  }" + LF + //
                "}");
        expectError(4, 15, NO_VAR);
        parseAndVerify();
    }

    @Test
    public void noVarIncMethod() {
        init("program Test {" + LF + //
                "  int method() { return 0; }" + LF + //
                "  void main() int i; {" + LF + //
                "    method++;" + LF + //
                "  }" + LF + //
                "}");
        expectError(4, 11, NO_VAR);
        parseAndVerify();
    }

    @Test
    public void noOperand() {
        init("program Test {" + LF + //
                "  void main() int i; {" + LF + //
                "    Test = i;" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 10, NO_OPERAND);
        parseAndVerify();
    }

    @Test
    public void noValueAssign() {
        init("program Test {" + LF + //
                "  char method() { return 'a'; }" + LF + //
                "  void main() char c; {" + LF + //
                "    c = method;" + LF + //
                "  }" + LF + //
                "}");
        expectError(4, 15, NO_VAL);
        parseAndVerify();
    }

    @Test
    public void noValueCalc() {
        init("program Test {" + LF + //
                "  int method() { return 0; }" + LF + //
                "  void main() int i; {" + LF + //
                "    i = 5 * method;" + LF + //
                "  }" + LF + //
                "}");
        expectError(4, 19, NO_VAL);
        parseAndVerify();
    }

    @Test
    public void noValueInc() {
        init("program Test {" + LF + //
                "  int method() { return 0; }" + LF + //
                "  void main() int i; {" + LF + //
                "    i += method;" + LF + //
                "  }" + LF + //
                "}");
        expectError(4, 16, NO_VAL);
        parseAndVerify();
    }

    @Test
    public void noValueIndex() {
        init("program Test {" + LF + //
                "  int[] getArray() {" + LF + //
                "    return new int[5];" + LF + //
                "  }" + LF + //
                "" + LF + //
                "  void main() {" + LF + //
                "    getArray[2]++;" + LF + //
                "  }" + LF + //
                "}");
        expectError(7, 13, NO_VAL);
        parseAndVerify();
    }

    @Test
    public void assignPlusNoIntOp() {
        init("program Test {" + LF + //
                "  void main() int i; char c; {" + LF + //
                "    c += i;" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 11, NO_INT_OP);
        parseAndVerify();
    }

    @Test
    public void assignTimesNoIntOp() {
        init("program Test {" + LF + //
                "  void main() int i; char c; {" + LF + //
                "    i *= c;" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 11, NO_INT_OP);
        parseAndVerify();
    }

    @Test
    public void incompTypes() {
        init("program Test {" + LF + //
                "  void main() int i; {  " + LF + //
                "    i = null;" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 13, INCOMP_TYPES);
        parseAndVerify();
    }

    @Test
    public void incompTypesArr() {
        init("program Test {" + LF + //
                "  void main() int[] ia; char[] ca; {  " + LF + //
                "    ia = ca;" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 12, INCOMP_TYPES);
        parseAndVerify();
    }

    @Test
    public void incompTypesClass() {
        init("program Test" + LF + //
                "  class C1 { }" + LF + //
                "  class C2 { }" + LF + //
                "{" + LF + //
                "  void main() C1 c1; C2 c2; {  " + LF + //
                "    c1 = c2;" + LF + //
                "  }" + LF + //
                "}");
        expectError(6, 12, INCOMP_TYPES);
        parseAndVerify();
    }

    @Test
    public void noIntegerInc() {
        init("program Test {" + LF + //
                "  void main() char ch; {" + LF + //
                "    ch++;" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 7, NO_INT);
        parseAndVerify();
    }

    @Test
    public void noIntegerDec() {
        init("program Test {" + LF + //
                "  void main() int[] ia; {" + LF + //
                "    ia--;" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 7, NO_INT);
        parseAndVerify();
    }

    @Test
    public void wrongReadValue() {
        init("program Test {" + LF + //
                "  void main() int[] ia; { " + LF + //
                "    read(ia);" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 12, READ_VALUE);
        parseAndVerify();
    }

    @Test
    public void wrongPrintValue() {
        init("program Test" + LF + //
                "  class C { }" + LF + //
                "{" + LF + //
                "  void main() C obj; { " + LF + //
                "    print(obj);" + LF + //
                "  }" + LF + //
                "}");
        expectError(5, 14, PRINT_VALUE);
        parseAndVerify();
    }

    @Test
    public void noIntOpNeg() {
        init("program Test {" + LF + //
                "  void main() int i; char c; {" + LF + //
                "    i = -c;" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 11, NO_INT_OP);
        parseAndVerify();
    }

    @Test
    public void noIntOpPlus() {
        init("program Test {" + LF + //
                "  void main() int i; int[] ia; {" + LF + //
                "    i = i + ia;" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 15, NO_INT_OP);
        parseAndVerify();
    }

    @Test
    public void noIntOpTimes() {
        init("program Test {" + LF + //
                "  void main() int i; int[] ia; {" + LF + //
                "    i = ia * i;" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 15, NO_INT_OP);
        parseAndVerify();
    }

    @Test
    public void procAsFunc() {
        init("program Test {" + LF + //
                "  void main() int x; {" + LF + //
                "    x = main();" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 13, INVALID_CALL);
        parseAndVerify();
    }

    @Test
    public void noTypeNew() {
        init("program Test {" + LF + //
                "  void main() int i; {" + LF + //
                "    i = new main;" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 17, NO_TYPE);
        parseAndVerify();
    }

    @Test
    public void wrongArraySize() {
        init("program Test {" + LF + //
                "  void main() int[] ia; {" + LF + //
                "    ia = new int[ia];" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 20, ARRAY_SIZE);
        parseAndVerify();
    }

    @Test
    public void noClassType() {
        init("program Test {" + LF + //
                "  void main() int i; {" + LF + //
                "    i = new int;" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 16, NO_CLASS_TYPE);
        parseAndVerify();
    }

    @Test
    public void noClass() {
        init("program Test {" + LF + //
                "  void main() int i; {" + LF + //
                "    i = i.i;" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 10, NO_CLASS);
        parseAndVerify();
    }

    @Test
    public void noArrayIndex() {
        init("program Test {" + LF + //
                "  void main() int[] ia; {" + LF + //
                "    ia[ia] = 1;" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 10, ARRAY_INDEX);
        parseAndVerify();
    }

    @Test
    public void noArray() {
        init("program Test {" + LF + //
                "  void main() int i; {" + LF + //
                "    i[i]++;" + LF + //
                "  }" + LF + //
                "}");
        expectError(3, 8, NO_ARRAY);
        parseAndVerify();
    }

    @Test
    public void testPrint() {
        init("program A {" + LF + //
                "  void main () {" + LF + //
                "	 print('a');" + LF + //
                "    print('b',1);" + LF + //
                "    print('c',2);" + LF + //
                "    print('d',3);" + LF + //
                "    print('e',4);" + LF + //
                "  }" + LF + //
                "}");
        addExpectedRun("ab c  d   e");
        parseAndVerify();
    }

    @Test
    public void testDesignator() {
        init("program A" + LF + //
                "  class A { int x; }" + LF + //
                "  class B { A a; }" + LF + //
                "  class C { B b; }" + LF + //
                "{" + LF + //
                "  void main ()" + LF + //
                "    A a; B b; C c;" + LF + //
                "  {" + LF + //
                "    c = new C;" + LF + //
                "    c.b = new B;" + LF + //
                "    c.b.a = new A;" + LF + //
                "    c.b.a.x++;" + LF + //
                "    print(c.b.a.x);" + LF + //
                "  }" + LF + //
                "}");

        addExpectedRun("1");
        parseAndVerify();
    }

    @Test
    public void testArrayAndDesignator() {
        init("program A" + LF + //
                "  class A { int[] x; }" + LF + //
                "  class B { A a; }" + LF + //
                "  class C { B b; }" + LF + //
                "{" + LF + //
                "  void main ()" + LF + //
                "    A a; B b; C[] c;" + LF + //
                "  {" + LF + //
                "    c = new C[5];" + LF + //
                "    c[0] = new C;" + LF + //
                "    c[0].b = new B;" + LF + //
                "    c[0].b.a = new A;" + LF + //
                "    c[0].b.a.x = new int[10];" + LF + //
                "    c[3] = new C;" + LF + //
                "    c[3].b = new B;" + LF + //
                "    c[3].b.a = new A;" + LF + //
                "    c[3].b.a.x = new int[30];" + LF + //
                "    c[0].b.a.x[0]--;" + LF + //
                "    c[0].b.a.x[8]++;" + LF + //
                "    c[3].b.a.x[2]++;" + LF + //
                "    c[3].b.a.x[2]*=3;" + LF + //
                "    c[0].b.a.x[8]+=50 + c[3].b.a.x[2] * c[3].b.a.x[2] * c[0].b.a.x[0];" + LF + //
                "    print(c[0].b.a.x[8]);" + LF + //
                "  }" + LF + //
                "}");

        addExpectedRun("42");
        parseAndVerify();
    }

    @Test
    public void testArrayAndDesignatorAndAssign() {
        init("program A" + LF + //
                "  class A { int[] x; }" + LF + //
                "  class B { A a; }" + LF + //
                "  class C { B b; }" + LF + //
                "{" + LF + //
                "  void main ()" + LF + //
                "    A a; B b; C[] c;" + LF + //
                "  {" + LF + //
                "    c = new C[5];" + LF + //
                "    c[0] = new C;" + LF + //
                "    c[0].b = new B;" + LF + //
                "    c[0].b.a = new A;" + LF + //
                "    c[0].b.a.x = new int[2];" + LF + //
                "    c[3] = new C;" + LF + //
                "    c[3].b = new B;" + LF + //
                "    c[3].b.a = new A;" + LF + //
                "    c[3].b.a.x = new int[3];" + LF + //
                "    c[0].b.a.x[1]++;" + LF + //
                "    c[0].b.a.x[1]*=256;" + LF + //
                "    c[0].b.a.x[1]/=2;" + LF + //
                "    c[0].b.a.x[1]--;" + LF + //
                "    c[0].b.a.x[1]%=64;" + LF + //
                "    c[3].b.a.x[2]++;" + LF + //
                "    c[3].b.a.x[2]*=21;" + LF + //
                "    c[0].b.a.x[1]-=c[3].b.a.x[2];" + LF + //
                "    print(c[0].b.a.x[1]);" + LF + //
                "  }" + LF + //
                "}");

        addExpectedRun("42");
        parseAndVerify();
    }

    @Test
    public void testArrayIndexExpression() {
        init("program A" + LF + // 1
                "{" + LF + // 2
                "  void main()" + LF + // 3
                "    int[] arr;" + LF + // 4
                "  {" + LF + // 5
                "    arr = new int[10];" + LF + // 6
                "    arr[ ( 1 + 2 ) * 3 ] = 4;" + LF + // 7
                "    arr[ 4 - 2 * 2 ] = 2;" + LF + // 8
                "    print(arr[ 90 / 10 ]);" + LF + // 9
                "    print(arr[ 6 - 3 * 2 ]);" + LF + // 10
                "  }" + LF + // 11
                "}");
        addExpectedRun("42");
        parseAndVerify();
    }

    @Test
    public void testReadAndPrint() {
        init("program A" + LF + // 1
                "{" + LF + // 2
                "  void main()" + LF + // 3
                "    int n;" + LF + // 4
                "  {" + LF + // 5
                "    n = 0;" + LF + // 6
                "    read(n);" + LF + // 7
                "    print(n);" + LF + // 7
                "  }" + LF + // 9
                "}");
        addExpectedRun("2", "2");
        parseAndVerify();
    }

    @Test
    public void testFields() {
        init("program A" + LF + //
                "  class A { int x; }" + LF + //
                "  class B { A a; }" + LF + //
                "{" + LF + //
                "  void main()" + LF + //
                "    A a;" + LF + //
                "    B b;" + LF + //
                "  {" + LF + //
                "    a = new A;" + LF + //
                "    b = new B;" + LF + //
                "    a.x = 20;" + LF + //
                "    a.x++;" + LF + //
                "    a.x /= 7;" + LF + //
                "    a.x *= a.x;" + LF + //
                "    a.x %= a.x - 5;" + LF + //
                "    b.a = new A;" + LF + //
                "    b.a.x = -12;" + LF + //
                "    b.a.x -= a.x;" + LF + //
                "    b.a.x *= -a.x;" + LF + //
                "    b.a.x %= 5;" + LF + //
                "    b.a.x *= a.x + 2 * 3;" + LF + //
                "    print(b.a.x);" + LF + //
                "  }" + LF + //
                "}");
        addExpectedRun("21");
        parseAndVerify();
    }

    @Test
    public void twoMethods() {
        init("program A" + LF + // 1
                "{" + LF + // 2
                "  void deadMethodToMoveMainPcFrom0()" + LF + // 3
                "    int n;" + LF + // 4
                "  {" + LF + // 5
                "    n = 0;" + LF + // 6
                "  }" + LF + // 7
                "  void main()" + LF + // 8
                "  {" + LF + // 9
                "    print(2);" + LF + // 10
                "  }" + LF + // 11
                "}");
        addExpectedRun("2");
        parseAndVerify();
        Assert.assertTrue(
                "In this example mainpc must be > 0, most likely it should be 7, but it is: " + parser.code.mainpc,
                parser.code.mainpc > 0);
    }

    @Test
    public void noMain() {
        init("program Test {" + LF + //
                "  void main_() { }" + LF + //
                "}");
        expectError(3, 2, METH_NOT_FOUND, "main");
        parseAndVerify();
    }

    @Test
    public void noValueAssignopMethod() {
        init("program Test {" + LF + //
                "  int method() { return 0; }" + LF + //
                "  void main() int i; {" + LF + //
                "    method += i;" + LF + //
                "  }" + LF + //
                "}");
        expectError(4, 15, NO_VAR);
        parseAndVerify();
    }

    @Test
    public void testMulops() {
        init("program Mulops" + LF + //
                "{" + LF + //
                "  void main ()" + LF + //
                "    int a; int b;" + LF + //
                "  {" + LF + //
                "    a = 42;" + LF + //
                "    b = 3;" + LF + //
                "    a = a / b;" + LF + //
                "    a = a % ( b * b );" + LF + //
                "    print(a);" + LF + //
                "  }" + LF + //
                "}");
        addExpectedRun("5");
        parseAndVerify();
    }

    @Test
    public void testLocalVarsIncDec() {
        init("program LocalVars" + LF + //
                "{" + LF + //
                "  void main()" + LF + //
                "    int a;" + LF + //
                "    int b;" + LF + //
                "  {" + LF + //
                "    a = 2;" + LF + //
                "    b = 5;" + LF + //
                "    a++;" + LF + //
                "    b--;" + LF + //
                "    print(a+b);" + LF + //
                "  }" + LF + //
                "}");
        addExpectedRun("7");
        parseAndVerify();
    }

    @Test
    public void testConstDecl() {
        init("program ConstDecl" + LF + //
                " final int a = 100;" + LF + //
                " final char b = 'A';" + LF + //
                "{" + LF + //
                "  void main()" + LF + //
                "  {" + LF + //
                "    print(a);" + LF + //
                "    print(b);" + LF + //
                "  }" + LF + //
                "}");
        addExpectedRun("100A");
        parseAndVerify();
    }

    @Test
    public void testMethodAsOperand() {
        init("program ConstDecl" + LF + //
                "{" + LF + //
                "  void foo() {}" + LF + //
                "  void main()" + LF + //
                "  {" + LF + //
                "    foo++;" + LF + //
                "  }" + LF + //
                "}");
        expectError(6, 8, NO_INT);
        parseAndVerify();
    }

    @Test
    public void testTypeAsOperand() {
        init("program ConstDecl" + LF + //
                "class Foo {}" + LF + //
                "{" + LF + //
                "  void main()" + LF + //
                "  {" + LF + //
                "    Foo++;" + LF + //
                "  }" + LF + //
                "}");
        expectError(6, 8, NO_OPERAND);
        parseAndVerify();
    }
}

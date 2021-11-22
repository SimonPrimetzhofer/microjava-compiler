// MicroJava Virtual Machine
// -------------------------
// Syntax: java ssw.mj.Run fileName [-debug]
// ===========================================================================
// by Hanspeter Moessenboeck, 2002-10-28
// edited by Albrecht Woess, 2002-10-30
package ssw.mj;

import ssw.mj.codegen.Code;
import ssw.mj.codegen.Code.OpCode;

import java.io.IOException;

public class Interpreter {

    private boolean debug; // debug output on or off
    private byte code[]; // code array
    private int data[]; // global data
    private int heap[]; // dynamic heap
    private int stack[]; // expression stack
    private int local[]; // method stack
    private int startPC; // address of main() method
    private int pc; // program counter
    private int fp, sp; // frame pointer, stack pointer on method stack
    private int esp; // expression stack pointer
    private int free; // next free heap address
    private static final int heapSize = 100000, // size of the heap in words
            mStackSize = 4000, // size of the method stack in words
            eStackSize = 30; // size of the expression stack in words

    private void write(String s, int len) {
        for (int i = 0; i < len; i++) {
            io.write(' ');
        }
        for (int i = 0; i < s.length(); i++) {
            io.write(s.charAt(i));
        }
    }

    public static class BufferIO implements IO {

        private StringBuffer output;
        private String input;

        private int inputPos;

        public BufferIO(String input) {
            output = new StringBuffer();
            this.input = input;
        }

        @Override
        public char read() {
            if (inputPos >= input.length()) {
                return 0;
            }

            return input.charAt(inputPos++);
        }

        @Override
        public void write(char c) {
            output.append(c);
        }

        public String getOutput() {
            return output.toString();
        }
    }

    public static final IO ConsoleIO = new IO() {

        @Override
        public char read() {
            try {
                int i = System.in.read();
                if (i == -1) {
                    return 0;
                }
                return (char) i;
            } catch (IOException ex) {
                return 0;
            }
        }

        @Override
        public void write(char c) {
            System.out.print(c);
        }
    };

    public interface IO {
        char read();

        void write(char c);
    }

    private IO io;

    public Interpreter(byte[] code, int startPC, int dataSize, IO io, boolean debug) {
        this.code = code;
        this.startPC = startPC;
        this.io = io;
        this.debug = debug;
        heap = new int[heapSize]; // fixed sized heap
        data = new int[dataSize]; // global data as specified in
        // classfile
        stack = new int[eStackSize]; // expression stack
        local = new int[mStackSize]; // method stack
        fp = 0;
        sp = 0;
        esp = 0;
        free = 1; // no block should start at address 0
    }

    // ----- expression stack
    private void push(int val) throws IllegalStateException {
        if (esp == eStackSize) {
            throw new IllegalStateException("expression stack overflow");
        }
        stack[esp++] = val;
    }

    private int pop() throws IllegalStateException {
        if (esp == 0) {
            throw new IllegalStateException("expression stack underflow");
        }
        return stack[--esp];
    }

    // ----- method stack
    private void PUSH(int val) throws IllegalStateException {
        if (sp == mStackSize) {
            throw new IllegalStateException("method stack overflow");
        }
        local[sp++] = val;
    }

    private int POP() throws IllegalStateException {
        if (sp == 0) {
            throw new IllegalStateException("method stack underflow");
        }
        return local[--sp];
    }

    // ----- instruction fetch
    private byte next(boolean dbgPrint) {
        byte b = code[pc++];
        if (debug && dbgPrint) {
            System.out.print(b + " ");
        }
        return b;
    }

    private short next2(boolean dbgPrint) {
        short s = (short) (((next(false) << 8)
                + (next(false) & 0xff)) << 16 >> 16);
        if (debug && dbgPrint) {
            System.out.print(s + " ");
        }
        return s;
    }

    private int next4() {
        return next4(true);
    }

    private int next4(boolean dbgPrint) {
        int n = (next2(false) << 16) + (next2(false) & 0xffff);
        if (debug && dbgPrint) {
            System.out.print(n + " ");
        }
        return n;
    }

    /**
     * Allocate heap block of size bytes
     */
    private int alloc(int size) throws IllegalStateException {
        int adr = free;
        free += ((size + 3) >> 2); // skip to next free adr
        // (>> 2 to convert byte to word)
        if (free > heapSize) {
            throw new IllegalStateException("heap overflow");
        }
        return adr;
    }

    /**
     * Retrieve byte n from val. Byte 0 is MSB
     */
    private static byte getByte(int val, int n) {
        return (byte) (val << (8 * n) >>> 24);
    }

    /**
     * Replace byte n in val by b
     */
    private static int setByte(int val, int n, byte b) {
        int delta = (3 - n) * 8;
        int mask = ~(255 << delta); // mask all 1 except on chosen byte
        int by = (b & 255) << delta;
        return (val & mask) ^ by;
    }

    /**
     * Read int from standard input stream
     */
    private int readInt() {
        int val = 0;
        int prev = ' ';
        int b = io.read();
        while (b < '0' || b > '9') {
            prev = b;
            b = io.read();
        }
        while (b >= '0' && b <= '9') {
            val = 10 * val + b - '0';
            b = io.read();
        }
        if (prev == '-') {
            val = -val;
        }
        return val;
    }

    private void printInstr() {
        int op = code[pc - 1];
        OpCode opCode = Code.OpCode.get(op);
        String instr = (opCode != null) ? opCode.cleanName() : "???";
        System.out.printf("%5d: %s ", pc - 1, instr);
    }

    private void printStack() {
        for (int i = 0; i < esp; i++) {
            System.out.print(stack[i] + " ");
        }
        System.out.println();
    }

    // ----- actual interpretation
    public void run() throws IllegalStateException {
        Code.OpCode op;
        int adr, val, val2, off, idx, len, i;
        pc = startPC;

        if (debug) { // header for debug output
            System.out.println();
            System.out.println("  pos: instruction operands");
            System.out.println("     | expressionstack");
            System.out.println("-----------------------------");
        }

        for (; ; ) { // terminated by return instruction
            op = Code.OpCode.get(next(false));
            if (debug) {
                printInstr();
            }

            switch (op) {

                // load/store local variables
                case load:
                    push(local[fp + next(true)]);
                    break;
                case load_0:
                case load_1:
                case load_2:
                case load_3:
                    push(local[fp + op.code() - Code.OpCode.load_0.code()]); // mapping
                    // on
                    // range
                    // 0..3
                    break;
                case store:
                    local[fp + next(true)] = pop();
                    break;
                case store_0:
                case store_1:
                case store_2:
                case store_3:
                    local[fp + op.code() - Code.OpCode.store_0.code()] = pop(); // mapping
                    // on
                    // range
                    // 0..3
                    break;

                // load/store global variables
                case getstatic:
                    push(data[next2(true)]);
                    break;
                case putstatic:
                    data[next2(true)] = pop();
                    break;

                // load/store object fields
                case getfield:
                    adr = pop();
                    if (adr == 0) {
                        throw new IllegalStateException("null reference used");
                    }
                    push(heap[adr + next2(true)]);
                    break;
                case putfield:
                    val = pop();
                    adr = pop();
                    if (adr == 0) {
                        throw new IllegalStateException("null reference used");
                    }
                    heap[adr + next2(true)] = val;
                    break;

                // load constants
                case const_0:
                case const_1:
                case const_2:
                case const_3:
                case const_4:
                case const_5:
                    push(op.code() - Code.OpCode.const_0.code()); // map opcode to
                    // 0..5
                    break;
                case const_m1:
                    push(-1);
                    break;
                case const_:
                    push(next4());
                    break;

                // arithmetic operations
                case add:
                    push(pop() + pop());
                    break;
                case sub:
                    push(-pop() + pop());
                    break;
                case mul:
                    push(pop() * pop());
                    break;
                case div:
                    val = pop();
                    if (val == 0) {
                        throw new IllegalStateException("division by zero");
                    }
                    push(pop() / val);
                    break;
                case rem:
                    val = pop();
                    if (val == 0) {
                        throw new IllegalStateException("division by zero");
                    }
                    push(pop() % val);
                    break;
                case neg:
                    push(-pop());
                    break;
                case shl:
                    val = pop();
                    push(pop() << val);
                    break;
                case shr:
                    val = pop();
                    push(pop() >> val);
                    break;
                case inc:
                    off = fp + next(true);
                    local[off] += next(true);
                    break;

                // object creation
                case new_:
                    push(alloc(next2(true) * 4));
                    break;
                case newarray:
                    val = next(true);
                    len = pop();
                    if (val == 0) {
                        adr = alloc(len + 4);
                    } else {
                        adr = alloc(len * 4 + 4);
                    }
                    heap[adr] = len;
                    push(adr + 1); // skip length field of array
                    break;

                // array access
                case aload:
                    idx = pop();
                    adr = pop();
                    if (adr == 0) {
                        throw new IllegalStateException("null reference used");
                    }
                    len = heap[adr - 1];
                    if (idx < 0 || idx >= len) {
                        throw new IllegalStateException("index out of bounds");
                    }
                    push(heap[adr + idx]);
                    break;
                case astore:
                    val = pop();
                    idx = pop();
                    adr = pop();
                    if (adr == 0) {
                        throw new IllegalStateException("null reference used");
                    }
                    len = heap[adr - 1];
                    if (debug) {
                        System.out.println("\nArraylength = " + len);
                        System.out.println("Address = " + adr);
                        System.out.println("Index = " + idx);
                        System.out.println("Value = " + val);
                    }
                    if (idx < 0 || idx >= len) {
                        throw new IllegalStateException("index out of bounds");
                    }
                    heap[adr + idx] = val;
                    break;
                case baload:
                    idx = pop();
                    adr = pop();
                    if (adr == 0) {
                        throw new IllegalStateException("null reference used");
                    }
                    len = heap[adr - 1];
                    if (idx < 0 || idx >= len) {
                        throw new IllegalStateException("index out of bounds");
                    }
                    push(getByte(heap[adr + idx / 4], idx % 4));
                    break;
                case bastore:
                    val = pop();
                    idx = pop();
                    adr = pop();
                    if (adr == 0) {
                        throw new IllegalStateException("null reference used");
                    }
                    len = heap[adr - 1];
                    if (idx < 0 || idx >= len) {
                        throw new IllegalStateException("index out of bounds");
                    }
                    heap[adr + idx / 4] = setByte(heap[adr + idx / 4], idx % 4,
                            (byte) val);
                    break;
                case arraylength:
                    adr = pop();
                    if (adr == 0) {
                        throw new IllegalStateException("null reference used");
                    }
                    push(heap[adr - 1]);
                    break;

                // stack manipulation
                case pop:
                    pop();
                    break;
                case dup:
                    val = pop();
                    push(val);
                    push(val);
                    break;
                case dup2:
                    val = pop();
                    val2 = pop();
                    push(val2);
                    push(val);
                    push(val2);
                    push(val);
                    break;

                // jumps
                case jmp:
                    off = next2(true);
                    pc += off - 3;
                    break;

                case jeq:
                case jne:
                case jlt:
                case jle:
                case jgt:
                case jge:
                    off = next2(true);
                    val2 = pop();
                    val = pop();
                    boolean cond = false;
                    switch (op) {
                        case jeq:
                            cond = val == val2;
                            break;
                        case jne:
                            cond = val != val2;
                            break;
                        case jlt:
                            cond = val < val2;
                            break;
                        case jle:
                            cond = val <= val2;
                            break;
                        case jgt:
                            cond = val > val2;
                            break;
                        case jge:
                            cond = val >= val2;
                            break;
                        default:
                            assert false;
                    }
                    if (cond) {
                        pc += off - 3;
                    }
                    break;

                // method calls
                case call:
                    off = next2(true);
                    PUSH(pc);
                    pc += off - 3;
                    break;

                case return_:
                    if (sp == 0) {
                        return;
                    }
                    pc = POP();
                    break;
                case enter:
                    int psize = next(true);
                    int lsize = next(true);
                    PUSH(fp);
                    fp = sp;
                    for (i = 0; i < lsize; i++) {
                        PUSH(0);
                    }
                    assert sp == (fp + lsize);
                    for (i = psize - 1; i >= 0; i--) {
                        local[fp + i] = pop();
                    }
                    break;
                case exit:
                    sp = fp;
                    fp = POP();
                    break;

                // I/O
                case read:
                    push(readInt());
                    break;

                case print:
                    len = pop();
                    val = pop();
                    String s = new Integer(val).toString();
                    len = len - s.length();
                    write(s, len);
                    break;
                case bread:
                    push(io.read());
                    break;
                case bprint:
                    len = pop() - 1;
                    val = pop();
                    write(Character.toString((char) val), len);
                    break;
                case nop:
                    // nothing to do
                    break;
                case trap:
                    throw new IllegalStateException("trap(" + next(true) + ")");
                default:
                    throw new IllegalStateException("wrong opcode " + op);
            }
            if (debug) {
                System.out.println();
                System.out.print("     | ");
                printStack();
            }
        }
    }
}

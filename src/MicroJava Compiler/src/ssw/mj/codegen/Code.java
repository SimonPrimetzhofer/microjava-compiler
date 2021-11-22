package ssw.mj.codegen;

import ssw.mj.Parser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;

/**
 * MicroJava Code Generator.
 */
public abstract class Code {
    private static enum Operands {
        B(1), // byte ( 8 bit signed)
        S(2), // short (16 bit signed)
        W(4); // word (32 bit signed)

        /**
         * Size in bytes (8 bit)
         */
        public final int size;

        Operands(int size) {
            this.size = size;
        }
    }

    private static final Operands[] B = new Operands[]{Operands.B};
    private static final Operands[] S = new Operands[]{Operands.S};
    private static final Operands[] W = new Operands[]{Operands.W};
    private static final Operands[] BB = new Operands[]{Operands.B,
            Operands.B};

    public static enum OpCode {
        load(B), //
        load_0, //
        load_1, //
        load_2, //
        load_3, //
        store(B), //
        store_0, //
        store_1, //
        store_2, //
        store_3, //
        getstatic(S), //
        putstatic(S), //
        getfield(S), //
        putfield(S), //
        const_0, //
        const_1, //
        const_2, //
        const_3, //
        const_4, //
        const_5, //
        const_m1, //
        const_(W), //
        add, //
        sub, //
        mul, //
        div, //
        rem, //
        neg, //
        shl, //
        shr, //
        inc(BB), //
        new_(S), //
        newarray(B), //
        aload, //
        astore, //
        baload, //
        bastore, //
        arraylength, //
        pop, //
        dup, //
        dup2, //
        jmp(S), //
        jeq(S), //
        jne(S), //
        jlt(S), //
        jle(S), //
        jgt(S), //
        jge(S), //
        call(S), //
        return_, //
        enter(BB), //
        exit, //
        read, //
        print, //
        bread, //
        bprint, //
        trap(B), //
        nop;

        private final Operands[] ops;

        private OpCode(Operands... operands) {
            this.ops = operands;
        }

        protected Collection<Operands> getOps() {
            return Arrays.asList(ops);
        }

        public int numOps() {
            return ops.length;
        }

        public int getOpsSize() {
            int size = 0;
            for (Operands op : ops) {
                size += op.size;
            }
            return size;
        }

        public int code() {
            return ordinal() + 1;
        }

        public String cleanName() {
            String name = name();
            if (name.endsWith("_")) {
                name = name.substring(0, name.length() - 1);
            }
            return name;
        }

        public static OpCode get(int code) {
            if (code < 1 || code > values().length) {
                return null;
            }
            return values()[code - 1];
        }
    }

    public enum CompOp {
        eq, ne, lt, le, gt, ge;

        public static CompOp invert(CompOp op) {
            switch (op) {
                case eq:
                    return ne;
                case ne:
                    return eq;
                case lt:
                    return ge;
                case le:
                    return gt;
                case gt:
                    return le;
                case ge:
                    return lt;
            }
            throw new IllegalArgumentException("Unexpected compare operator");
        }
    }

    /**
     * Code buffer
     */
    public byte[] buf;

    /**
     * Program counter. Indicates next free byte in code buffer.
     */
    public int pc;

    /**
     * PC of main method (set by parser).
     */
    public int mainpc;

    /**
     * Length of static data in words (set by parser).
     */
    public int dataSize;

    /**
     * According parser.
     */
    protected Parser parser;

    // ----- initialization

    public Code(Parser p) {
        parser = p;
        buf = new byte[100];
        pc = 0;
        mainpc = -1;
        dataSize = 0;
    }

    // ----- code storage management

    public void put(OpCode code) {
        put(code.code());
    }

    public void put(int x) {
        if (pc == buf.length) {
            buf = Arrays.copyOf(buf, buf.length * 2);
        }
        buf[pc++] = (byte) x;
    }

    public void put2(int x) {
        put(x >> 8);
        put(x);
    }

    public void put4(int x) {
        put2(x >> 16);
        put2(x);
    }

    public void put2(int pos, int x) {
        int oldpc = pc;
        pc = pos;
        put2(x);
        pc = oldpc;
    }

    public int get(int pos) {
        return buf[pos];
    }

    public int get2(int pos) {
        return (get(pos) << 8) + (get(pos + 1) & 0xFF);
    }

    /**
     * Write the code buffer to the output stream.
     */
    public void write(OutputStream os) throws IOException {
        int codeSize = pc;
        // uncomment for debugging output
        // Decoder.decode(buf, 0, codeSize);

        ByteArrayOutputStream header = new ByteArrayOutputStream();
        DataOutputStream headerWriter = new DataOutputStream(header);
        headerWriter.writeByte('M');
        headerWriter.writeByte('J');
        headerWriter.writeInt(codeSize);
        headerWriter.writeInt(dataSize);
        headerWriter.writeInt(mainpc);
        headerWriter.close();

        os.write(header.toByteArray());

        os.write(buf, 0, codeSize);
        os.flush();
        os.close();
    }

    /**
     * String representation for JUnit test cases.
     */
    public String dump() {
        StringBuilder sb = new StringBuilder();
        Decoder dec = new Decoder();
        sb.append(dec.decode(buf, 0, pc));
        sb.append("\n#CodeSize: ");
        sb.append(pc);
        sb.append("\n#DataSize: ");
        sb.append(dataSize);
        sb.append("\n#MainPC: ");
        sb.append(mainpc);
        return sb.toString();
    }
}

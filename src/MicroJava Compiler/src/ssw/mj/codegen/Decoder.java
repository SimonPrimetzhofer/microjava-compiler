package ssw.mj.codegen;

import ssw.mj.codegen.Code.OpCode;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/* MicroJava Instruction Decoder
 =============================
 */
public class Decoder {

    private byte[] codeBuf; // code buffer
    private int cur; // address of next byte to decode
    private int adr; // address of currently decoded instruction

    private int get() {
        return codeBuf[cur++];
    }

    private int get2() {
        return (get() << 8) + (get() & 0xFF);
    }

    private int get4() {
        return (get2() << 16) + (get2() & 0xFFFF);
    }

    private String jumpDist() {
        int dist = get2();
        int pos = adr + dist;
        return String.valueOf(dist) + " (=" + String.valueOf(pos) + ")";
    }

    public String decode(byte[] buf, int off, int len) {
        StringBuilder sb = new StringBuilder();
        codeBuf = buf;
        cur = off;
        adr = cur;
        while (cur < len) {
            sb.append(adr);
            sb.append(": ");
            sb.append(decode(OpCode.get(get())));
            sb.append("\n");
            adr = cur;
        }
        return sb.toString();
    }

    private String decode(OpCode opCode) {
        if (opCode == null) {
            return "--error, unknown opcode--";
        }

        String s = "";
        switch (opCode) {
            // Operations without parameters in the code buffer
            case load_0:
            case load_1:
            case load_2:
            case load_3:
            case store_0:
            case store_1:
            case store_2:
            case store_3:
            case const_0:
            case const_1:
            case const_2:
            case const_3:
            case const_4:
            case const_5:
            case const_m1:
            case add:
            case sub:
            case mul:
            case div:
            case rem:
            case neg:
            case shl:
            case shr:
            case aload:
            case astore:
            case baload:
            case bastore:
            case arraylength:
            case pop:
            case dup:
            case dup2:
            case exit:
            case return_:
            case read:
            case print:
            case bread:
            case bprint:
                s = opCode.cleanName();
                break;
            // Operations with one 1 byte parameter in the code buffer
            case load:
            case store:
            case newarray:
            case trap:
                s = opCode.cleanName() + " " + get();
                break;
            // Operations with one 2 byte parameter in the code buffer
            case getstatic:
            case putstatic:
            case getfield:
            case putfield:
            case new_:
                s = opCode.cleanName() + " " + get2();
                break;
            // Operations with one 4 byte parameter in the code buffer
            case const_:
                s = opCode.cleanName() + " " + get4();
                break;
            // Operations with two 1 byte parameters in the code buffer
            case inc:
            case enter:
                s = opCode.cleanName() + " " + get() + ", " + get();
                break;
            // Operations with a jump distance as a parameter in the code buffer
            case jmp:
            case jeq:
            case jne:
            case jlt:
            case jle:
            case jgt:
            case jge:
            case call:
                s = opCode.cleanName() + " " + jumpDist();
                break;
            default:
                s = "--error--";
                break;
        }
        return s;
    }

    public void decodeFile(String filename) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
        byte[] sig = new byte[2];
        in.read(sig, 0, 2);
        System.out.println("" + (char) sig[0] + (char) sig[1]);
        int codeSize = in.readInt();
        System.out.println("codesize = " + codeSize);
        System.out.println("datasize = " + in.readInt());
        System.out.println("startPC  = " + in.readInt());
        byte[] code = new byte[codeSize];
        in.read(code);
        System.out.println(decode(code, 0, codeSize));
        in.close();
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            Decoder dec = new Decoder();
            dec.decodeFile(args[0]);
        }
    }
}

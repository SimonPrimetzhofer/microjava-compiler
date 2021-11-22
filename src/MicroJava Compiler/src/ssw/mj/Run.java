// MicroJava Virtual Machine
// -------------------------
// Syntax: java ssw.mj.Run fileName [-debug]
// ===========================================================================
// by Hanspeter Moessenboeck, 2002-10-28
// edited by Albrecht Woess, 2002-10-30
package ssw.mj;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Run {

    // ----- VM internals
    static Interpreter load(String name, boolean debug) throws IOException {
        int codeSize;
        byte[] sig = new byte[2];
        DataInputStream in = new DataInputStream(new FileInputStream(name));
        in.read(sig, 0, 2);
        if (sig[0] != 'M' || sig[1] != 'J') {
            in.close();
            throw new FormatException("wrong marker");
        }
        codeSize = in.readInt();
        if (codeSize <= 0) {
            in.close();
            throw new FormatException("codeSize <= 0");
        }
        int dataSize = in.readInt();
        if (dataSize < 0) {
            in.close();
            throw new FormatException("dataSize < 0");
        }
        int startPC = in.readInt();
        if (startPC < 0 || startPC >= codeSize) {
            in.close();
            throw new FormatException("startPC not in code area");
        }
        byte[] code = new byte[codeSize];
        in.read(code, 0, codeSize);
        in.close();

        return new Interpreter(code, startPC, dataSize, Interpreter.ConsoleIO, debug);
    }

    public static void main(String[] arg) {
        String fileName = null;
        boolean debug = false;
        for (int i = 0; i < arg.length; i++) {
            if (arg[i].equals("-debug")) {
                debug = true;
            } else {
                fileName = arg[i];
            }
        }
        if (fileName == null) {
            System.out.println("Syntax: java ssw.mj.Run filename [-debug]");
            return;
        }
        try {
            Interpreter r = load(fileName, debug);

            long startTime = System.currentTimeMillis();
            r.run();

            System.out.print("\nCompletion took " + (System.currentTimeMillis() - startTime) + " ms");
        } catch (FileNotFoundException e) {
            System.out.println("-- file " + fileName + " not found");
        } catch (FormatException e) {
            System.out.println("-- corrupted object file " + fileName + ": " + e.getMessage());
        } catch (IOException e) {
            System.out.println("-- error reading file " + fileName);
        }
    }
}

class FormatException extends IOException {

    private static final long serialVersionUID = 1L;

    FormatException(String s) {
        super(s);
    }
}
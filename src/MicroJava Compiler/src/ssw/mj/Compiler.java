package ssw.mj;

import ssw.mj.impl.ParserImpl;
import ssw.mj.impl.ScannerImpl;

import java.io.*;

/**
 * <code>Compiler</code> is the driver for the MicroJava-Compiler.
 * <p>
 * Execute<br>
 * <code>java ssw.mj.Compiler &lt;<i>MJ-Source-Filename</i>&gt;</code><br>
 * to start compilation.
 */
public class Compiler {

    private static String objFileName(String s) {
        int i = s.lastIndexOf('.');
        if (i < 0) {
            return s + ".obj";
        }
        return s.substring(0, i) + ".obj";
    }

    public static void main(String[] args) {
        // --- get the filename
        if (args.length != 1) {
            System.out.println("usage: java Compiler filename.mj");
            return;
        }
        String inFilename = args[0];
        String outFilename = objFileName(inFilename);

        try {
            ScannerImpl scanner = new ScannerImpl(new BufferedReader(new FileReader(inFilename)));

            System.out.println("-----------------------------------");
            System.out.println("Parsing file " + inFilename);

            ParserImpl parser = new ParserImpl(scanner);
            parser.parse();
            if (scanner.errors.numErrors() == 0) {
                parser.code.write(new BufferedOutputStream(new FileOutputStream(outFilename)));
            }

            if (scanner.errors.numErrors() > 0) {
                System.out.println(scanner.errors.dump());
                System.out.println(scanner.errors.numErrors() + " errors.");
            } else {
                System.out.println("No errors.");
            }
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }
    }
}

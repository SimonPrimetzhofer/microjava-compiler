package ssw.mj;

import ssw.mj.Errors.Message;
import ssw.mj.impl.CodeImpl;
import ssw.mj.impl.TabImpl;

import static ssw.mj.Token.Kind.none;

/**
 * The Parser for the MicroJava Compiler.
 */
public abstract class Parser {

    /**
     * Maximum number of global variables per program
     */
    protected static final int MAX_GLOBALS = 32767;

    /**
     * Maximum number of fields per class
     */
    protected static final int MAX_FIELDS = 32767;

    /**
     * Maximum number of local variables per method
     */
    protected static final int MAX_LOCALS = 127;

    /**
     * Last recognized token;
     */
    protected Token t;

    /**
     * Lookahead token (not recognized).)
     */
    protected Token la;

    /**
     * Shortcut to kind attribute of lookahead token (la).
     */
    protected Token.Kind sym;

    /**
     * According scanner
     */
    public final Scanner scanner;

    /**
     * According code buffer
     */
    public final CodeImpl code;

    /**
     * According symbol table
     */
    public final TabImpl tab;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        tab = new TabImpl(this);
        code = new CodeImpl(this);
        // Avoid crash when 1st symbol has scanner error.
        la = new Token(none, 1, 1);
    }

    /**
     * Adds error message to the list of errors.
     */
    public void error(Message msg, Object... msgParams) {
        scanner.errors.error(la.line, la.col, msg, msgParams);
        // panic mode
        throw new Errors.PanicMode();
    }

    /**
     * Starts the analysis.
     */
    public abstract void parse();
}

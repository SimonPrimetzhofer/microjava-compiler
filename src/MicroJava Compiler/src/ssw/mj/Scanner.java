package ssw.mj;

import ssw.mj.Errors.Message;

import java.io.Reader;

/**
 * The Scanner for the MicroJava Compiler.
 */
public abstract class Scanner {
    protected static final char EOF = (char) -1;
    protected static final char LF = '\n';

    /**
     * Input data to read from.
     */
    protected Reader in;

    /**
     * Lookahead character. (= next (unhandled) character in the input stream)
     */
    protected char ch;

    /**
     * Current line in input stream.
     */
    protected int line;

    /**
     * Current column in input stream.
     */
    protected int col;

    /**
     * According errors object.
     */
    public final Errors errors;

    public Scanner(Reader r) {
        // store reader
        in = r;

        // initialize error handling support
        errors = new Errors();
    }

    /**
     * Adds error message to the list of errors.
     */
    protected final void error(Token t, Message msg, Object... msgParams) {
        errors.error(t.line, t.col, msg, msgParams);

        // reset token content (consistent JUnit tests)
        t.val = 0;
        t.str = null;
    }

    /**
     * Returns next token. To be used by parser.
     */
    public Token next() {
        return new Token(Token.Kind.eof, 1, 1);
    }
}

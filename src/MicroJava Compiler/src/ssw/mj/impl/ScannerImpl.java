package ssw.mj.impl;

import ssw.mj.Errors;
import ssw.mj.Scanner;
import ssw.mj.Token;
import ssw.mj.Token.Kind;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

public final class ScannerImpl extends Scanner {

    private final HashMap<String, Kind> keywords;

    public ScannerImpl(Reader r) {
        super(r);
        init(r);

        keywords = new HashMap<>();
        keywords.put("program", Kind.program);
        keywords.put("class", Kind.class_);
        keywords.put("if", Kind.if_);
        keywords.put("else", Kind.else_);
        keywords.put("while", Kind.while_);
        keywords.put("read", Kind.read);
        keywords.put("print", Kind.print);
        keywords.put("return", Kind.return_);
        keywords.put("break", Kind.break_);
        keywords.put("void", Kind.void_);
        keywords.put("final", Kind.final_);
        keywords.put("new", Kind.new_);

    }

    private void init(Reader r) {
        in = r;
        line = 1;
        col = 0;
        nextCh();
    }

    /**
     * Returns next token. To be used by parser.
     */
    @Override
    public Token next() {
        while (Character.isWhitespace(ch)) nextCh();

        Token t = new Token(Kind.none, line, col);

        switch (ch) {
            // letters
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
                readName(t);
                break;
            // numbers
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                readNumber(t);
                break;
            // simple tokens
            case ';':
                t.kind = Kind.semicolon;
                nextCh();
                break;
            case ',':
                t.kind = Kind.comma;
                nextCh();
                break;
            case '\'':
                readCharConst(t);
                break;
            case '(':
                t.kind = Kind.lpar;
                nextCh();
                break;
            case ')':
                t.kind = Kind.rpar;
                nextCh();
                break;
            case '[':
                t.kind = Kind.lbrack;
                nextCh();
                break;
            case ']':
                t.kind = Kind.rbrack;
                nextCh();
                break;
            case '{':
                t.kind = Kind.lbrace;
                nextCh();
                break;
            case '}':
                t.kind = Kind.rbrace;
                nextCh();
                break;
            case EOF:
                t.kind = Kind.eof;
                break;
            // compound tokens
            case '+':
                nextCh();
                if (ch == '=') {
                    t.kind = Kind.plusas;
                    nextCh();
                } else if (ch == '+') {
                    t.kind = Kind.pplus;
                    nextCh();
                } else {
                    t.kind = Kind.plus;
                }
                break;
            case '-':
                nextCh();
                if (ch == '=') {
                    t.kind = Kind.minusas;
                    nextCh();
                } else if (ch == '-') {
                    t.kind = Kind.mminus;
                    nextCh();
                } else {
                    t.kind = Kind.minus;
                }
                break;
            case '*':
                nextCh();
                if (ch == '=') {
                    t.kind = Kind.timesas;
                    nextCh();
                } else {
                    t.kind = Kind.times;
                }
                break;
            case '/':
                nextCh();
                if (ch == '=') {
                    t.kind = Kind.slashas;
                    nextCh();
                } else if (ch == '*') {
                    skipComment(t);
                    t = next();
                } else {
                    t.kind = Kind.slash;
                }
                break;
            case '%':
                nextCh();
                if (ch == '=') {
                    t.kind = Kind.remas;
                    nextCh();
                } else {
                    t.kind = Kind.rem;
                }
                break;
            case '=':
                nextCh();
                if (ch == '=') {
                    t.kind = Kind.eql;
                    nextCh();
                } else {
                    t.kind = Kind.assign;
                }
                break;
            case '!':
                nextCh();
                if (ch == '=') {
                    t.kind = Kind.neq;
                    nextCh();
                } else {
                    error(t, Errors.Message.INVALID_CHAR, '!');
                }
                break;
            case '>':
                nextCh();
                if (ch == '=') {
                    t.kind = Kind.geq;
                    nextCh();
                } else {
                    t.kind = Kind.gtr;
                }
                break;
            case '<':
                nextCh();
                if (ch == '=') {
                    t.kind = Kind.leq;
                    nextCh();
                } else {
                    t.kind = Kind.lss;
                }
                break;
            case '&':
                nextCh();
                if (ch == '&') {
                    t.kind = Kind.and;
                    nextCh();
                } else {
                    error(t, Errors.Message.INVALID_CHAR, '&');
                }
                break;
            case '|':
                nextCh();
                if (ch == '|') {
                    t.kind = Kind.or;
                    nextCh();
                } else {
                    error(t, Errors.Message.INVALID_CHAR, '|');
                }
                break;
            case '.':
                nextCh();
                if (ch == '.') {
                    nextCh();
                    if (ch == '.') {
                        t.kind = Kind.ppperiod;
                        nextCh();
                    } else {
                        t.kind = Kind.pperiod;
                    }
                } else {
                    t.kind = Kind.period;
                }
                break;
            case '#':
                t.kind = Kind.hash;
                nextCh();
                break;
            default:
                error(t, Errors.Message.INVALID_CHAR, ch);
                nextCh();
                break;
        }
        return t;
    }

    private void readName(final Token t) {
        final StringBuilder str = new StringBuilder();

        // read only letters, digits or underscores
        // check by ascii-code to only allow a-z and A-Z regarding letters
        while (Character.isDigit(ch) || (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || ch == '_') {
            str.append(ch);
            nextCh();
        }

        // result must be stored in str
        t.str = str.toString();

        // check if we have a keyword
        t.kind = keywords.getOrDefault(t.str, Kind.ident);
    }

    private void readNumber(final Token t) {
        final StringBuilder str = new StringBuilder();

        while (Character.isDigit(ch)) {
            str.append(ch);
            nextCh();
        }

        try {
            t.val = Integer.parseInt(str.toString());
        } catch (NumberFormatException ex) {
            error(t, Errors.Message.BIG_NUM, str.toString());
        }
        t.kind = Kind.number;
    }

    private void readCharConst(final Token t) {
        char convertedChar = 0;

        t.kind = Kind.charConst;

        // get next letter
        nextCh();

        // check if char is empty
        if (ch == '\'') {
            nextCh();
            error(t, Errors.Message.EMPTY_CHARCONST);
            return;
        } else if (ch == LF || ch == '\r') {
            error(t, Errors.Message.ILLEGAL_LINE_END);
            return;
        } else if (ch == EOF) {
            error(t, Errors.Message.EOF_IN_CHAR);
            return;
        } else if (ch == '\\') {
            nextCh();
            switch (ch) {
                case 'r':
                    convertedChar = '\r';
                    break;
                case 'n':
                    convertedChar = '\n';
                    break;
                case '\'':
                    convertedChar = '\'';
                    break;
                case '\\':
                    convertedChar = '\\';
                    break;
                default:
                    error(t, Errors.Message.UNDEFINED_ESCAPE, ch);
            }
        } else {
            convertedChar = ch;
        }

        nextCh();

        if (ch != '\'') {
            error(t, Errors.Message.MISSING_QUOTE);
        } else {
            t.val = convertedChar;

            nextCh();
        }
    }

    private void skipComment(final Token t) {
        nextCh();
        int depth = 1;
        while (depth > 0) {
            // check for nested comment
            if (ch == '/') {
                nextCh();
                if (ch == '*') {
                    // increase comment depth
                    depth++;
                    nextCh();
                }
            } else if (ch == '*') { // check for closing of a comment
                nextCh();
                if (ch == '/') {
                    // decrease comment depth
                    depth--;
                    nextCh();
                }
            } else if (ch == EOF) {
                // EOF inside comment
                error(t, Errors.Message.EOF_IN_COMMENT);
                break;
            } else {
                nextCh();
            }

        }

    }

    private void nextCh() {
        try {
            ch = (char) in.read();
            col++;
            if (ch == LF) {
                line++;
                col = 0;
            }
        } catch (IOException ex) {
            ch = EOF;
        }
    }
}

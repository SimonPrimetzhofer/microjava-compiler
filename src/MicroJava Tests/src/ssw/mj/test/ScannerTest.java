package ssw.mj.test;

import static ssw.mj.Errors.Message.*;
import static ssw.mj.Token.Kind.*;

import org.junit.Test;

/**
 * Test cases for the <code>Scanner</code> class.
 */
public class ScannerTest extends CompilerTestCaseSupport {
	private static final char invalidChar = (char) 65533;

	@Test
	public void oneToken() {
		initScanner(";");

		expectToken(semicolon, 1, 1);
		expectToken(eof, 1, 2);

		scanAndVerify();
	}

	@Test
	public void twoTokens() {
		initScanner(";;");

		expectToken(semicolon, 1, 1);
		expectToken(semicolon, 1, 2);
		expectToken(eof, 1, 3);

		scanAndVerify();
	}

	@Test
	public void space() {
		initScanner(";  ;");

		expectToken(semicolon, 1, 1);
		expectToken(semicolon, 1, 4);
		expectToken(eof, 1, 5);

		scanAndVerify();
	}

	@Test
	public void tabulator() {
		initScanner(";\t\t;");

		expectToken(semicolon, 1, 1);
		expectToken(semicolon, 1, 4);
		expectToken(eof, 1, 5);

		scanAndVerify();
	}

	@Test
	public void noToken() {
		initScanner("");

		expectToken(eof, 1, 1);

		scanAndVerify();
	}

	@Test
	public void crLfLineSeparators() {
		initScanner(";" + CR + LF + " ;" + CR + LF + "  ; ");

		expectToken(semicolon, 1, 1);
		expectToken(semicolon, 2, 2);
		expectToken(semicolon, 3, 3);
		expectToken(eof, 3, 5);

		scanAndVerify();
	}

	@Test
	public void lFLineSeparators() {
		initScanner(";" + LF + " ;" + LF + "  ; ");

		expectToken(semicolon, 1, 1);
		expectToken(semicolon, 2, 2);
		expectToken(semicolon, 3, 3);
		expectToken(eof, 3, 5);

		scanAndVerify();
	}

	@Test
	public void invalidChar1() {
		initScanner(" {" + invalidChar + "} ");

		expectToken(lbrace, 1, 2);
		expectToken(none, 1, 3);
		expectError(1, 3, INVALID_CHAR, invalidChar);
		expectToken(rbrace, 1, 4);
		expectToken(eof, 1, 6);

		scanAndVerify();
	}

	@Test
	public void invalidChar2() {
		initScanner(" {\0} ");

		expectToken(lbrace, 1, 2);
		expectToken(none, 1, 3);
		expectError(1, 3, INVALID_CHAR, '\0');
		expectToken(rbrace, 1, 4);
		expectToken(eof, 1, 6);

		scanAndVerify();
	}

	@Test
	public void invalidChar3() {
		initScanner(" {&} ");

		expectToken(lbrace, 1, 2);
		expectToken(none, 1, 3);
		expectError(1, 3, INVALID_CHAR, '&');
		expectToken(rbrace, 1, 4);
		expectToken(eof, 1, 6);

		scanAndVerify();
	}

	@Test
	public void invalidChar4() {
		initScanner(" {|} ");

		expectToken(lbrace, 1, 2);
		expectToken(none, 1, 3);
		expectError(1, 3, INVALID_CHAR, '|');
		expectToken(rbrace, 1, 4);
		expectToken(eof, 1, 6);

		scanAndVerify();
	}

	@Test
	public void invalidChar5() {
		initScanner(" {!} ");

		expectToken(lbrace, 1, 2);
		expectToken(none, 1, 3);
		expectError(1, 3, INVALID_CHAR, '!');
		expectToken(rbrace, 1, 4);
		expectToken(eof, 1, 6);

		scanAndVerify();
	}

	@Test
	public void invalidChar6() {
		initScanner(" {ident" + invalidChar + "} ");

		expectToken(lbrace, 1, 2);
		expectToken(ident, 1, 3, "ident");
		expectToken(none, 1, 8);
		expectError(1, 8, INVALID_CHAR, invalidChar);
		expectToken(rbrace, 1, 9);
		expectToken(eof, 1, 11);

		scanAndVerify();
	}

	@Test
	public void ident() {
		initScanner(" {i I i1 i_ i1I_i} ");

		expectToken(lbrace, 1, 2);
		expectToken(ident, 1, 3, "i");
		expectToken(ident, 1, 5, "I");
		expectToken(ident, 1, 7, "i1");
		expectToken(ident, 1, 10, "i_");
		expectToken(ident, 1, 13, "i1I_i");
		expectToken(rbrace, 1, 18);
		expectToken(eof, 1, 20);

		scanAndVerify();
	}

	@Test
	public void identSepararator() {
		initScanner(" {i[i<i0i_i>i]i} ");

		expectToken(lbrace, 1, 2);
		expectToken(ident, 1, 3, "i");
		expectToken(lbrack, 1, 4);
		expectToken(ident, 1, 5, "i");
		expectToken(lss, 1, 6);
		expectToken(ident, 1, 7, "i0i_i");
		expectToken(gtr, 1, 12);
		expectToken(ident, 1, 13, "i");
		expectToken(rbrack, 1, 14);
		expectToken(ident, 1, 15, "i");
		expectToken(rbrace, 1, 16);
		expectToken(eof, 1, 18);

		scanAndVerify();
	}

	@Test
	public void singleIdent() {
		initScanner("i");

		expectToken(ident, 1, 1, "i");
		expectToken(eof, 1, 2);

		scanAndVerify();
	}

	@Test
	public void number() {
		initScanner(" {123 2147483647} ");

		expectToken(lbrace, 1, 2);
		expectToken(number, 1, 3, 123);
		expectToken(number, 1, 7, 2147483647);
		expectToken(rbrace, 1, 17);
		expectToken(eof, 1, 19);

		scanAndVerify();
	}

	@Test
	public void singleNumber() {
		initScanner("123");

		expectToken(number, 1, 1, 123);
		expectToken(eof, 1, 4);

		scanAndVerify();
	}

	@Test
	public void negativeNumber() {
		initScanner(" {-123} ");

		expectToken(lbrace, 1, 2);
		expectToken(minus, 1, 3);
		expectToken(number, 1, 4, 123);
		expectToken(rbrace, 1, 7);
		expectToken(eof, 1, 9);

		scanAndVerify();
	}

	@Test
	public void bigNumber() {
		initScanner(" {2147483648} ");

		expectToken(lbrace, 1, 2);
		expectToken(number, 1, 3, 0);
		expectError(1, 3, BIG_NUM, "2147483648");
		expectToken(rbrace, 1, 13);
		expectToken(eof, 1, 15);

		scanAndVerify();
	}

	@Test
	public void negativeBigNumber() {
		initScanner(" {-2147483648} ");

		expectToken(lbrace, 1, 2);
		expectToken(minus, 1, 3);
		expectToken(number, 1, 4, 0);
		expectError(1, 4, BIG_NUM, "2147483648");
		expectToken(rbrace, 1, 14);
		expectToken(eof, 1, 16);

		scanAndVerify();
	}

	@Test
	public void reallyBigNumber() {
		initScanner(" {1234567890123456789012345678901234567890} ");

		expectToken(lbrace, 1, 2);
		expectToken(number, 1, 3, 0);
		expectError(1, 3, BIG_NUM, "1234567890123456789012345678901234567890");
		expectToken(rbrace, 1, 43);
		expectToken(eof, 1, 45);

		scanAndVerify();
	}

	@Test
	public void numberIdent() {
		initScanner(" {123abc123 123break} ");

		expectToken(lbrace, 1, 2);
		expectToken(number, 1, 3, 123);
		expectToken(ident, 1, 6, "abc123");
		expectToken(number, 1, 13, 123);
		expectToken(break_, 1, 16);
		expectToken(rbrace, 1, 21);
		expectToken(eof, 1, 23);

		scanAndVerify();
	}

	@Test
	public void charConst() {
		initScanner(" {' ' 'A' 'z' '0' '!' '\"' '" + invalidChar + "' '\0'} ");

		expectToken(lbrace, 1, 2);
		expectToken(charConst, 1, 3, ' ');
		expectToken(charConst, 1, 7, 'A');
		expectToken(charConst, 1, 11, 'z');
		expectToken(charConst, 1, 15, '0');
		expectToken(charConst, 1, 19, '!');
		expectToken(charConst, 1, 23, '"');
		expectToken(charConst, 1, 27, invalidChar);
		expectToken(charConst, 1, 31, '\0');
		expectToken(rbrace, 1, 34);
		expectToken(eof, 1, 36);

		scanAndVerify();
	}

	@Test
	public void singleCharConst() {
		initScanner("'x'");

		expectToken(charConst, 1, 1, 'x');
		expectToken(eof, 1, 4);

		scanAndVerify();
	}

	@Test
	public void escapeCharConst() {
		initScanner(" {'\\n' '\\r' '\\\\' '\\''} ");

		expectToken(lbrace, 1, 2);
		expectToken(charConst, 1, 3, '\n');
		expectToken(charConst, 1, 8, '\r');
		expectToken(charConst, 1, 13, '\\');
		expectToken(charConst, 1, 18, '\'');
		expectToken(rbrace, 1, 22);
		expectToken(eof, 1, 24);

		scanAndVerify();
	}

	@Test
	public void singleEscapeCharConst() {
		initScanner("'\\n'");

		expectToken(charConst, 1, 1, '\n');
		expectToken(eof, 1, 5);

		scanAndVerify();
	}

	@Test
	public void emptyCharConst() {
		initScanner(" {''} ");

		expectToken(lbrace, 1, 2);
		expectToken(charConst, 1, 3, '\0');
		expectError(1, 3, EMPTY_CHARCONST);
		expectToken(rbrace, 1, 5);
		expectToken(eof, 1, 7);

		scanAndVerify();
	}

	@Test
	public void unclosedCharConst() {
		initScanner(" {'a} ");

		expectToken(lbrace, 1, 2);
		expectToken(charConst, 1, 3, '\0');
		expectError(1, 3, MISSING_QUOTE);
		expectToken(rbrace, 1, 5);
		expectToken(eof, 1, 7);

		scanAndVerify();
	}

	@Test
	public void emptyAndUnclosedCharConst() {
		initScanner(" \'\'\' ");

		expectToken(charConst, 1, 2, '\0');
		expectError(1, 2, EMPTY_CHARCONST);
		expectToken(charConst, 1, 4, '\0');
		expectError(1, 4, MISSING_QUOTE);
		expectToken(eof, 1, 6);

		scanAndVerify();
	}

	@Test
	public void unclosedEscapeCharConst() {
		initScanner(" {'\\r} ");

		expectToken(lbrace, 1, 2);
		expectToken(charConst, 1, 3, '\0');
		expectError(1, 3, MISSING_QUOTE);
		expectToken(rbrace, 1, 6);
		expectToken(eof, 1, 8);

		scanAndVerify();
	}

	@Test
	public void unclosedBackslashCharConst() {
		initScanner(" {'\\'} ");

		expectToken(lbrace, 1, 2);
		expectToken(charConst, 1, 3, '\0');
		expectError(1, 3, MISSING_QUOTE);
		expectToken(rbrace, 1, 6);
		expectToken(eof, 1, 8);

		scanAndVerify();
	}

	@Test
	public void invalidEscapeCharConst() {
		initScanner(" {'\\a'} ");

		expectToken(lbrace, 1, 2);
		expectToken(charConst, 1, 3, '\0');
		expectError(1, 3, UNDEFINED_ESCAPE, 'a');
		expectToken(rbrace, 1, 7);
		expectToken(eof, 1, 9);

		scanAndVerify();
	}

	@Test
	public void invalidEscapeCharMissingQuote() {
		initScanner(" '\\a ");

		expectToken(charConst, 1, 2, '\0');
		expectError(1, 2, UNDEFINED_ESCAPE, 'a');
		expectError(1, 2, MISSING_QUOTE);
		expectToken(eof, 1, 6);

		scanAndVerify();
	}

	@Test
	public void fileEndCharConst() {
		initScanner(" {'");

		expectToken(lbrace, 1, 2);
		expectToken(charConst, 1, 3, '\0');
		expectError(1, 3, EOF_IN_CHAR);
		expectToken(eof, 1, 4);

		scanAndVerify();
	}

	@Test
	public void lineEndCharConst() {
		initScanner(" {'" + LF + "'a'} ");

		expectToken(lbrace, 1, 2);
		expectToken(charConst, 1, 3, '\0');
		expectError(1, 3, ILLEGAL_LINE_END);
		expectToken(charConst, 2, 1, 'a');
		expectToken(rbrace, 2, 4);
		expectToken(eof, 2, 6);

		scanAndVerify();
	}

	@Test
	public void lineEndWithCRCharConst() {
		initScanner(" {'" + CR + LF + "'a'} ");

		expectToken(lbrace, 1, 2);
		expectToken(charConst, 1, 3, '\0');
		expectError(1, 3, ILLEGAL_LINE_END);
		expectToken(charConst, 2, 1, 'a');
		expectToken(rbrace, 2, 4);
		expectToken(eof, 2, 6);

		scanAndVerify();
	}

	@Test
	public void keyword1() {
		initScanner(" { if } ");

		expectToken(lbrace, 1, 2);
		expectToken(if_, 1, 4);
		expectToken(rbrace, 1, 7);
		expectToken(eof, 1, 9);

		scanAndVerify();
	}

	@Test
	public void keyword2() {
		initScanner(" {if} ");

		expectToken(lbrace, 1, 2);
		expectToken(if_, 1, 3);
		expectToken(rbrace, 1, 5);
		expectToken(eof, 1, 7);

		scanAndVerify();
	}

	@Test
	public void singleKeyword() {
		initScanner("if");

		expectToken(if_, 1, 1);
		expectToken(eof, 1, 3);

		scanAndVerify();
	}

	@Test
	public void keyword3() {
		initScanner(" {for_} ");

		expectToken(lbrace, 1, 2);
		expectToken(ident, 1, 3, "for_");
		expectToken(rbrace, 1, 7);
		expectToken(eof, 1, 9);

		scanAndVerify();
	}

	@Test
	public void keyword4() {
		initScanner(" {&if} ");

		expectToken(lbrace, 1, 2);
		expectToken(none, 1, 3);
		expectError(1, 3, INVALID_CHAR, '&');
		expectToken(if_, 1, 4);
		expectToken(rbrace, 1, 6);
		expectToken(eof, 1, 8);

		scanAndVerify();
	}

	@Test
	public void caseSensitiv1() {
		initScanner(" {For} ");

		expectToken(lbrace, 1, 2);
		expectToken(ident, 1, 3, "For");
		expectToken(rbrace, 1, 6);
		expectToken(eof, 1, 8);

		scanAndVerify();
	}

	@Test
	public void caseSensitiv2() {
		initScanner(" {FOR} ");

		expectToken(lbrace, 1, 2);
		expectToken(ident, 1, 3, "FOR");
		expectToken(rbrace, 1, 6);
		expectToken(eof, 1, 8);

		scanAndVerify();
	}

	@Test
	public void simpleSingleLineComment() {
		initScanner(" {/* Simple / single * line comment. */} ");

		expectToken(lbrace, 1, 2);
		expectToken(rbrace, 1, 40);
		expectToken(eof, 1, 42);

		scanAndVerify();
	}

	@Test
	public void simpleMultiLineComment() {
		initScanner(" {" + LF + "  /* Simple " + LF + "     / multi * line " + LF //
				+ "     comment. */ " + LF + " } ");

		expectToken(lbrace, 1, 2);
		expectToken(rbrace, 5, 2);
		expectToken(eof, 5, 4);

		scanAndVerify();
	}

	@Test
	public void nestedSingleLineComment2() {
		initScanner(" {/*//*///****/**/*/} ");

		expectToken(lbrace, 1, 2);
		expectToken(rbrace, 1, 21);
		expectToken(eof, 1, 23);

		scanAndVerify();
	}

	@Test
	public void nestedSingleLineComment() {
		initScanner(" {/* This / is * a /* nested  /* single line */ comment. */*/} ");

		expectToken(lbrace, 1, 2);
		expectToken(rbrace, 1, 62);
		expectToken(eof, 1, 64);

		scanAndVerify();
	}

	@Test
	public void nestedMultiLineComment() {
		initScanner(" {" + LF + "  /* This / is * a " + LF + "   /* nested  " + LF //
				+ "    /* multi line */" + LF + "    comment. " + LF + "   */" + LF //
				+ "  */ " + LF + " } ");

		expectToken(lbrace, 1, 2);
		expectToken(rbrace, 8, 2);
		expectToken(eof, 8, 4);

		scanAndVerify();
	}

	@Test
	public void nestedMultiLineComment2() {
		initScanner(" {" + LF + "  /* This / is * a " + LF + "   /* nested  " + LF //
				+ "    /* multi /*/* double nestet */*/ line */" + LF + "    comment. " + LF + "   */" + LF //
				+ "  */ " + LF + " } ");

		expectToken(lbrace, 1, 2);
		expectToken(rbrace, 8, 2);
		expectToken(eof, 8, 4);

		scanAndVerify();
	}

	@Test
	public void commentAtEnd1() {
		initScanner(" {/* This / is * a /* nested  /* single line */ comment. */*/ ");

		expectToken(lbrace, 1, 2);
		expectToken(eof, 1, 63);

		scanAndVerify();
	}

	@Test
	public void commentAtEnd2() {
		initScanner(" {/* This / is * a /* nested  /* single line */ comment. */*/");

		expectToken(lbrace, 1, 2);
		expectToken(eof, 1, 62);

		scanAndVerify();
	}

	@Test
	public void unclosedComment() {
		initScanner(" {/* This / is * a nested unclosed comment. } ");

		expectToken(lbrace, 1, 2);
		expectError(1, 3, EOF_IN_COMMENT);
		expectToken(eof, 1, 47);

		scanAndVerify();
	}

	@Test
	public void unclosedComment2() {
		initScanner(" {/*/");

		expectToken(lbrace, 1, 2);
		expectError(1, 3, EOF_IN_COMMENT);
		expectToken(eof, 1, 6);

		scanAndVerify();
	}

	@Test
	public void nestedUnclosedComment() {
		initScanner(" {/* This / is * a /* nested /* unclosed comment. */} ");

		expectToken(lbrace, 1, 2);
		expectError(1, 3, EOF_IN_COMMENT);
		expectToken(eof, 1, 55);

		scanAndVerify();
	}

	@Test
	public void nestedUnclosedComment2() {
		initScanner(" {/* This / is * a nested unclosed /* comment. } */");

		expectToken(lbrace, 1, 2);
		expectError(1, 3, EOF_IN_COMMENT);
		expectToken(eof, 1, 52);

		scanAndVerify();
	}

	@Test
	public void noLineComment() {
		initScanner(" {This is // no comment} ");

		expectToken(lbrace, 1, 2);
		expectToken(ident, 1, 3, "This");
		expectToken(ident, 1, 8, "is");
		expectToken(slash, 1, 11);
		expectToken(slash, 1, 12);
		expectToken(ident, 1, 14, "no");
		expectToken(ident, 1, 17, "comment");
		expectToken(rbrace, 1, 24);
		expectToken(eof, 1, 26);

		scanAndVerify();
	}

	@Test
	public void dots() {
		initScanner(".......... .....");

		expectToken(ppperiod, 1, 1);
		expectToken(ppperiod, 1, 4);
		expectToken(ppperiod, 1, 7);
		expectToken(period, 1, 10);
		expectToken(ppperiod, 1, 12);
		expectToken(pperiod, 1, 15);
		expectToken(eof, 1, 17);

		scanAndVerify();
	}

	@Test
	public void mulitilineDots() {
		initScanner("..\n.");

		expectToken(pperiod, 1, 1);
		expectToken(period, 2, 1);
		expectToken(eof, 2, 2);

		scanAndVerify();
	}

	@Test
	public void hash() {
		initScanner("foo(#3");

		expectToken(ident, 1, 1, "foo");
		expectToken(lpar, 1, 4);
		expectToken(hash, 1, 5);
		expectToken(number, 1, 6, 3);
		expectToken(eof, 1, 7);

		scanAndVerify();
	}

	@Test
	public void hash2() {
		initScanner("foo( # 3");

		expectToken(ident, 1, 1, "foo");
		expectToken(lpar, 1, 4);
		expectToken(hash, 1, 6);
		expectToken(number, 1, 8, 3);
		expectToken(eof, 1, 9);

		scanAndVerify();
	}

	@Test
	public void allTokens() {
		initScanner("anIdentifier 123 'c'" + LF //
				+ "+ - * / % == != < <= > >= && || = += -= *= /= %= ++ -- ; , . ( ) [ ] { }" + LF //
				+ "break class else final if new print program read return void while .. ... #" + LF);

		expectToken(ident, 1, 1, "anIdentifier");
		expectToken(number, 1, 14, 123);
		expectToken(charConst, 1, 18, 'c');
		expectToken(plus, 2, 1);
		expectToken(minus, 2, 3);
		expectToken(times, 2, 5);
		expectToken(slash, 2, 7);
		expectToken(rem, 2, 9);
		expectToken(eql, 2, 11);
		expectToken(neq, 2, 14);
		expectToken(lss, 2, 17);
		expectToken(leq, 2, 19);
		expectToken(gtr, 2, 22);
		expectToken(geq, 2, 24);
		expectToken(and, 2, 27);
		expectToken(or, 2, 30);
		expectToken(assign, 2, 33);
		expectToken(plusas, 2, 35);
		expectToken(minusas, 2, 38);
		expectToken(timesas, 2, 41);
		expectToken(slashas, 2, 44);
		expectToken(remas, 2, 47);
		expectToken(pplus, 2, 50);
		expectToken(mminus, 2, 53);
		expectToken(semicolon, 2, 56);
		expectToken(comma, 2, 58);
		expectToken(period, 2, 60);
		expectToken(lpar, 2, 62);
		expectToken(rpar, 2, 64);
		expectToken(lbrack, 2, 66);
		expectToken(rbrack, 2, 68);
		expectToken(lbrace, 2, 70);
		expectToken(rbrace, 2, 72);
		expectToken(break_, 3, 1);
		expectToken(class_, 3, 7);
		expectToken(else_, 3, 13);
		expectToken(final_, 3, 18);
		expectToken(if_, 3, 24);
		expectToken(new_, 3, 27);
		expectToken(print, 3, 31);
		expectToken(program, 3, 37);
		expectToken(read, 3, 45);
		expectToken(return_, 3, 50);
		expectToken(void_, 3, 57);
		expectToken(while_, 3, 62);
		expectToken(pperiod, 3, 68);
		expectToken(ppperiod, 3, 71);
		expectToken(hash, 3, 75);
		expectToken(eof, 4, 1);

		scanAndVerify();
	}
}

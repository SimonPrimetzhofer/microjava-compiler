package ssw.mj.impl;

import ssw.mj.Errors.Message;
import ssw.mj.Parser;
import ssw.mj.Scanner;
import ssw.mj.Token.Kind;
import ssw.mj.symtab.Obj;
import ssw.mj.symtab.Struct;
import ssw.mj.symtab.Tab;

import java.util.EnumSet;

public final class ParserImpl extends Parser {

    private static final String MAIN_NAME = "main";

    private final EnumSet<Kind> firstAssignop;
    private final EnumSet<Kind> firstExpr;
    private final EnumSet<Kind> firstStatement;
    private final EnumSet<Kind> firstRelop;
    private final EnumSet<Kind> recoverDecl;
    private final EnumSet<Kind> recoverStat;
    private final EnumSet<Kind> recoverMethodDecl;
    private int errDist = 3;

    public ParserImpl(Scanner scanner) {
        super(scanner);

        firstAssignop = EnumSet.of(Kind.assign, Kind.plusas, Kind.minusas, Kind.timesas, Kind.slashas, Kind.remas);
        firstExpr = EnumSet.of(Kind.minus, Kind.ident, Kind.number, Kind.charConst, Kind.new_, Kind.lpar);
        firstStatement = EnumSet.of(Kind.ident, Kind.if_, Kind.while_, Kind.break_, Kind.return_, Kind.read, Kind.print, Kind.lbrace, Kind.semicolon);
        firstRelop = EnumSet.of(Kind.eql, Kind.neq, Kind.gtr, Kind.geq, Kind.lss, Kind.leq);
        recoverDecl = EnumSet.of(Kind.final_, Kind.class_, Kind.lbrace, Kind.eof);
        recoverMethodDecl = EnumSet.of(Kind.ident, Kind.void_, Kind.rbrace, Kind.eof);
        recoverStat = EnumSet.of(Kind.if_, Kind.while_, Kind.break_, Kind.return_, Kind.read, Kind.print, Kind.semicolon, Kind.rbrace, Kind.eof);
    }

    @Override
    public void parse() {
        scan();
        Program();
        check(Kind.eof);
    }

    private void scan() {
        t = la;
        la = scanner.next();
        sym = la.kind;
        errDist++;
    }

    private void check(Kind expected) {
        if (sym == expected) {
            scan();
        } else {
            error(Message.TOKEN_EXPECTED, expected.label());
        }
    }

    private void Program() {
        check(Kind.program);
        check(Kind.ident);

        Obj prog = tab.insert(Obj.Kind.Prog, t.str, Tab.noType);

        tab.openScope();

        for (; ; ) {
            if (sym == Kind.final_) {
                ConstDecl();
            } else if (sym == Kind.ident) {
                VarDecl();
            } else if (sym == Kind.class_) {
                ClassDecl();
            } else if (sym == Kind.lbrace || sym == Kind.eof) {
                break;
            } else {
                recoverDecl();
            }
        }
        if (tab.curScope.locals().size() > MAX_GLOBALS) {
            error(Message.TOO_MANY_GLOBALS);
        }

        check(Kind.lbrace);

        for (; ; ) {
            if (sym == Kind.ident || sym == Kind.void_) {
                MethodDecl();
            } else if (sym == Kind.rbrace || sym == Kind.eof) {
                break;
            } else {
                recoverMethodDecl();
            }
        }

        check(Kind.rbrace);

        prog.locals = tab.curScope.locals();

        tab.closeScope();
    }

    private void ConstDecl() {
        check(Kind.final_);
        StructImpl type = Type();
        check(Kind.ident);
        Obj obj = tab.insert(Obj.Kind.Con, t.str, type);
        check(Kind.assign);

        if (sym == Kind.number) {
            if (type.kind != Struct.Kind.Int) {
                error(Message.CONST_TYPE);
            }
            scan();
        } else if (sym == Kind.charConst) {
            if (type.kind != Struct.Kind.Char) {
                error(Message.CONST_TYPE);
            }
            scan();
        } else {
            error(Message.CONST_DECL);
        }
        obj.val = t.val;
        check(Kind.semicolon);
    }

    private void VarDecl() {
        StructImpl type = Type();
        check(Kind.ident);
        tab.insert(Obj.Kind.Var, t.str, type);

        while (sym == Kind.comma) {
            scan();
            check(Kind.ident);
            tab.insert(Obj.Kind.Var, t.str, type);
        }
        check(Kind.semicolon);
    }

    private void ClassDecl() {
        check(Kind.class_);
        check(Kind.ident);

        Obj classObj = tab.insert(Obj.Kind.Type, t.str, new StructImpl(StructImpl.Kind.Class));
        check(Kind.lbrace);
        tab.openScope();

        while (sym == Kind.ident) {
            VarDecl();
        }
        classObj.type.fields = tab.curScope.locals();
        if (classObj.type.nrFields() > MAX_FIELDS) {
            error(Message.TOO_MANY_FIELDS);
        }

        tab.closeScope();

        check(Kind.rbrace);
    }

    private void MethodDecl() {
        StructImpl type = Tab.noType;
        if (sym == Kind.ident) {
            type = Type();
        } else if (sym == Kind.void_) {
            scan();
        } else {
            error(Message.METH_DECL);
        }

        check(Kind.ident);

        Obj meth = tab.insert(Obj.Kind.Meth, t.str, type);
        meth.adr = code.pc;

        check(Kind.lpar);
        tab.openScope();

        if (sym == Kind.ident) {
            FormPars(meth);
        }
        check(Kind.rpar);

        if (meth.name.equals(MAIN_NAME) && meth.nPars > 0) {
            error(Message.MAIN_WITH_PARAMS);
        }

        if (meth.name.equals(MAIN_NAME) && type.kind != Tab.noType.kind) {
            error(Message.MAIN_NOT_VOID);
        }

        while (sym == Kind.ident) {
            VarDecl();
        }
        if (tab.curScope.locals().size() > MAX_LOCALS) {
            error(Message.TOO_MANY_LOCALS);
        }

        Block();

        meth.locals = tab.curScope.locals();
        tab.closeScope();
    }

    private StructImpl Type() {
        check(Kind.ident);
        Obj o = tab.find(t.str);
        if (o.kind != Obj.Kind.Type) {
            error(Message.NO_TYPE);
        }
        StructImpl type = o.type;
        if (sym == Kind.lbrack) {
            scan();
            check(Kind.rbrack);
            type = new StructImpl(type);
        }
        return type;
    }

    private void FormPars(Obj meth) {
        Obj obj;
        for (; ; ) {
            StructImpl type = Type();
            check(Kind.ident);
            obj = tab.insert(Obj.Kind.Var, t.str, type);
            meth.nPars++;

            if (sym == Kind.comma) {
                scan();
            } else break;
        }
        if (sym == Kind.ppperiod) {
            scan();
            meth.hasVarArg = true;
            obj.type = new StructImpl(obj.type);
        }
    }

    private void Block() {
        check(Kind.lbrace);
        // check for rbrace and eof since we could not enter recover statements otherwise
        // since the loop would not be entered if the start of a statement is incorrect
        while (sym != Kind.rbrace && sym != Kind.eof) {
            Statement();
        }
        check(Kind.rbrace);
    }

    private void Statement() {
        if (!firstStatement.contains(sym)) {
            recoverStat();
        }
        switch (sym) {
            case ident:
                Designator();

                // avoid nested switch in this case
                if (firstAssignop.contains(sym)) {
                    Assignop();
                    Expr();
                } else if (sym == Kind.lpar) {
                    ActPars();
                } else if (sym == Kind.pplus) {
                    scan();
                } else if (sym == Kind.mminus) {
                    scan();
                } else error(Message.DESIGN_FOLLOW);

                check(Kind.semicolon);
                break;
            case if_:
                scan();
                check(Kind.lpar);
                Condition();
                check(Kind.rpar);
                Statement();
                if (sym == Kind.else_) {
                    scan();
                    Statement();
                }
                break;
            case while_:
                scan();
                check(Kind.lpar);
                Condition();
                check(Kind.rpar);
                Statement();
                break;
            case break_:
                scan();
                check(Kind.semicolon);
                break;
            case return_:
                scan();
                if (firstExpr.contains(sym)) {
                    Expr();
                }
                check(Kind.semicolon);
                break;
            case read:
                scan();
                check(Kind.lpar);
                Designator();
                check(Kind.rpar);
                check(Kind.semicolon);
                break;
            case print:
                scan();
                check(Kind.lpar);
                Expr();
                if (sym == Kind.comma) {
                    scan();
                    check(Kind.number);
                }
                check(Kind.rpar);
                check(Kind.semicolon);
                break;
            case lbrace:
                Block();
                break;
            case semicolon:
                scan();
                break;
        }
    }

    private void Designator() {
        check(Kind.ident);
        for (; ; ) {
            if (sym == Kind.period) {
                scan();
                check(Kind.ident);
            } else if (sym == Kind.lbrack) {
                scan();
                Expr();
                check(Kind.rbrack);
            } else break;
        }
    }

    private void Expr() {
        if (sym == Kind.minus) {
            scan();
        }
        Term();

        while (sym == Kind.plus || sym == Kind.minus) {
            AddOp();
            Term();
        }
    }

    private void Term() {
        Factor();

        while (sym == Kind.times || sym == Kind.slash || sym == Kind.rem) {
            MulOp();
            Factor();
        }
    }

    private void AddOp() {
        if (sym == Kind.plus || sym == Kind.minus) {
            scan();
        } else {
            error(Message.ADD_OP);
        }
    }

    private void MulOp() {
        if (sym == Kind.times || sym == Kind.slash || sym == Kind.rem) {
            scan();
        } else {
            error(Message.MUL_OP);
        }
    }

    private void Assignop() {
        switch (sym) {
            case assign:
                scan();
                break;
            case plusas:
                scan();
                break;
            case minusas:
                scan();
                break;
            case timesas:
                scan();
                break;
            case slashas:
                scan();
                break;
            case remas:
                scan();
                break;
            default:
                error(Message.ASSIGN_OP);
        }
    }

    private void Factor() {
        switch (sym) {
            case ident:
                Designator();
                if (sym == Kind.lpar) {
                    ActPars();
                }
                break;
            case number:
                scan();
                break;
            case charConst:
                scan();
                break;
            case new_:
                scan();
                check(Kind.ident);
                if (sym == Kind.lbrack) {
                    scan();
                    Expr();
                    check(Kind.rbrack);
                }
                break;
            case lpar:
                scan();
                Expr();
                check(Kind.rpar);
                break;
            default:
                error(Message.INVALID_FACT);
                break;
        }
    }

    private void ActPars() {
        check(Kind.lpar);
        if (firstExpr.contains(sym)) {
            Expr();
            while (sym == Kind.comma) {
                scan();
                Expr();
            }
        }

        if (sym == Kind.hash) {
            VarArgs();
        }
        check(Kind.rpar);
    }

    private void VarArgs() {
        check(Kind.hash);
        check(Kind.number);
        if (firstExpr.contains(sym)) {
            Expr();
            while (sym == Kind.comma) {
                scan();
                Expr();
            }
        }
    }

    private void Condition() {
        CondTerm();
        while (sym == Kind.or) {
            scan();
            CondTerm();
        }
    }

    private void CondTerm() {
        CondFact();
        while (sym == Kind.and) {
            scan();
            CondFact();
        }
    }

    private void CondFact() {
        Expr();
        Relop();
        Expr();
    }

    private void Relop() {
        if (firstRelop.contains(sym)) {
            scan();
        } else {
            error(Message.REL_OP);
        }
    }

    // override error method for error handling
    @Override
    public void error(Message msg, Object... msgParams) {
        if (errDist >= 3) {
            scanner.errors.error(la.line, la.col, msg, msgParams);
        }
        errDist = 0;
    }

    private void recoverDecl() {
        error(Message.INVALID_DECL);
        do {
            scan();
        } while (!recoverDecl.contains(sym) && !(sym == Kind.ident && tab.find(t.str).type != Tab.noType));
        errDist = 0;
    }

    private void recoverMethodDecl() {
        error(Message.METH_DECL);
        do {
            scan();
        } while (!recoverMethodDecl.contains(sym));
        errDist = 0;
    }

    private void recoverStat() {
        error(Message.INVALID_STAT);
        do {
            scan();
        } while (!recoverStat.contains(sym));
        if (sym == Kind.semicolon) {
            scan();
        }
        errDist = 0;
    }

}

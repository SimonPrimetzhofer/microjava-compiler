package ssw.mj.impl;

import ssw.mj.Errors.Message;
import ssw.mj.Parser;
import ssw.mj.Scanner;
import ssw.mj.Token.Kind;
import ssw.mj.codegen.Code.OpCode;
import ssw.mj.codegen.Operand;
import ssw.mj.symtab.Obj;
import ssw.mj.symtab.Struct;
import ssw.mj.symtab.Tab;

import java.util.EnumSet;
import java.util.Iterator;

public final class ParserImpl extends Parser {

    private static final String MAIN_NAME = "main";
    private static final int INVALID_MAIN_PC = -1;
    private static final int MIN_DIST = 3;
    private static final int INC_VALUE = 1;


    private final EnumSet<Kind> firstAssignop;
    private final EnumSet<Kind> firstExpr;
    private final EnumSet<Kind> firstStatement;
    private final EnumSet<Kind> recoverDecl;
    private final EnumSet<Kind> recoverStat;
    private final EnumSet<Kind> recoverMethodDecl;
    private int errDist = 3;

    public ParserImpl(Scanner scanner) {
        super(scanner);

        firstAssignop = EnumSet.of(Kind.assign, Kind.plusas, Kind.minusas, Kind.timesas, Kind.slashas, Kind.remas);
        firstExpr = EnumSet.of(Kind.minus, Kind.ident, Kind.number, Kind.charConst, Kind.new_, Kind.lpar);
        firstStatement = EnumSet.of(Kind.ident, Kind.if_, Kind.while_, Kind.break_, Kind.return_, Kind.read, Kind.print, Kind.lbrace, Kind.semicolon);
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

        // main method not found
        if (code.mainpc == INVALID_MAIN_PC) {
            error(Message.METH_NOT_FOUND, MAIN_NAME);
        }

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
        code.dataSize++;

        while (sym == Kind.comma) {
            scan();
            check(Kind.ident);
            tab.insert(Obj.Kind.Var, t.str, type);
            code.dataSize++;
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
        check(Kind.rbrace);

        tab.closeScope();

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

        check(Kind.lpar);
        tab.openScope();

        if (sym == Kind.ident) {
            FormPars(meth);
        }
        check(Kind.rpar);

        // if we are parsing the main method, we have to set the main program count
        if (meth.name.equals(MAIN_NAME)) {
            code.mainpc = code.pc;

            if (meth.nPars > 0) {
                error(Message.MAIN_WITH_PARAMS);
            }

            if (!meth.type.equals(Tab.noType)) {
                error(Message.MAIN_NOT_VOID);
            }
        }

        while (sym == Kind.ident) {
            VarDecl();
        }

        if (tab.curScope.locals().size() > MAX_LOCALS) {
            error(Message.TOO_MANY_LOCALS);
        } else {
            meth.adr = code.pc;
            code.put(OpCode.enter);
            code.put(meth.nPars);
            code.put(tab.curScope.nVars());
        }

        Block();

        meth.locals = tab.curScope.locals();

        if (meth.type == Tab.noType) {
            code.put(OpCode.exit);
            code.put(OpCode.return_);
        } else {
            code.put(OpCode.trap);
            code.put(1);
        }

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

        Operand x;

        switch (sym) {
            case ident:
                x = Designator();

                // avoid nested switch in this case
                if (firstAssignop.contains(sym)) {
                    OpCode op = Assignop();

                    if (op != OpCode.nop) {
                        if (!code.isAssignable(x)) {
                            error(Message.NO_VAR);
                        }

                        if (x.kind == Operand.Kind.Elem) {
                            code.put(OpCode.dup2);
                            code.put(OpCode.aload);
                        } else {
                            code.put(OpCode.dup);
                        }
                    }

                    Operand y = Expr();

                    if (op != OpCode.nop && (x.type != Tab.intType || y.type != Tab.intType)) {
                        error(Message.NO_INT_OP);
                    }

                    if (y.type.assignableTo(x.type)) {
                        code.assign(x, y, op);
                    } else {
                        error(Message.INCOMP_TYPES);
                    }
                } else if (sym == Kind.lpar) {
                    if (x.kind != Operand.Kind.Meth) {
                        error(Message.NO_METH);
                        x.obj = tab.noObj;
                    }
                    ActPars(x);
                } else if (sym == Kind.pplus) {
                    if (x.type != Tab.intType) {
                        error(Message.NO_INT);
                    }
                    if (!code.isAssignable(x)) {
                        error(Message.NO_VAR);
                    }

                    if (x.kind != Operand.Kind.Local) {
                        code.incFieldOrElem(x, INC_VALUE);
                    } else {
                        code.incLocal(x, INC_VALUE);
                    }
                    scan();
                } else if (sym == Kind.mminus) {
                    if (x.type != Tab.intType) {
                        error(Message.NO_INT);
                    }

                    if (!code.isAssignable(x)) {
                        error(Message.NO_VAR);
                    }

                    if (x.kind != Operand.Kind.Local) {
                        code.incFieldOrElem(x, -INC_VALUE);
                    } else {
                        code.incLocal(x, -INC_VALUE);
                    }
                    scan();
                } else {
                    error(Message.DESIGN_FOLLOW);
                }

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
                    x = Expr();
                    code.load(x);
                }
                check(Kind.semicolon);
                code.put(OpCode.exit);
                code.put(OpCode.return_);
                break;
            case read:
                scan();
                check(Kind.lpar);
                x = Designator();
                if (x.type != Tab.intType && x.type != Tab.charType) {
                    error(Message.READ_VALUE);
                }
                check(Kind.rpar);
                check(Kind.semicolon);

                code.load(x);
                if (x.type.kind == StructImpl.Kind.Char) {
                    code.put(OpCode.bread);
                } else if (x.type.kind == StructImpl.Kind.Int) {
                    code.put(OpCode.read);
                }
                code.storeConst(x.adr);
                break;
            case print:
                scan();
                check(Kind.lpar);
                x = Expr();
                code.load(x);

                if (sym == Kind.comma) {
                    scan();
                    check(Kind.number);
                    code.loadConst(t.val);
                } else {
                    // if no further parameters are present push width 1
                    code.loadConst(INC_VALUE);
                }

                if (x.type == Tab.intType) {
                    code.put(OpCode.print);
                } else if (x.type == Tab.charType) {
                    code.put(OpCode.bprint);
                } else {
                    error(Message.PRINT_VALUE);

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

    private Operand Designator() {
        check(Kind.ident);

        Operand x = new Operand(tab.find(t.str), this);
        for (; ; ) {
            if (sym == Kind.period) {
                if (x.type.kind != Struct.Kind.Class) {
                    error(Message.NO_CLASS);
                }
                scan();
                code.load(x);
                check(Kind.ident);

                Obj obj = tab.findField(t.str, x.type);
                x.kind = Operand.Kind.Fld;
                x.type = obj.type;
                x.adr = obj.adr;
            } else if (sym == Kind.lbrack) {
                code.load(x);
                scan();

                Operand y = Expr();

                if (x.type.kind != StructImpl.Kind.Arr) {
                    error(Message.NO_ARRAY);
                }

                if (y.type.kind != StructImpl.Kind.Int) {
                    error(Message.ARRAY_INDEX);
                }

                code.load(y);
                x.kind = Operand.Kind.Elem;
                x.type = x.type.elemType;

                check(Kind.rbrack);
            } else break;
        }
        return x;
    }

    private Operand Expr() {
        OpCode negCode = null;
        if (sym == Kind.minus) {
            scan();
            negCode = OpCode.neg;
        }
        Operand x = Term();

        // Term started with minus
        if (negCode != null) {
            if (x.type != Tab.intType) {
                error(Message.NO_INT_OP);
            }
            if (x.kind == Operand.Kind.Con) {
                x.val = -x.val;
            } else {
                code.load(x);
                code.put(OpCode.neg);
            }
        }


        while (sym == Kind.plus || sym == Kind.minus) {
            OpCode op = AddOp();
            code.load(x);

            Operand y = Term();
            code.load(y);

            if (x.type != Tab.intType || y.type != Tab.intType) {
                error(Message.NO_INT_OP);
            }

            code.put(op);
        }

        return x;
    }

    private Operand Term() {
        Operand x = Factor();

        while (sym == Kind.times || sym == Kind.slash || sym == Kind.rem) {
            OpCode op = MulOp();
            code.load(x);
            Operand y = Factor();
            code.load(y);

            if (x.type != Tab.intType || y.type != Tab.intType) {
                error(Message.NO_INT_OP);
            }

            code.put(op);
        }

        return x;
    }

    private OpCode AddOp() {
        if (sym == Kind.plus) {
            scan();
            return OpCode.add;
        } else if (sym == Kind.minus) {
            scan();
            return OpCode.sub;
        }
        error(Message.ADD_OP);
        return OpCode.nop;
    }

    private OpCode MulOp() {
        switch (sym) {
            case times:
                scan();
                return OpCode.mul;
            case slash:
                scan();
                return OpCode.div;
            case rem:
                scan();
                return OpCode.rem;
            default:
                error(Message.MUL_OP);
                return OpCode.nop;
        }
    }

    private OpCode Assignop() {
        switch (sym) {
            case assign:
                scan();
                return OpCode.nop;
            case plusas:
                scan();
                return OpCode.add;
            case minusas:
                scan();
                return OpCode.sub;
            case timesas:
                scan();
                return OpCode.mul;
            case slashas:
                scan();
                return OpCode.div;
            case remas:
                scan();
                return OpCode.rem;
            default:
                error(Message.ASSIGN_OP);
                return OpCode.nop;
        }
    }

    private Operand Factor() {
        Operand x;

        switch (sym) {
            case ident:
                x = Designator();
                if (sym == Kind.lpar) {
                    if (x.type == Tab.noType) {
                        error(Message.INVALID_CALL);
                    }
                    if (x.kind != Operand.Kind.Meth) {
                        error(Message.NO_METH);
                        x.obj = tab.noObj;
                    }
                    ActPars(x);
                }
                break;
            case number:
                scan();
                x = new Operand(t.val);
                break;
            case charConst:
                scan();
                x = new Operand(t.val);
                x.type = Tab.charType;
                break;
            case new_:
                scan();
                check(Kind.ident);

                Obj obj = tab.find(t.str);
                if (obj.kind != Obj.Kind.Type) {
                    error(Message.NO_TYPE);
                }

                StructImpl type = obj.type;

                if (sym == Kind.lbrack) {
                    scan();

                    Operand y = Expr();
                    if (y.type != Tab.intType) {
                        error(Message.ARRAY_SIZE);
                    }
                    code.load(y);
                    code.put(OpCode.newarray);

                    if (type == Tab.charType) {
                        // 0 stands for char-array
                        code.put(0);
                    } else {
                        // 1 stands for int-array
                        code.put(1);
                    }
                    type = new StructImpl(type);
                    check(Kind.rbrack);
                } else {
                    // Class or Type discovered
                    if (obj.kind != Obj.Kind.Type || type.kind != StructImpl.Kind.Class) {
                        error(Message.NO_CLASS_TYPE);
                    }
                    code.put(OpCode.new_);
                    code.put2(type.nrFields());
                }

                x = new Operand(type);

                break;
            case lpar:
                scan();
                x = Expr();
                check(Kind.rpar);
                break;
            default:
                error(Message.INVALID_FACT);
                x = new Operand(Tab.noType);
        }

        return x;
    }

    private void ActPars(Operand x) {
        check(Kind.lpar);

        x.kind = Operand.Kind.Stack;

        // we have to iterate over params to check assignability and amount of params
        Iterator<Obj> localsIterator = x.obj.locals.iterator();
        int nPars = 0;

        if (firstExpr.contains(sym)) {
            Operand y = Expr();
            nPars++;
            if (localsIterator.hasNext() && !y.type.assignableTo(localsIterator.next().type)) {
                error(Message.PARAM_TYPE);
            }

            // handle more params
            while (sym == Kind.comma) {
                scan();
                y = Expr();
                nPars++;
                if (localsIterator.hasNext() && !y.type.assignableTo(localsIterator.next().type)) {
                    error(Message.PARAM_TYPE);
                }
            }
        }

        // compare number of parameters and consider possible varargs (then more may follow)
        if (nPars < x.obj.nPars && !x.obj.hasVarArg) {
            error(Message.LESS_ACTUAL_PARAMS);
        } else if (nPars > x.obj.nPars) {
            error(Message.MORE_ACTUAL_PARAMS);
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
        Operand x = Expr();
        OpCode op = Relop();
        Operand y = Expr();

        if (!x.type.compatibleWith(y.type)) {
            error(Message.INCOMP_TYPES);
        } else if ((x.type.isRefType() || y.type.isRefType()) && (op != OpCode.jne && op != OpCode.jeq)) {
            error(Message.EQ_CHECK);
        }
    }

    private OpCode Relop() {
        switch (sym) {
            case eql:
                scan();
                return OpCode.jeq;
            case neq:
                scan();
                return OpCode.jne;
            case gtr:
                scan();
                return OpCode.jgt;
            case geq:
                scan();
                return OpCode.jge;
            case lss:
                scan();
                return OpCode.jlt;
            case leq:
                scan();
                return OpCode.jle;
            default:
                error(Message.REL_OP);
                return OpCode.nop;
        }
    }

    // override error method for error handling
    @Override
    public void error(Message msg, Object... msgParams) {
        if (errDist >= MIN_DIST) {
            scanner.errors.error(la.line, la.col, msg, msgParams);
        }
        errDist = 0;
    }

    private void recoverDecl() {
        error(Message.INVALID_DECL);
        do {
            scan();
        } while (!(recoverDecl.contains(sym) || (sym != Kind.ident || tab.find(t.str).type != Tab.noType) || t.kind == Kind.semicolon));
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

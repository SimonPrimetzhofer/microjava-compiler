package ssw.mj.impl;

import ssw.mj.Errors.Message;
import ssw.mj.Parser;
import ssw.mj.Scanner;
import ssw.mj.Token.Kind;
import ssw.mj.codegen.Code.CompOp;
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
    private Obj curMethod = null;

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

        curMethod = tab.insert(Obj.Kind.Meth, t.str, type);

        check(Kind.lpar);
        tab.openScope();

        if (sym == Kind.ident) {
            FormPars(curMethod);
        }
        check(Kind.rpar);

        // if we are parsing the main method, we have to set the main program count
        if (curMethod.name.equals(MAIN_NAME)) {
            code.mainpc = code.pc;

            if (curMethod.nPars > 0) {
                error(Message.MAIN_WITH_PARAMS);
            }

            if (!curMethod.type.equals(Tab.noType)) {
                error(Message.MAIN_NOT_VOID);
            }
        }

        while (sym == Kind.ident) {
            VarDecl();
        }

        if (tab.curScope.locals().size() > MAX_LOCALS) {
            error(Message.TOO_MANY_LOCALS);
        } else {
            curMethod.adr = code.pc;
            code.put(OpCode.enter);
            code.put(curMethod.nPars);
            code.put(tab.curScope.nVars());
        }

        Block(null);

        curMethod.locals = tab.curScope.locals();

        if (curMethod.type == Tab.noType) {
            code.put(OpCode.exit);
            code.put(OpCode.return_);
        } else {
            code.put(OpCode.trap);
            code.put(INC_VALUE);
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
            } else {
                break;
            }
        }
        if (sym == Kind.ppperiod) {
            scan();
            meth.hasVarArg = true;
            obj.type = new StructImpl(obj.type);
        }
    }

    private void Block(LabelImpl breakLab) {
        check(Kind.lbrace);
        // check for rbrace and eof since we could not enter recover statements otherwise
        // and the loop would not be entered if the start of a statement is incorrect
        while (sym != Kind.rbrace && sym != Kind.eof) {
            Statement(breakLab);
        }
        check(Kind.rbrace);
    }

    private void Statement(LabelImpl breakLab) {
        if (!firstStatement.contains(sym)) {
            recoverStat();
        }

        Operand x = null;

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
                            // don't load fields twice
                            if (x.kind != Operand.Kind.Fld) {
                                Operand.Kind opKind = x.kind;
                                code.load(x);
                                x.kind = opKind;
                            }
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
                x = Condition();
                code.fJump(x);
                check(Kind.rpar);
                x.tLabel.here();
                Statement(breakLab);

                if (sym == Kind.else_) {
                    LabelImpl ifEnd = new LabelImpl(code);
                    code.jump(ifEnd);
                    x.fLabel.here();
                    scan();
                    Statement(breakLab);
                    ifEnd.here();
                } else {
                    x.fLabel.here();
                }
                break;
            case while_:
                // break only allowed in while
                breakLab = new LabelImpl(code);

                scan();
                check(Kind.lpar);
                LabelImpl top = new LabelImpl(code);
                top.here();
                x = Condition();
                code.fJump(x);
                x.tLabel.here();
                check(Kind.rpar);
                Statement(breakLab);

                code.jump(top);
                x.fLabel.here();
                breakLab.here();
                break;
            case break_:
                scan();

                // check if break was encountered outside a loop
                if (breakLab == null) {
                    error(Message.NO_LOOP);
                } else {
                    code.jump(breakLab);
                }

                check(Kind.semicolon);
                break;
            case return_:
                scan();
                if (firstExpr.contains(sym)) {
                    if (curMethod.type == Tab.noType) {
                        error(Message.RETURN_VOID);
                    }
                    x = Expr();
                    code.load(x);

                    if (!x.type.assignableTo(curMethod.type)) {
                        error(Message.RETURN_TYPE);
                    }
                }

                if (x == null && curMethod.type != Tab.noType) {
                    error(Message.RETURN_NO_VAL);
                }

                check(Kind.semicolon);
                code.put(OpCode.exit);
                code.put(OpCode.return_);
                break;
            case read:
                scan();
                check(Kind.lpar);
                x = Designator();
                Operand.Kind kind = x.kind;
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
                code.store(x, kind);
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
                Block(breakLab);
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
                    code.createArray(-INC_VALUE, type);
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

        if (x.kind != Operand.Kind.Meth) {
            error(Message.NO_METH);
            x.obj = tab.noObj;
        }

        x.kind = Operand.Kind.Stack;

        // we have to iterate over params to check assignability and amount of them
        Iterator<Obj> localsIterator = x.obj.locals.iterator();
        int nPars = 0;

        if (firstExpr.contains(sym)) {
            Operand y = Expr();
            nPars++;
            if (localsIterator.hasNext() && !y.type.assignableTo(localsIterator.next().type)) {
                error(Message.PARAM_TYPE);
            }

            code.load(y);

            while (sym == Kind.comma) {
                scan();
                y = Expr();
                nPars++;

                if (localsIterator.hasNext() && !y.type.assignableTo(localsIterator.next().type) && (!x.obj.hasVarArg || localsIterator.hasNext())) {
                    error(Message.PARAM_TYPE);
                }
                code.load(y);
            }
        }

        // compare number of parameters and consider possible varargs (then more may follow)
        if (nPars < x.obj.nPars && !x.obj.hasVarArg) {
            error(Message.LESS_ACTUAL_PARAMS);
        } else if (nPars > x.obj.nPars) {
            error(Message.MORE_ACTUAL_PARAMS);
        }

        if (sym == Kind.hash) {
            if (x.obj.hasVarArg) {
                VarArgs(localsIterator.next());
            } else {
                VarArgs(tab.noObj);
                error(Message.INVALID_VARARG_CALL);
            }
        } else if (x.obj.hasVarArg) {
            VarArgs(tab.noObj);
        }

        if (x.obj == tab.lenObj) {
            code.put(OpCode.arraylength);
        } else if (x.obj != tab.ordObj && x.obj != tab.chrObj) {
            code.put(OpCode.call);
            code.put2(x.adr - (code.pc - 1));
        }

        check(Kind.rpar);
    }

    private void VarArgs(Obj obj) {
        int expectedVarArgs = 0;

        if (sym == Kind.hash) {
            scan();
            check(Kind.number);

            expectedVarArgs = t.val;
            code.createArray(expectedVarArgs, obj.type.elemType);

            int parsedVarArgs = 0;
            for (; ; ) {
                if (firstExpr.contains(sym)) {
                    code.put(OpCode.dup);
                    code.loadConst(parsedVarArgs);

                    Operand x = Expr();
                    if (obj != tab.noObj && !x.type.assignableTo(obj.type.elemType)) {
                        error(Message.PARAM_TYPE);
                    }

                    code.load(x);
                    code.storeInArray(obj.type.elemType);
                    parsedVarArgs++;
                } else if (sym == Kind.comma) {
                    scan();
                } else {
                    break;
                }
            }

            if (parsedVarArgs < expectedVarArgs) {
                error(Message.LESS_ACTUAL_VARARGS);
            } else if (parsedVarArgs > expectedVarArgs) {
                error(Message.MORE_ACTUAL_VARARGS);
            }
        } else {
            code.createArray(expectedVarArgs, obj.type.elemType);
        }
    }

    private Operand Condition() {
        Operand x = CondTerm();
        while (sym == Kind.or) {
            code.tJump(x);
            scan();
            x.fLabel.here();
            Operand y = CondTerm();
            x.fLabel = y.fLabel;
            x.op = y.op;
        }

        return x;
    }

    private Operand CondTerm() {
        Operand x = CondFact();
        while (sym == Kind.and) {
            code.fJump(x);
            scan();
            Operand y = CondFact();
            x.op = y.op;
        }

        return x;
    }

    private Operand CondFact() {
        Operand x = Expr();
        code.load(x);
        CompOp op = Relop();
        Operand y = Expr();
        code.load(y);

        if (!x.type.compatibleWith(y.type)) {
            error(Message.INCOMP_TYPES);
        } else if ((x.type.isRefType() || y.type.isRefType()) && (op != CompOp.ne && op != CompOp.eq)) {
            error(Message.EQ_CHECK);
        }

        if (op == null) {
            op = CompOp.eq;
        }

        return new Operand(op, code);
    }

    private CompOp Relop() {
        switch (sym) {
            case eql:
                scan();
                return CompOp.eq;
            case neq:
                scan();
                return CompOp.ne;
            case gtr:
                scan();
                return CompOp.gt;
            case geq:
                scan();
                return CompOp.ge;
            case lss:
                scan();
                return CompOp.lt;
            case leq:
                scan();
                return CompOp.le;
            default:
                error(Message.REL_OP);
                return null;
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

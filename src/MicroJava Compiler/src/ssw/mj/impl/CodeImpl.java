package ssw.mj.impl;

import ssw.mj.Errors.Message;
import ssw.mj.Parser;
import ssw.mj.codegen.Code;
import ssw.mj.codegen.Operand;
import ssw.mj.symtab.Tab;

import java.util.EnumSet;

public final class CodeImpl extends Code {

    private final EnumSet<Operand.Kind> assignableKinds;

    public CodeImpl(Parser p) {
        super(p);
        assignableKinds = EnumSet.of(Operand.Kind.Local, Operand.Kind.Static, Operand.Kind.Elem, Operand.Kind.Fld);
    }

    public void load(Operand x) {
        switch (x.kind) {
            case Con:
                loadConst(x.val);
                break;
            case Local:
                switch (x.adr) {
                    case 0:
                        put(OpCode.load_0);
                        break;
                    case 1:
                        put(OpCode.load_1);
                        break;
                    case 2:
                        put(OpCode.load_2);
                        break;
                    case 3:
                        put(OpCode.load_3);
                        break;
                    default:
                        put(OpCode.load);
                        put(x.adr);
                        break;
                }
                break;
            case Static:
                put(OpCode.getstatic);
                put2(x.adr);
                break;
            case Stack:
                break;
            case Fld:
                put(OpCode.getfield);
                put2(x.adr);
                break;
            case Elem:
                if (x.type == Tab.charType) {
                    put(OpCode.baload);
                } else {
                    put(OpCode.aload);
                }
                break;
            default:
                parser.error(Message.NO_VAL);
        }
        x.kind = Operand.Kind.Stack;
    }

    public void loadConst(int x) {
        switch (x) {
            case -1:
                put(OpCode.const_m1);
                break;
            case 0:
                put(OpCode.const_0);
                break;
            case 1:
                put(OpCode.const_1);
                break;
            case 2:
                put(OpCode.const_2);
                break;
            case 3:
                put(OpCode.const_3);
                break;
            case 4:
                put(OpCode.const_4);
                break;
            case 5:
                put(OpCode.const_5);
                break;
            default:
                put(OpCode.const_);
                put4(x);
        }
    }

    public void storeConst(int x) {
        switch (x) {
            case 0:
                put(OpCode.store_0);
                break;
            case 1:
                put(OpCode.store_1);
                break;
            case 2:
                put(OpCode.store_2);
                break;
            case 3:
                put(OpCode.store_3);
                break;
            default:
                put(OpCode.store);
                put4(x);
        }
    }

    public void assign(Operand x, Operand y, OpCode op) {
        load(y);

        // check, if we have a combined operation (like +=)
        if (op != OpCode.nop) {
            put(op);
        }
        switch (x.kind) {
            case Local:
                switch (x.adr) {
                    case 0:
                        put(OpCode.store_0);
                        break;
                    case 1:
                        put(OpCode.store_1);
                        break;
                    case 2:
                        put(OpCode.store_2);
                        break;
                    case 3:
                        put(OpCode.store_3);
                        break;
                    default:
                        put(OpCode.store);
                        put2(x.adr);
                        break;
                }
                break;
            case Static:
                put(OpCode.putstatic);
                put2(x.adr);
                break;
            case Fld:
                put(OpCode.putfield);
                put2(x.adr);
                break;
            case Elem:
                if (x.type == Tab.charType) {
                    put(OpCode.bastore);
                } else {
                    put(OpCode.astore);
                }
                break;
            default:
                parser.error(Message.NO_VAR);
        }
    }

    public void incLocal(Operand x, int val) {
        put(OpCode.inc);
        put(x.adr);
        put(val);
    }

    public void incFieldOrElem(Operand x, int val) {
        // load x changes kind to stack -> memorize it for later
        Operand.Kind opKind = x.kind;
        if (opKind == Operand.Kind.Elem) {
            put(OpCode.dup2);
        } else if (opKind != Operand.Kind.Static) {
            put(OpCode.dup);
        }

        load(x);
        loadConst(val);
        put(OpCode.add);

        if (opKind == Operand.Kind.Elem) {
            put(OpCode.astore);
        } else if (opKind == Operand.Kind.Con || opKind == Operand.Kind.Local || opKind == Operand.Kind.Fld) {
            put(OpCode.putfield);
            put2(x.adr);
        } else if (opKind == Operand.Kind.Static) {
            put(OpCode.putstatic);
            put2(x.adr);
        }
    }

    // we can assign values to local and global variables, array-elements and fields
    public boolean isAssignable(Operand x) {
        return assignableKinds.contains(x.kind);
    }

}

package ssw.mj.codegen;

import ssw.mj.Parser;
import ssw.mj.codegen.Code.CompOp;
import ssw.mj.impl.LabelImpl;
import ssw.mj.impl.StructImpl;
import ssw.mj.symtab.Obj;
import ssw.mj.symtab.Tab;

import static ssw.mj.Errors.Message.NO_OPERAND;

/**
 * An operand stores the attributes of an operand during code generation.
 */
public class Operand {
    /**
     * Possible operands.
     */
    public enum Kind {
        Con, Local, Static, Stack, Fld, Elem, Meth, Cond, None
    }

    /**
     * Kind of the operand.
     */
    public Kind kind;
    /**
     * The type of the operand (reference to symbol table).
     */
    public StructImpl type;
    /**
     * Only for Con: Value of the constant.
     */
    public int val;
    /**
     * Only for Local, Static, Fld, Meth: Offset of the element.
     */
    public int adr;
    /**
     * Only for Cond: Relational operator.
     */
    public CompOp op;
    /**
     * Only for Meth: Method object from the symbol table.
     */
    public Obj obj;
    /**
     * Only for Cond: Target for true jumps.
     */
    public LabelImpl tLabel;
    /**
     * Only for Cond: Target for false jumps.
     */
    public LabelImpl fLabel;

    /**
     * Constructor for named objects: constants, variables, methods
     */
    public Operand(Obj o, Parser parser) {
        type = o.type;
        val = o.val;
        adr = o.adr;
        switch (o.kind) {
            case Con:
                kind = Kind.Con;
                break;
            case Var:
                if (o.level == 0) {
                    kind = Kind.Static;
                } else {
                    kind = Kind.Local;
                }
                break;
            case Meth:
                kind = Kind.Meth;
                obj = o;
                break;
            default:
                kind = Kind.None;
                parser.error(NO_OPERAND);
        }
    }

    /**
     * Constructor for compare operations
     */
    public Operand(CompOp op, Code code) {
        this(code);
        this.kind = Kind.Cond;
        this.op = op;
    }

    public Operand(Code code) {
        tLabel = new LabelImpl(code);
        fLabel = new LabelImpl(code);
    }

    /**
     * Constructor for stack operands
     */
    public Operand(StructImpl type) {
        this.kind = Kind.Stack;
        this.type = type;
    }

    /**
     * Constructor for integer constants
     */
    public Operand(int x) {
        kind = Kind.Con;
        type = Tab.intType;
        val = x;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Op[");
        switch (kind) {
            case Con:
                sb.append(type).append(' ');
                sb.append(val);
                break;
            case Local:
            case Static:
            case Fld:
                sb.append(kind).append(' ');
                sb.append(type).append(' ');
                sb.append(adr);
                break;
            case Cond:
                sb.append(op);
                break;
            case Meth:
                sb.append(obj);
                break;
            case Elem:
            case Stack:
                sb.append(kind).append(' ');
                sb.append(type);
                break;
        }
        return sb.append(']').toString();
    }

}

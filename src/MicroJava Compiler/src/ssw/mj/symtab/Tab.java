package ssw.mj.symtab;

import ssw.mj.Parser;
import ssw.mj.impl.StructImpl;

/**
 * MicroJava Symbol Table
 */
public abstract class Tab {
    // Universe
    public static final StructImpl noType = new StructImpl(Struct.Kind.None);
    public static final StructImpl intType = new StructImpl(Struct.Kind.Int);
    public static final StructImpl charType = new StructImpl(Struct.Kind.Char);
    public static final StructImpl nullType = new StructImpl(Struct.Kind.Class);

    public Obj noObj, chrObj, ordObj, lenObj;

    /**
     * Only used for reporting errors.
     */
    protected final Parser parser;
    /**
     * The current top scope.
     */
    public Scope curScope = null;
    // First scope opening (universe) will increase this to -1
    /**
     * Nesting level of current scope.
     */
    protected int curLevel = -2;

    public Tab(Parser p) {
        parser = p;
    }
}

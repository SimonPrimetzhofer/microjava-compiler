package ssw.mj.impl;

import ssw.mj.symtab.Struct;

public final class StructImpl extends Struct {

    private StructImpl(Kind kind, StructImpl elemType) {
        super(kind, elemType);
    }

    public StructImpl(Kind kind) {
        super(kind);
    }

    public StructImpl(StructImpl elemType) {
        super(elemType);
    }

    // TODO Exercise 5: checks for different kinds of type compatibility

    @Override
    public boolean compatibleWith(StructImpl other) {
       // TODO
    	return false;
    }

    @Override
    public boolean assignableTo(StructImpl dest) {
        // TODO
    	return false;
    }
}

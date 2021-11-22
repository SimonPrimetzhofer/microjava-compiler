package ssw.mj.impl;

import ssw.mj.codegen.Code;
import ssw.mj.codegen.Label;

public final class LabelImpl extends Label {

    // TODO Exercise 6: Implementation of Labels for management of jump targets

	public LabelImpl(Code code) {
        super(code);
    }

    /**
     * Generates code for a jump to this label.
     */
    @Override
    public void put() {
        // TODO
    }

    /**
     * Defines <code>this</code> label to be at the current pc position
     */
    @Override
    public void here() {
        // TODO
    }
}

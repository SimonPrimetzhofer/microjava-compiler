package ssw.mj.codegen;

/**
 * MircoJava Jump Label. Forward jumps use labels which are still undefined.
 * Such jump instructions are linked in the code. A fixup is made as soon as the
 * label becomes defined. Backward jumps use labels which are already defined.
 * Such jump instructions are already generated with the definite label address.
 */
public abstract class Label {

    /**
     * Jump destination address.
     */
    protected int adr;

    /**
     * The code buffer this Label belongs to.
     */
    protected final Code code;

    public Label(Code code) {
        this.code = code;
    }

    public abstract void put();

    public abstract void here();
}

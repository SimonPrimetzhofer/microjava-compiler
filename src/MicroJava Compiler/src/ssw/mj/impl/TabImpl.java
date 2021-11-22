package ssw.mj.impl;

import ssw.mj.Errors.Message;
import ssw.mj.Parser;
import ssw.mj.symtab.Obj;
import ssw.mj.symtab.Obj.Kind;
import ssw.mj.symtab.Scope;
import ssw.mj.symtab.Tab;

import java.util.LinkedList;

public final class TabImpl extends Tab {

    /**
     * Set up "universe" (= predefined names).
     */
    public TabImpl(Parser p) {
        super(p);
        init();
    }

    private void init() {
        openScope();

        insert(Kind.Type, "int", intType);
        insert(Kind.Type, "char", charType);
        insert(Kind.Con, "null", nullType);

        chrObj = insert(Kind.Meth, "chr", charType);
        chrObj.locals = new LinkedList<>();
        Obj iObj = new Obj(Kind.Var, "i", intType);
        iObj.level = curLevel;
        chrObj.locals.add(iObj);
        chrObj.nPars = 1;

        ordObj = insert(Kind.Meth, "ord", intType);
        ordObj.locals = new LinkedList<>();
        Obj chObj = new Obj(Kind.Var, "ch", charType);
        chObj.level = curLevel;
        ordObj.locals.add(chObj);
        ordObj.nPars = 1;

        lenObj = insert(Kind.Meth, "len", intType);
        lenObj.locals = new LinkedList<>();
        Obj arrObj = new Obj(Kind.Var, "arr", new StructImpl(noType));
        arrObj.level = curLevel;
        lenObj.locals.add(arrObj);
        lenObj.nPars = 1;

        noObj = new Obj(Kind.Var, "$none", noType);
    }

    public void openScope() {
        curScope = new Scope(curScope);
        curLevel++;
    }

    public void closeScope() {
        curScope = curScope.outer();
        curLevel--;
    }

    public Obj insert(Kind kind, String name, StructImpl type) {
        // name must not be null
        if (name == null) {
            return noObj;
        }

        // check if element is already declared
        if (curScope.findLocal(name) != null) {
            parser.error(Message.DECL_NAME, name);
            return noObj;
        }

        Obj obj = new Obj(kind, name, type);
        if (kind == Kind.Var) {
            obj.adr = curScope.nVars();
            obj.level = curLevel;
        } else if (kind == Kind.Meth) {
            obj.adr = curScope.nVars();
        }
        curScope.insert(obj);

        return obj;
    }

    public Obj find(String name) {
        Obj obj = curScope.findGlobal(name);
        if (obj == null) {
            parser.error(Message.NOT_FOUND, name);
            return noObj;
        }
        return obj;
    }

    public Obj findField(String name, StructImpl type) {
        Obj obj = type.findField(name);

        if (obj == null) {
            parser.error(Message.NO_FIELD, name);
            return noObj;
        }

        return obj;
    }

}

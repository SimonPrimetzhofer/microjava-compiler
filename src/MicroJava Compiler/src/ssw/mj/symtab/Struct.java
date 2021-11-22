package ssw.mj.symtab;

import ssw.mj.impl.StructImpl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * MicroJava Type Structures: A type structure stores the type attributes of a
 * declared type.
 */
public abstract class Struct {
    /**
     * Possible codes for structure kinds.
     */
    public enum Kind {
        None, Int, Char, Arr, Class
    }

    /**
     * Kind of the structure node.
     */
    public final Kind kind;
    /**
     * Only for Arr: Type of the array elements.
     */
    public final StructImpl elemType;
    // This is a Collections.emptyMap() on purpose, do not change this line
    // If you finished reading the fields of a class, use clazz.fields = curScope.locals() and close the scope afterwards
    /**
     * Only for Class: First element of the linked list of local variables.
     */
    public List<Obj> fields = Collections.emptyList();

    protected Struct(Kind kind, StructImpl elemType) {
        this.kind = kind;
        this.elemType = elemType;
    }

    public Struct(Kind kind) {
        this(kind, null);
    }

    /**
     * Creates a new array structure with a specified element type.
     */
    public Struct(StructImpl elemType) {
        this(Kind.Arr, elemType);
    }

    /**
     * Retrieves the field <code>name</code>.
     */
    public Obj findField(String name) {
        for(Obj field : fields) {
            if(field.name.equals(name)) {
                return field;
            }
        }
        return null;
    }

    /**
     * Only for Class: Number of fields.
     */
    public int nrFields() {
        return fields.size();
    }

    @Override
    public String toString() {
        if (this == Tab.nullType) {
            return "null";
        }
        switch (kind) {
            case Int:
            case Char:
            case None:
                return kind.toString();
            case Arr:
                return elemType + "[]";
            case Class:
                StringBuilder sb = new StringBuilder();
                sb.append("Class{");
                boolean first = true;
                for (Obj field : fields) {
                    if (!first) {
                        sb.append(", ");
                    }
                    sb.append(field.name).append('=').append(field.type);
                    first = false;
                }
                sb.append('}');
                return sb.toString();
        }
        throw new RuntimeException("Unknown Struct " + kind);
    }

    public abstract boolean compatibleWith(StructImpl other);

    public abstract boolean assignableTo(StructImpl dest);
}

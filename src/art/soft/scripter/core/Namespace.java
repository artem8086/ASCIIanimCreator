package art.soft.scripter.core;

import java.util.HashMap;

/**
 *
 * @author Артём Святоха
 */
public class Namespace extends HashMap<String, Object> implements TableAccessor {

    private Namespace parentSpace;

    public Namespace() {
        super();
    }

    Namespace(Namespace parent) {
        super();
        parentSpace = parent;
    }

    Namespace(Namespace copy, boolean unused) {
        super(copy);
    }

    public Namespace(Namespace parent, Object thisPtr) {
        super();
        parentSpace = parent;
        put(ScriptCore.THIS_POINTER, thisPtr);
    }

    public Namespace getParent() {
        return getParentSpace();
    }

    public void setToParent(String name, Object value) {
        Namespace temp = this;
        while (temp.parentSpace != null && !temp.containsKey(name)) {
            temp = temp.parentSpace;
        }
        if (value == null) {
            temp.remove(name);
        } else {
            temp.put(name, value);
        }
    }

    public Namespace findVarNamespace(String name) {
        Namespace temp = this;
        while (temp.parentSpace != null && !temp.containsKey(name)) {
            temp = temp.parentSpace;
        }
        return temp;
    }

    @Override
    public void set(String name, Object value) {
        if (value == null) {
            remove(name);
        } else {
            put(name, value);
        }
    }

    public Namespace getParentSpace() {
        return parentSpace;
    }

    public void setParentSpace(Namespace parentSpace) {
        this.parentSpace = parentSpace;
    }

    @Override
    public Object get(String name) {
        return super.get(name);
    }
}

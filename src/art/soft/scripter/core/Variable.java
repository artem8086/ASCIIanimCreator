package art.soft.scripter.core;

/**
 *
 * @author Артём Святоха
 */
public class Variable {

    private final TableAccessor table;
    private final String name;

    public Variable(TableAccessor table, String name) {
        this.table = table;
        this.name = name;
    }

    public Variable(String name) {
        this.table = null;
        this.name = name;
    }

    public TableAccessor getNamespace(ScriptCore core) {
        if (table == null) return core.getLocalNamespace().findVarNamespace(name);
        return table;
    }

    public String getName() {
        return name;
    }

    public Object get(ScriptCore core) {
        if (table == null) return core.getVar(name);
        return table.get(name);
    }

    public void set(ScriptCore core, Object value) {
        if (table == null) {
            core.setVar(name, value);
        } else {
            table.set(name, value);
        }
    }

    @Override
    public String toString() {
        return '@' + name;
    }
}

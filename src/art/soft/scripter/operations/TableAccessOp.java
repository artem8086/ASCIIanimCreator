package art.soft.scripter.operations;

import art.soft.scripter.core.ScriptCore;
import art.soft.scripter.core.TableAccessor;
import art.soft.scripter.core.Variable;

/**
 *
 * @author Артём Святоха
 */
public class TableAccessOp  extends Operation<Variable> implements VariableReturnOp {

    private final Operation table;
    private final Operation accessor;

    public TableAccessOp(Operation table, Operation accesor) {
        this.table = table;
        this.accessor = accesor;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        return false;
    }

    @Override
    public Variable exec(ScriptCore core) {
        Object tab = core.getValue(table.exec(core));
        String field = core.toString(accessor.exec(core));
        if (tab instanceof TableAccessor) {
            return new Variable((TableAccessor) tab, field);
        }
        throw new RuntimeException("try to apply dot operator to non table accessor");
    }
}


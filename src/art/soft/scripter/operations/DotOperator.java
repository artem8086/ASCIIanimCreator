package art.soft.scripter.operations;

import art.soft.scripter.core.ScriptCore;
import art.soft.scripter.core.Variable;
import art.soft.scripter.core.TableAccessor;

/**
 *
 * @author Артём Святоха
 */
public class DotOperator extends Operation<Variable> implements VariableReturnOp {

    private final Operation table;
    private final String field;

    public DotOperator(Operation table, String field) {
        this.table = table;
        this.field = field;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        return false;
    }

    @Override
    public Variable exec(ScriptCore core) {
        Object tab = core.getValue(table.exec(core));
        if (tab instanceof TableAccessor) {
            return new Variable((TableAccessor) tab, field);
        }
        throw new RuntimeException("try to apply dot operator to non table accessor");
    }
}

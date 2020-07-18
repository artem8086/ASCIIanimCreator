package art.soft.scripter.operations;

import art.soft.scripter.core.ScriptCore;
import art.soft.scripter.core.Variable;

/**
 *
 * @author Артём Святоха
 */
public class VariableValueOp extends Operation<Variable> implements VariableReturnOp {

    private final Variable variable;

    public VariableValueOp(Variable variable) {
        this.variable = variable;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        return false;
    }

    @Override
    public Variable exec(ScriptCore core) {
        return variable;
    }
}
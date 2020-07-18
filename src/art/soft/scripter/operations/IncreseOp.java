package art.soft.scripter.operations;

import art.soft.scripter.core.ScriptCore;
import art.soft.scripter.core.Variable;

/**
 *
 * @author Артём Святоха
 */
public class IncreseOp extends Operation<Variable> implements VariableReturnOp {
    
    public final VariableReturnOp operand;
    
    public IncreseOp(VariableReturnOp operand) {
        this.operand = operand;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        return false;
    }

    @Override
    public Variable exec(ScriptCore core) {
        Object var = operand.exec(core);
        if (var instanceof Variable) {
            Number val = core.toNumber(var);
            if (val instanceof Double) {
                ((Variable) var).set(core, val.doubleValue() + 1);
            } else {
                ((Variable) var).set(core, val.intValue() + 1);
            }
            return (Variable) var;
        }
        throw new RuntimeException("try to increse static value");
    }
}

package art.soft.scripter.operations;

import art.soft.scripter.core.ScriptCore;
import art.soft.scripter.core.Variable;

/**
 *
 * @author Артём Святоха
 */
public class AssignOp extends Operation<Variable> implements VariableReturnOp {

    private final VariableReturnOp assignVar;
    private final Operation operand;
    
    
    public AssignOp(VariableReturnOp assignVar, Operation operand) {
        this.assignVar = assignVar;
        this.operand = operand;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        return false;
    }

    @Override
    public Variable exec(ScriptCore core) {
        Object var = assignVar.exec(core);
        if (var instanceof Variable) {
            ((Variable) var).set(core, core.getValue(operand.exec(core)));
            return (Variable) var;
        }
        throw new RuntimeException("can't assign to static value");
    }
}

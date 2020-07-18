package art.soft.scripter.operations;

import art.soft.scripter.core.BaseOperations.TwoArgsOperation;
import art.soft.scripter.core.ScriptCore;
import art.soft.scripter.core.Variable;

/**
 *
 * @author Артём Святоха
 */
public class AssignDoOp extends Operation<Variable> implements VariableReturnOp {

    private final TwoArgsOperation operation;
    private final VariableReturnOp assignVar;
    private final Operation operand;
    
    
    public AssignDoOp(VariableReturnOp assignVar, Operation operand, TwoArgsOperation operation) {
        this.assignVar = assignVar;
        this.operation = operation;
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
            ((Variable) var).set(core, operation.doOp(core.getValue(var),
                    core.getValue(operand.exec(core))));
            return (Variable) var;
        }
        throw new RuntimeException("can't assign to static value");
    }
}

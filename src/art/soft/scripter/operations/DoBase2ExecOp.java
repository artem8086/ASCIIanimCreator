package art.soft.scripter.operations;

import art.soft.scripter.core.BaseOperations;
import art.soft.scripter.core.ScriptCore;

/**
 *
 * @author Артём Святоха
 */
public class DoBase2ExecOp extends Operation<Object> {

    private final BaseOperations.TwoArgsOperation operation;
    private final Operation operand1;
    private final Operation operand2;
    
    
    public DoBase2ExecOp(Operation operand1, Operation operand2, BaseOperations.TwoArgsOperation operation) {
        this.operation = operation;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        return operand1.isConstOp(core) && operand2.isConstOp(core);
    }

    @Override
    public Object exec(ScriptCore core) {
        return operation.doOp(operand1, operand2);
    }
}


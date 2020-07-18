package art.soft.scripter.operations;

import art.soft.scripter.core.BaseOperations.TwoArgsOperation;
import art.soft.scripter.core.ScriptCore;

/**
 *
 * @author Артём Святоха
 */
public class DoBase2ArgOp extends Operation<Object> {

    private final TwoArgsOperation operation;
    private final Operation operand1;
    private final Operation operand2;
    
    
    public DoBase2ArgOp(Operation operand1, Operation operand2, TwoArgsOperation operation) {
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
        return operation.doOp(core.getValue(operand1.exec(core)), core.getValue(operand2.exec(core)));
    }
}

package art.soft.scripter.operations;

import art.soft.scripter.core.BaseOperations.UnaryOperation;
import art.soft.scripter.core.ScriptCore;

/**
 *
 * @author Артём Святоха
 */
public class DoBaseUnaryOp extends Operation<Object> {

    private final UnaryOperation operation;
    private final Operation operand;
    
    public DoBaseUnaryOp(Operation operand, UnaryOperation operation) {
        this.operation = operation;
        this.operand = operand;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        return operand.isConstOp(core);
    }

    @Override
    public Object exec(ScriptCore core) {
        return operation.doOp(core.getValue(operand.exec(core)));
    }
}

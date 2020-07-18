package art.soft.scripter.operations;

import art.soft.scripter.core.ScriptCore;

/**
 *
 * @author Артём Святоха
 */
public class TernarIfOp extends Operation<Object> {

    private final Operation condition;
    private final Operation operand1;
    private final Operation operand2;
    
    
    public TernarIfOp(Operation condition, Operation operand1, Operation operand2) {
        this.condition = condition;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        return false;
    }

    @Override
    public Object exec(ScriptCore core) {
        if (core.toBoolean(condition.exec(core))) {
            return operand1.exec(core);
        } else {
            if (operand2 != null) return operand2.exec(core);
        }
        return null;
    }    
}

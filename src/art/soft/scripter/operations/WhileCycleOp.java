package art.soft.scripter.operations;

import art.soft.scripter.core.ScriptCore;

/**
 *
 * @author Артём Святоха
 */
public class WhileCycleOp extends Operation<Object> {

    private final Operation condition;
    private final Operation expression;
    
    public WhileCycleOp(Operation condition, Operation expression) {
        this.condition = condition;
        this.expression = expression;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        return false;
    }

    @Override
    public Object exec(ScriptCore core) {
        while (core.toBoolean(condition.exec(core))) {
            core.blockExitType = ScriptCore.BlockExitType.NONE;
            if (expression != null) expression.exec(core);
            if (core.blockExitType == ScriptCore.BlockExitType.BREAK ||
                    core.blockExitType == ScriptCore.BlockExitType.RETURN) {
                break;
            }
        }
        if (core.blockExitType != ScriptCore.BlockExitType.RETURN) {
            core.blockExitType = ScriptCore.BlockExitType.NONE;
        }
        return null;
    }
}

package art.soft.scripter.operations;

import art.soft.scripter.core.ScriptCore;

/**
 *
 * @author Артём Святоха
 */
public class DoWhileOp extends Operation<Object> {

    private final Operation condition;
    private final Operation expression;
    
    public DoWhileOp(Operation expression, Operation condition) {
        this.expression = expression;
        this.condition = condition;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        return false;
    }

    @Override
    public Object exec(ScriptCore core) {
        do {
            core.blockExitType = ScriptCore.BlockExitType.NONE;
            expression.exec(core);
            if (core.blockExitType == ScriptCore.BlockExitType.BREAK ||
                    core.blockExitType == ScriptCore.BlockExitType.RETURN) {
                break;
            }
        } while (core.toBoolean(condition.exec(core)));
        if (core.blockExitType != ScriptCore.BlockExitType.RETURN) {
            core.blockExitType = ScriptCore.BlockExitType.NONE;
        }
        return null;
    }
}

package art.soft.scripter.operations;

import art.soft.scripter.core.ScriptCore;
import java.util.Stack;

/**
 *
 * @author Артём Святоха
 */
public class ForCycleOp extends Operation<Object> {

    private final Operation initilaizer;
    private final Operation condition;
    private final Operation[] postOps;
    private final Operation expression;

    public ForCycleOp(Operation initilaizer, Operation condition,
            Operation[] postOps, Operation expression) {
        this.initilaizer = initilaizer;
        this.condition = condition;
        this.postOps = postOps;
        this.expression = expression;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        return false;
    }

    @Override
    public Object exec(ScriptCore core) {
        core.createLocalNamespace();
        if (initilaizer != null) initilaizer.exec(core);
        while (core.toBoolean(condition.exec(core))) {
            core.blockExitType = ScriptCore.BlockExitType.NONE;
            if (expression != null) expression.exec(core);
            if (core.blockExitType == ScriptCore.BlockExitType.BREAK ||
                    core.blockExitType == ScriptCore.BlockExitType.RETURN) {
                break;
            }
            for (Operation op : postOps) {
                op.exec(core);
            }
        }
        if (core.blockExitType != ScriptCore.BlockExitType.RETURN) {
            core.blockExitType = ScriptCore.BlockExitType.NONE;
        }
        core.popLocalNamespace();
        return null;
    }
}

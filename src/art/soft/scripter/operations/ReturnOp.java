package art.soft.scripter.operations;

import art.soft.scripter.core.ScriptCore;
import art.soft.scripter.core.Variable;

/**
 *
 * @author Артём Святоха
 */
public class ReturnOp extends Operation<Object> {

    private final Operation retExpression;

    public ReturnOp(Operation retExpression) {
        this.retExpression = retExpression;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        return false;
    }

    @Override
    public Object exec(ScriptCore core) {
        if (retExpression != null) {
            Object value = retExpression.exec(core);
            if (value instanceof Variable) {
                value = new Variable(((Variable) value).getNamespace(core),
                        ((Variable) value).getName());
            }
            core.returnValue = value;
        } else
        core.returnValue = null;
        core.blockExitType = ScriptCore.BlockExitType.RETURN;
        return null;
    }
}

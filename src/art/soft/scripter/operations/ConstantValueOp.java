package art.soft.scripter.operations;

import art.soft.scripter.core.ScriptCore;

/**
 *
 * @author Артём Святоха
 */
public class ConstantValueOp extends Operation<Object> {

    private final Object value;

    public ConstantValueOp(Object value) {
        this.value = value;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        return true;
    }

    @Override
    public Object exec(ScriptCore core) {
        return value;
    }
    
}

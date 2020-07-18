package art.soft.scripter.operations;

import art.soft.scripter.core.ScriptCore;

/**
 *
 * @author Артём Святоха
 */
public abstract class Operation<T> {

    public abstract boolean isConstOp(ScriptCore core);

    public abstract T exec(ScriptCore core);
}

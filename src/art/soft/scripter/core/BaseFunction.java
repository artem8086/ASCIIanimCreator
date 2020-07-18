package art.soft.scripter.core;

/**
 *
 * @author Артём Святоха
 */
public interface BaseFunction {

    public Object exec(Object[] args, ScriptCore core, Object thisPtr);
}

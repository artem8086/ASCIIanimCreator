package art.soft.scripter.operations;

import art.soft.scripter.core.BaseFunction;
import art.soft.scripter.core.ScriptCore;

/**
 *
 * @author Артём Святоха
 */
public class ExtendsOp extends Operation<Object> implements VariableReturnOp {

    private final Operation functionVar;
    private final Operation[] arguments;
    
    
    public ExtendsOp(FunctionCallOp function) {
        this.functionVar = function.getFunctionVar();
        this.arguments = function.getArguments();
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        return false;
    }

    @Override
    public Object exec(ScriptCore core) {
        Object function = core.getValue(functionVar.exec(core));;
        if (!(function instanceof BaseFunction)) {
            throw new RuntimeException("can't exec no executable value");
        }
        //
        Object[] args;
        if (arguments != null) {
            args = new Object[arguments.length];
            for (int i = 0; i < args.length; i ++) {
                args[i] = core.getValue(arguments[i].exec(core));
            }
        } else args = null;
        //
        core.returnValue = null;
        Object ret = ((BaseFunction) function).exec(args, core, core.getLocalNamespace().get(ScriptCore.THIS_POINTER));
        //
        return ret == null ? core.returnValue : ret;
    }
    
}

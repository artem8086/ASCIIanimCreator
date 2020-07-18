package art.soft.scripter.operations;

import art.soft.scripter.core.BaseFunction;
import art.soft.scripter.core.ScriptCore;
import art.soft.scripter.core.TableAccessor;
import art.soft.scripter.core.Variable;

/**
 *
 * @author Артём Святоха
 */
public class FunctionCallOp extends Operation<Object> implements VariableReturnOp {

    private final Operation functionVar;
    private final Operation[] arguments;
    
    
    public FunctionCallOp(Operation functionVar, Operation[] arguments) {
        this.functionVar = functionVar;
        this.arguments = arguments;
    }

    public Operation getFunctionVar() {
        return functionVar;
    }

    public Operation[] getArguments() {
        return arguments;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        return false;
    }

    @Override
    public Object exec(ScriptCore core) {
        Object var = functionVar.exec(core);
        Object function = core.getValue(var);
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
        TableAccessor funcSpace;
        if (var instanceof Variable) {
            funcSpace = ((Variable) var).getNamespace(core);
        } else {
            funcSpace = core.getLocalNamespace();
        }
        //core.createLocalNamespace().set(ScriptCore.THIS_POINTER, funcSpace);
        //
        core.returnValue = null;
        Object ret = ((BaseFunction) function).exec(args, core, funcSpace);
        //
        return ret == null ? core.returnValue : ret;
    }
}

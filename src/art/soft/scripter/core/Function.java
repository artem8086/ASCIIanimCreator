package art.soft.scripter.core;

import art.soft.scripter.operations.Operation;

/**
 *
 * @author Артём Святоха
 */
public class Function implements BaseFunction {

    private final String[] argsName;
    private final Operation[] operations;

    public Function(String[] argsName, Operation[] operations) {
        this.argsName = argsName;
        this.operations = operations;
    }

    @Override
    public Object exec(Object[] args, ScriptCore core, Object thisPtr) {
        {
            Namespace lSpace = core.createLocalNamespace();
            lSpace.put(ScriptCore.THIS_POINTER, thisPtr);
            int i = 0;
            if (args != null) {
                for (; i < argsName.length && i < args.length; i ++) {
                    lSpace.put(argsName[i], args[i]);
                }
                if (i < args.length) {
                    int j = 0;
                    Namespace tArgs = new Namespace();
                    do {
                        tArgs.set(Integer.toString(j++), args[i++]);
                    } while (i < args.length);
                    lSpace.set(ScriptCore.ARGUMENTS_VAR, tArgs);
                }
            }
            for (; i < argsName.length; i ++) {
                lSpace.put(argsName[i], null);
            }
        }
        for (Operation op : operations) {
            op.exec(core);
            if (core.blockExitType == ScriptCore.BlockExitType.RETURN) break;
        }
        core.popLocalNamespace();
        core.blockExitType = ScriptCore.BlockExitType.NONE;
        return null;
    }

    @Override
    public String toString() {
        String func = "function(";
        for (int i = 0; i != argsName.length;) {
            func += argsName[i];
            i ++;
            if (i == argsName.length) break;
            else func += ", ";
        }
        func += ")";
        return func;
    }
}

package art.soft.scripter.operations;

import art.soft.scripter.core.Namespace;
import art.soft.scripter.core.ScriptCore;
import art.soft.scripter.core.Variable;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Артём Святоха
 */
public class ForEachOp extends Operation<Object> {

    private final VariableReturnOp variable;
    private final Operation container;
    private final Operation expression;

    public ForEachOp(VariableReturnOp variable, Operation container,
            Operation expression) {
        this.variable = variable;
        this.container = container;
        this.expression = expression;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        return false;
    }

    @Override
    public Object exec(ScriptCore core) {
        core.createLocalNamespace();
        Object var = variable.exec(core);
        if (!(var instanceof Variable)) {
            throw new RuntimeException("can't do forEach to static value");
        }
        Object table = core.getValue(container.exec(core));
        if (!(table instanceof Namespace)) {
            throw new RuntimeException("can't do forEach to non table container");
        }
        Iterator<Map.Entry<String, Object>> it = ((Namespace) table).entrySet().iterator();
        while (it.hasNext()) {
            core.blockExitType = ScriptCore.BlockExitType.NONE;
            Map.Entry<String, Object> entry = it.next();
            Namespace lSpace = new Namespace();
            lSpace.set(ScriptCore.KEY_FOR_EACH, entry.getKey());
            lSpace.set(ScriptCore.VALUE_FOR_EACH, entry.getValue());
            ((Variable) var).set(core, lSpace);
            if (expression != null) expression.exec(core);
            if (core.blockExitType == ScriptCore.BlockExitType.BREAK ||
                    core.blockExitType == ScriptCore.BlockExitType.RETURN) {
                break;
            }
        }
        if (core.blockExitType != ScriptCore.BlockExitType.RETURN) {
            core.blockExitType = ScriptCore.BlockExitType.NONE;
        }
        core.popLocalNamespace();
        return null;
    }
}

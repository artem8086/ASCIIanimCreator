package art.soft.scripter.operations;

import art.soft.scripter.core.Namespace;
import art.soft.scripter.core.ScriptCore;
import java.util.Stack;

/**
 *
 * @author Артём Святоха
 */
public class ArrayInitListOp extends Operation<Namespace> {

    private final Operation[] operations;

    public ArrayInitListOp(Operation[] operations) {
        this.operations = operations;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        for (Operation op : operations) {
            if (!op.isConstOp(core)) return false;
        }
        return true;
    }

    @Override
    public Namespace exec(ScriptCore core) {
        Namespace list = new Namespace();
        for (int i = 0; i < operations.length; i ++) {
            Object value = core.getValue(operations[i].exec(core));
            if (value != null) list.set(Integer.toString(i), value);
        }
        return list;
    }
}

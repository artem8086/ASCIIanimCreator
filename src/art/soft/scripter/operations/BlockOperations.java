package art.soft.scripter.operations;

import art.soft.scripter.core.Namespace;
import art.soft.scripter.core.ScriptCore;
import java.util.Stack;

/**
 *
 * @author Артём Святоха
 */
public class BlockOperations extends Operation<Namespace> {

    private final Operation[] operations;

    public BlockOperations(Operation[] operations) {
        this.operations = operations;
    }

    public Operation[] getOperations() {
        return operations;
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
        Namespace lSpace = core.createLocalNamespace();
        for (Operation op : operations) {
            op.exec(core);
            if (core.blockExitType != ScriptCore.BlockExitType.NONE) break;
        }
        core.popLocalNamespace();
        return lSpace;
    }
}

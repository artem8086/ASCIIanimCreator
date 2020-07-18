package art.soft.scripter.operations;

import art.soft.scripter.core.Namespace;
import art.soft.scripter.core.ScriptCore;
import art.soft.scripter.core.Variable;

/**
 *
 * @author Артём Святоха
 */
public class LocalInitilaizerOp extends Operation<Object> implements VariableReturnOp {

    private final String[] nameList;
    private final Operation[] operations;

    public LocalInitilaizerOp(String[] nameList,Operation[] operations) {
        this.operations = operations;
        this.nameList = nameList;
    }

    @Override
    public boolean isConstOp(ScriptCore core) {
        for (Operation op : operations) {
            if (!op.isConstOp(core)) return false;
        }
        return true;
    }

    @Override
    public Object exec(ScriptCore core) {
        Namespace locSpace = core.getLocalNamespace();
        int len = nameList.length;
        Object value = null;
        for (int i = 0; i < len; i ++) {
            if (operations[i] != null) {
                value = core.getValue(operations[i].exec(core));
            } else value = null;
            locSpace.put(nameList[i], value);
        }
        return new Variable(locSpace, nameList[0]);
    }
}

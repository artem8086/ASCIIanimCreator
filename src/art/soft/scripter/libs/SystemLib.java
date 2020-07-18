package art.soft.scripter.libs;

import art.soft.scripter.core.BaseFunction;
import art.soft.scripter.core.BaseLibrary;
import art.soft.scripter.core.ScriptCore;
import java.util.Random;

/**
 *
 * @author Артём Святоха
 */
public class SystemLib extends BaseLibrary {

    private Random random;

    @Override
    public String getLibName() {
        return "system";
    }

    @Override
    public String toString() {
        return "<System Library>";
    }

    @Override
    public void initialize(ScriptCore core) {
        random = new Random();

        super.set("print", new BaseFunction() {
            @Override
            public Object exec(Object[] args, ScriptCore core, Object thisPtr) {
                if (args != null) for (Object var : args) {
                    core.getOutStream().print(core.toString(var));
                }
                return null;
            }
            
            @Override
            public String toString() {
                return "function(...)";
            }
        });

        super.set("println", new BaseFunction() {
            @Override
            public Object exec(Object[] args, ScriptCore core, Object thisPtr) {
                if (args != null) for (Object var : args) {
                    core.getOutStream().print(core.toString(var));
                }
                core.getOutStream().println();
                return null;
            }
            
            @Override
            public String toString() {
                return "function(...)";
            }
        });

        super.set("random", new BaseFunction() {
            @Override
            public Object exec(Object[] args, ScriptCore core, Object thisPtr) {
                if (args != null && args.length != 0) {
                    if (args.length == 1) {
                        return random.nextInt(core.toNumber(args[0]).intValue());
                    } else {
                        int min = core.toNumber(args[0]).intValue();
                        return min + random.nextInt(core.toNumber(args[1]).intValue() - min);
                    }
                }
                return random.nextDouble();
            }

            @Override
            public String toString() {
                return "function(...)";
            }
        });
    }

    @Override
    public void set(String name, Object value) {}
}

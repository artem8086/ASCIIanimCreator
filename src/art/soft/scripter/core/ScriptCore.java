package art.soft.scripter.core;

import art.soft.scripter.operations.Operation;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashSet;

/**
 *
 * @author Артём Святоха
 */
public class ScriptCore {

    public PrintStream getOutStream() {
        return out;
    }

    public void setOutStream(PrintStream out) {
        this.out = out;
    }

    public PrintStream getErrorStream() {
        return err;
    }

    public void setErrorStream(PrintStream err) {
        this.err = err;
    }

    public static enum BlockExitType {
        NONE, BREAK, CONTINUE, RETURN
    };

    public static final String THIS_POINTER = "this";
    public static final String NULL_POINTER = "null";
    public static final String ARGUMENT_LIST_NAME = "args";
    public static final String BOOLEAN_TRUE = "true";
    public static final String BOOLEAN_FALSE = "false";
    public static final String ARGUMENTS_VAR = "args";
    public static final String KEY_FOR_EACH = "key";
    public static final String VALUE_FOR_EACH = "value";

    public final String readStringFromFile(File file) throws IOException {
        StringBuilder output = new StringBuilder((int) file.length());
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
        char[] buffer = new char[256];
        while (true) {
            int length = reader.read(buffer);
            if (length == -1) {
                break;
            }
            output.append(buffer, 0, length);
        }
        return output.toString();
    }

    public static void Init() {
        Parser.Init();
    }

    public final BaseOperations baseOps;

    private Operation[] operations;

    private final Namespace global;
    private Namespace local;

    public ScriptCore() {
        baseOps = new BaseOperations(this);
        global = new Namespace();
        resetScripEnvironment();
    }

    public ScriptCore(ScriptCore core) {
        baseOps = core.baseOps;
        global = core.global;
        resetScripEnvironment();
        operations = core.operations;
        out = core.out;
        err = core.err;
    }

    private PrintStream out;
    private PrintStream err;

    public final void resetScripEnvironment() {
        global.clear();
        global.set("global", global);
        local = global;
        returnValue = null;
        operations = null;
    }

    Operation[] getOperations() {
        return operations;
    }

    private FileAccessor fileAccessor = null;
    private String currentFileName;
    private final HashSet<String> openFiles = new HashSet<String>();

    public void setFileAccesor(FileAccessor fileAccessor) {
        this.fileAccessor = fileAccessor;
    }
        
    public void loadScriptFromFile(String fileName) {
        operations = null;
        File file;
        if (fileAccessor != null) {
            file = fileAccessor.getScriptFileName(fileName);
        } else {
            file = new File(fileName);
        }
        fileName = file.getAbsolutePath();
        if (openFiles.add(fileName)) {
            String temp = currentFileName;
            currentFileName = fileName;
            try {
                loadScript(readStringFromFile(file));
            } catch (IOException ex) {
                err.println("[SCRIPT] File load error: " + fileName);
            }
            currentFileName = temp;
            openFiles.remove(fileName);
        }
    }

    private void printErrorFileName() {
        if (currentFileName != null) {
            err.print("[SCRIPT] file: ");
            err.println(currentFileName);
        }
    }

    public void loadScript(String script) {
        ScriptBuilder builder = new ScriptBuilder(this);
        builder.loadScript(script);
        operations = null;
        try {
            operations = builder.buildScript();
        } catch (ParserException ex) {
            printErrorFileName();
            err.println("Parser error: " + ex.getMessage());
        } catch (RuntimeException re) {
            printErrorFileName();
            Parser parser = builder.getParser();
            err.println("Optimazation error: " + parser.getErrorMessage(re.getMessage()));
        }
    }

    public void loadLibrary(BaseLibrary library) {
        global.set(library.getLibName(), library);
        library.initialize(this);
    }

    public BlockExitType blockExitType;
    public Object returnValue;

    public Namespace getGlobalNamespace() {
        return global;
    }

    public Namespace getLocalNamespace() {
        return local;
    }

    public Namespace createLocalNamespace() {
        return local = new Namespace(local);
    }

    public void popLocalNamespace() {
        Namespace temp = local;
        local = temp.getParentSpace();
        temp.setParentSpace(null);
    }

    public Object getVar(String name) {
        Namespace temp = local;
        Object value = null;
        while (temp != null && (value = temp.get(name)) == null) {
            temp = temp.getParentSpace();
        }
        return value;
    }

    public void setVar(String name, Object value) {
        local.setToParent(name, value);
    }

    public String execToString(Object[] args) {
        String val = "";
        local = global;
        try {
            if (operations != null) {
                if (args != null && args.length != 0) {
                    Namespace arguments = new Namespace();
                    for (int i = args.length - 1; i >= 0; i--) {
                        arguments.set(Integer.toString(i), args[i]);
                    }
                    global.set(ARGUMENTS_VAR, arguments);
                } else {
                    global.remove(ARGUMENTS_VAR);
                }
                blockExitType = BlockExitType.NONE;
                for (Operation op : operations) {
                    val += toString(op.exec(this)) + '\n';
                }
            } else val = NULL_POINTER;
        } catch (RuntimeException re) {
            err.println("[SCRIPT] Runtime error: " + re.getMessage());
        } catch (StackOverflowError se) {
            err.println("[SCRIPT] Stack overflow error.");
        }
        return val;
    }

    public void execWithEcho(Object[] args) {
        out.println(execToString(args));
    }

    public void exec(Object[] args) {
        try {
            if (operations != null) {
                if (args != null && args.length != 0) {
                    Namespace arguments = new Namespace();
                    int i = 0;
                    for (Object obj : args) {
                        arguments.set(Integer.toString(i), obj);
                    }
                    global.set(ARGUMENTS_VAR, arguments);
                } else {
                    global.remove(ARGUMENTS_VAR);
                }
                blockExitType = BlockExitType.NONE;
                for (Operation op : operations) {
                    op.exec(this);
                }
            }
        } catch (RuntimeException re) {
            err.println("[SCRIPT] Runtime error: " + re.getMessage());
            re.printStackTrace();
        } catch (StackOverflowError se) {
            err.println("[SCRIPT] Stack overflow error.");
        }
    }

    public Object execFunction(BaseFunction function, Object[] arguments, Object thisPointer) {
        if (function != null) {
            try {
                ((BaseFunction) function).exec(arguments, this, thisPointer);
                return returnValue;
            } catch (RuntimeException re) {
                err.println("[SCRIPT] Runtime error: " + re.getMessage());
                re.printStackTrace();
            } catch (StackOverflowError se) {
                err.println("[SCRIPT] Stack overflow error.");
            }
        }
        return null;
    }

        // Функции преобразования типов
    public Number toNumber(Object value) {
        if (value == null) return null; //throw new RuntimeException("can't convert null pointer to number");
        if (value instanceof Variable) {
            value = ((Variable) value).get(this);
            if (value == null) return null; //throw new RuntimeException("can't convert uninitilazed variable to number");
        }
        if (value instanceof Number) {
            return (Number) value;
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        }
        if (value instanceof String) {
            try {
                return Integer.decode((String) value);
            } catch (NumberFormatException nfe) {}
            try {
                return Double.valueOf((String) value);
            } catch (NumberFormatException nfe) {
                //throw new RuntimeException("incorrect string format. Can't convert to number");
            }
        }
        //throw new RuntimeException("incorrect type. Can't convert to number");
        return null;
    }

    public boolean toBoolean(Object value) {
        if (value instanceof Variable) {
            value = ((Variable) value).get(this);
        }
        if (value == null) return false;
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return !value.equals(0);
        }
        if (value instanceof String) {
            return value.equals(BOOLEAN_TRUE);
        }
        return true;
    }

    public String toString(Object value) {
        if (value instanceof Variable) {
            value = ((Variable) value).get(this);
        }
        return value == null ? NULL_POINTER : value.toString();
    }

    public Object getValue(Object value) {
        if (value instanceof Variable) {
            return ((Variable) value).get(this);
        }
        return value;
    }
}

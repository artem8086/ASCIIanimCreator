package art.soft.scripter.core;

/**
 *
 * @author Артём Святоха
 */
public abstract class BaseLibrary extends Namespace {

    public abstract String getLibName();

    public abstract void initialize(ScriptCore core);

    protected void loadLibrary(ScriptCore core, BaseLibrary library) {
        set(library.getLibName(), library);
        library.initialize(core);
    }
}

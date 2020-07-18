package art.soft.scripter.core;

/**
 *
 * @author Артём Святоха
 */
public interface TableAccessor {

    public Object get(String name);

    public void set(String name, Object value);
}

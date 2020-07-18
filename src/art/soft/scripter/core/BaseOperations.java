package art.soft.scripter.core;

import art.soft.scripter.operations.Operation;

/**
 *
 * @author Артём Святоха
 */
public class BaseOperations {

    public interface TwoArgsOperation {

        public Object doOp(Object value1, Object value2);
    }

    private final ScriptCore core;

    public BaseOperations(ScriptCore core) {
        this.core = core;
    }

    public final Operation<Object> BreakOp = new Operation<Object>() {

        @Override
        public boolean isConstOp(ScriptCore core) {
            return false;
        }

        @Override
        public Object exec(ScriptCore core) {
            core.blockExitType = ScriptCore.BlockExitType.BREAK;
            return null;
        }
    };

    public final Operation<Object> ContinueOp = new Operation<Object>() {

        @Override
        public boolean isConstOp(ScriptCore core) {
            return false;
        }

        @Override
        public Object exec(ScriptCore core) {
            core.blockExitType = ScriptCore.BlockExitType.CONTINUE;
            return null;
        }
    };

    public final TwoArgsOperation add = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            if (value1 instanceof Namespace || value2 instanceof Namespace) {
                Namespace value = new Namespace();
                if (value1 != null) value.putAll((Namespace) value1);
                if (value2 != null) value.putAll((Namespace) value2);
                return value;
            }
            if (value1 == null) {
                throw new RuntimeException("Can't done operation with null pointer");
            }
            if (value1 instanceof String) {
                return ((String) value1) + core.toString(value2);
            }
            if (value2 == null) {
                throw new RuntimeException("Can't done operation with null pointer");
            }
            if (value1 instanceof Number) {
                if (value1 instanceof Double || value2 instanceof Double) {
                    return core.toNumber(value1).doubleValue() + core.toNumber(value2).doubleValue();
                } else {
                    return core.toNumber(value1).intValue() + core.toNumber(value2).intValue();
                }
            }
            throw new RuntimeException("incorrect type. Can't done operation");
        }
    };

    public final TwoArgsOperation sub = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Can't done operation with null pointer");
            }
            if (value1 instanceof Number) {
                if (value1 instanceof Double || value2 instanceof Double) {
                    return core.toNumber(value1).doubleValue() - core.toNumber(value2).doubleValue();
                } else {
                    return core.toNumber(value1).intValue() - core.toNumber(value2).intValue();
                }
            }
            throw new RuntimeException("incorrect type. Can't done operation");
        }
    };

    public final TwoArgsOperation mul = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            if (value1 == null || value2 == null) throw new RuntimeException("Can't done operation with null pointer");
            if (value1 instanceof Number) {
                if (value1 instanceof Double || value2 instanceof Double) {
                    return core.toNumber(value1).doubleValue() * core.toNumber(value2).doubleValue();
                } else {
                    return core.toNumber(value1).intValue() * core.toNumber(value2).intValue();
                }
            }
            throw new RuntimeException("incorrect type. Can't done operation");
        }
    };

    public final TwoArgsOperation div = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Can't done operation with null pointer");
            }
            if (value1 instanceof Number) {
                if (value1 instanceof Double || value2 instanceof Double) {
                    return core.toNumber(value1).doubleValue() / core.toNumber(value2).doubleValue();
                } else {
                    /*int val2 = core.toNumber(value2).intValue();
                    if (val2 == 0) {
                        throw new RuntimeException("divizion by zero");
                    }*/
                    return core.toNumber(value1).intValue() / core.toNumber(value2).intValue();
                }
            }
            throw new RuntimeException("incorrect type. Can't done operation");
        }
    };

    public final TwoArgsOperation mod = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Can't done operation with null pointer");
            }
            if (value1 instanceof Number) {
                if (value1 instanceof Double || value2 instanceof Double) {
                    return core.toNumber(value1).doubleValue() % core.toNumber(value2).intValue();
                } else {
                    return core.toNumber(value1).intValue() % core.toNumber(value2).intValue();
                }
            }
            throw new RuntimeException("incorrect type. Can't done operation");
        }
    };

    public final TwoArgsOperation or = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            if (value1 == null) {
                throw new RuntimeException("Can't done operation with null pointer");
            }
            if (value1 instanceof Boolean) {
                return ((Boolean) value1) | core.toBoolean(value2);
            }
            if (value2 == null) throw new RuntimeException("Can't done operation with null pointer");
            if (value1 instanceof Number) {
                return ((Number) value1).intValue() | core.toNumber(value2).intValue();
            }
            throw new RuntimeException("incorrect type. Can't done operation");
        }
    };

    public final TwoArgsOperation and = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            if (value1 == null) {
                throw new RuntimeException("Can't done operation with null pointer");
            }
            if (value1 instanceof Boolean) {
                return ((Boolean) value1) & core.toBoolean(value2);
            }
            if (value2 == null) throw new RuntimeException("Can't done operation with null pointer");
            if (value1 instanceof Number) {
                return ((Number) value1).intValue() & core.toNumber(value2).intValue();
            }
            throw new RuntimeException("incorrect type. Can't done operation");
        }
    };

    public final TwoArgsOperation xor = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            if (value1 == null) {
                throw new RuntimeException("Can't done operation with null pointer");
            }
            if (value1 instanceof Boolean) {
                return ((Boolean) value1) ^ core.toBoolean(value2);
            }
            if (value2 == null) throw new RuntimeException("Can't done operation with null pointer");
            if (value1 instanceof Number) {
                return ((Number) value1).intValue() ^ core.toNumber(value2).intValue();
            }
            throw new RuntimeException("incorrect type. Can't done operation");
        }
    };

    public final TwoArgsOperation shr = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Can't done operation with null pointer");
            }
            if (value1 instanceof Number) {
                return ((Number) value1).intValue() >> core.toNumber(value2).intValue();
            }
            throw new RuntimeException("incorrect type. Can't done operation");
        }
    };

    public final TwoArgsOperation shl = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Can't done operation with null pointer");
            }
            if (value1 instanceof Number) {
                return ((Number) value1).intValue() << core.toNumber(value2).intValue();
            }
            throw new RuntimeException("incorrect type. Can't done operation");
        }
    };

    public final TwoArgsOperation bool_or = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            if (core.toBoolean(((Operation) value1).exec(core))) return true;
            return core.toBoolean(((Operation) value2).exec(core));
        }
    };

    public final TwoArgsOperation bool_and = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            if (!core.toBoolean(((Operation) value1).exec(core))) return false;
            return core.toBoolean(((Operation) value2).exec(core));
        }
    };

    public final TwoArgsOperation bool_xor = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            return core.toBoolean(value1) ^ core.toBoolean(value2);
        }
    };

    public final TwoArgsOperation equls = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            if (value1 == null) return value2 == null;
            return value1.equals(value2);
        }
    };

    public final TwoArgsOperation not_equls = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            if (value1 == null) return value2 != null;
            return !value1.equals(value2);
        }
    };

    public final TwoArgsOperation ref_equls = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            return value1 == value2;
        }
    };

    public final TwoArgsOperation ref_not_equls = new TwoArgsOperation() {

        @Override
        public Object doOp(Object value1, Object value2) {
            return value1 != value2;
        }
    };

    public final TwoArgsOperation bigger = new TwoArgsOperation() { // >

        @Override
        public Object doOp(Object value1, Object value2) {
            if (value1 == null || value2 == null) throw new RuntimeException("Can't done operation with null pointer");
            if (value1 instanceof Number) {
                if (value1 instanceof Double || value2 instanceof Double) {
                    return core.toNumber(value1).doubleValue() > core.toNumber(value2).doubleValue();
                } else {
                    return core.toNumber(value1).intValue() > core.toNumber(value2).intValue();
                }
            }
            throw new RuntimeException("incorrect type. Can't done operation");
        }
    };

    public final TwoArgsOperation bigger_equal = new TwoArgsOperation() { // >=

        @Override
        public Object doOp(Object value1, Object value2) {
            if (value1 == null || value2 == null) throw new RuntimeException("Can't done operation with null pointer");
            if (value1 instanceof Number) {
                if (value1 instanceof Double || value2 instanceof Double) {
                    return core.toNumber(value1).doubleValue() >= core.toNumber(value2).doubleValue();
                } else {
                    return core.toNumber(value1).intValue() >= core.toNumber(value2).intValue();
                }
            }
            throw new RuntimeException("incorrect type. Can't done operation");
        }
    };

    public final TwoArgsOperation smaller = new TwoArgsOperation() { // <

        @Override
        public Object doOp(Object value1, Object value2) {
            if (value1 == null || value2 == null) throw new RuntimeException("Can't done operation with null pointer");
            if (value1 instanceof Number) {
                if (value1 instanceof Double || value2 instanceof Double) {
                    return core.toNumber(value1).doubleValue() < core.toNumber(value2).doubleValue();
                } else {
                    return core.toNumber(value1).intValue() < core.toNumber(value2).intValue();
                }
            }
            throw new RuntimeException("incorrect type. Can't done operation");
        }
    };

    public final TwoArgsOperation smaller_equal = new TwoArgsOperation() { // <=

        @Override
        public Object doOp(Object value1, Object value2) {
            if (value1 == null || value2 == null) throw new RuntimeException("Can't done operation with null pointer");
            if (value1 instanceof Number) {
                if (value1 instanceof Double || value2 instanceof Double) {
                    return core.toNumber(value1).doubleValue() <= core.toNumber(value2).doubleValue();
                } else {
                    return core.toNumber(value1).intValue() <= core.toNumber(value2).intValue();
                }
            }
            throw new RuntimeException("incorrect type. Can't done operation");
        }
    };

    public interface UnaryOperation {

        public Object doOp(Object value);
    }

    public final UnaryOperation minus = new UnaryOperation() {

        @Override
        public Object doOp(Object value) {
            Number num = core.toNumber(value);
            if (num instanceof Double) {
                return - num.doubleValue();
            } else {
                return - num.intValue();
            }
        }
    };

    public final UnaryOperation plus = new UnaryOperation() {

        @Override
        public Object doOp(Object value) {
            if (value instanceof Namespace) {
                return new Namespace((Namespace) value, true);
            }
            return core.toNumber(value);
        }
    };

    public final UnaryOperation not = new UnaryOperation() {

        @Override
        public Object doOp(Object value) {
            if (value instanceof Boolean) {
                return !((Boolean) value);
            }
            if (value instanceof Number) {
                return ~ ((Number) value).intValue();
            }
            throw new RuntimeException("incorrect type. Can't done operation");
        }
    };

    public final UnaryOperation bool_not = new UnaryOperation() {

        @Override
        public Object doOp(Object value) {
            return !core.toBoolean(value);
        }
    };

    public final UnaryOperation sizeof = new UnaryOperation() {

        @Override
        public Object doOp(Object value) {
            if (value == null) return 0;
            if (value instanceof String) {
                return ((String) value).length();
            }
            if (value instanceof Namespace) {
                return ((Namespace) value).size();
            }
            return 1;
        }
    };

    public final UnaryOperation is_number = new UnaryOperation() {

        @Override
        public Object doOp(Object value) {
            return value instanceof Number;
        }
    };

    public final UnaryOperation is_integer = new UnaryOperation() {

        @Override
        public Object doOp(Object value) {
            return value instanceof Integer;
        }
    };

    public final UnaryOperation is_double = new UnaryOperation() {

        @Override
        public Object doOp(Object value) {
            return value instanceof Double;
        }
    };

    public final UnaryOperation is_boolean = new UnaryOperation() {

        @Override
        public Object doOp(Object value) {
            return value instanceof Boolean;
        }
    };

    public final UnaryOperation is_string = new UnaryOperation() {

        @Override
        public Object doOp(Object value) {
            return value instanceof String;
        }
    };

    public final UnaryOperation is_table = new UnaryOperation() {

        @Override
        public Object doOp(Object value) {
            return value instanceof TableAccessor;
        }
    };

    public final UnaryOperation is_function = new UnaryOperation() {

        @Override
        public Object doOp(Object value) {
            return value instanceof BaseFunction;
        }
    };
}

package art.soft.scripter.core;

import art.soft.scripter.core.BaseOperations.TwoArgsOperation;
import art.soft.scripter.core.BaseOperations.UnaryOperation;
import art.soft.scripter.core.Parser.Operator;
import art.soft.scripter.core.Parser.ParserState;
import art.soft.scripter.core.Parser.Token;
import art.soft.scripter.operations.ArrayInitListOp;
import art.soft.scripter.operations.AssignDoOp;
import art.soft.scripter.operations.AssignOp;
import art.soft.scripter.operations.BlockOperations;
import art.soft.scripter.operations.ConstantValueOp;
import art.soft.scripter.operations.DecreseOp;
import art.soft.scripter.operations.DoBase2ArgOp;
import art.soft.scripter.operations.DoBase2ExecOp;
import art.soft.scripter.operations.DoBaseUnaryOp;
import art.soft.scripter.operations.DoWhileOp;
import art.soft.scripter.operations.DotOperator;
import art.soft.scripter.operations.ExtendsOp;
import art.soft.scripter.operations.ForCycleOp;
import art.soft.scripter.operations.ForEachOp;
import art.soft.scripter.operations.FunctionCallOp;
import art.soft.scripter.operations.IncreseOp;
import art.soft.scripter.operations.LocalInitilaizerOp;
import art.soft.scripter.operations.NewObjectOp;
import art.soft.scripter.operations.Operation;
import art.soft.scripter.operations.ReturnOp;
import art.soft.scripter.operations.TableAccessOp;
import art.soft.scripter.operations.TernarIfOp;
import art.soft.scripter.operations.VariableReturnOp;
import art.soft.scripter.operations.VariableValueOp;
import art.soft.scripter.operations.WhileCycleOp;
import java.util.Stack;

/**
 *
 * @author Артём Святоха
 */
class ScriptBuilder {

    private final Parser parser;
    private final ScriptCore core;

    private int blockLevel;

    private boolean canBreakAndContinue, canReturn;
    private boolean forEachTwoDots, functionName;
    private boolean dontExit;

    ScriptBuilder(ScriptCore core) {
        parser = new Parser();
        this.core = core;
    }

    public Parser getParser() {
        return parser;
    }

    void loadScript(String script) {
        blockLevel = 0;
        parser.setScript(script);
        currCloseBlock = Token.END;
        functionName = forEachTwoDots = canBreakAndContinue = canReturn = false;
    }

    private Token currCloseBlock;
    private Stack<Operation> currOperations;

    Operation[] buildScript() throws ParserException {
        Stack<Operation> operations = new Stack<Operation>();
        Token token;
        blockLevel ++;
        do {
            dontExit = false;
            currOperations = operations;
            Operation op = expression(true);
            if (op != null) operations.push(op);
            token = parser.getCurrToken();
        } while (token != Token.END && (token != currCloseBlock || dontExit));
        blockLevel --;
        Operation[] temp = new Operation[operations.size()];
        return operations.toArray(temp);
    }

    private Operation optimaze2arg(Operation op1, Operation op2, TwoArgsOperation baseOp) {
        DoBase2ArgOp op = new DoBase2ArgOp(op1, op2, baseOp);
        if (op.isConstOp(core)) {
            return new ConstantValueOp(op.exec(core));
        } else {
            return op;
        }
    }

    private Operation optimaze2exec(Operation op1, Operation op2, TwoArgsOperation baseOp) {
        DoBase2ExecOp op = new DoBase2ExecOp(op1, op2, baseOp);
        if (op.isConstOp(core)) {
            return new ConstantValueOp(op.exec(core));
        } else {
            return op;
        }
    }

    private Operation optimazeUnary(Operation operand, UnaryOperation baseOp) {
        DoBaseUnaryOp op = new DoBaseUnaryOp(operand, baseOp);
        if (op.isConstOp(core)) {
            return new ConstantValueOp(op.exec(core));
        } else {
            return op;
        }
    }

    private Operation namePostOp(Operation op) throws ParserException {
        switch (parser.getCurrToken()) {
            case DOT:
                if (parser.getToken() != Token.NAME) {
                    throw new ParserException("expected name after dot operator", parser);
                }
                String name = parser.getName();
                parser.getToken();
                return namePostOp(new DotOperator(op, name));
            case ARRAY_OPEN: {
                boolean temp = functionName;
                functionName = false;
                Operation accessor = expression(false);
                functionName = temp;
                Operation tableAccess;
                if (accessor.isConstOp(core)) {
                    tableAccess = new DotOperator(op, core.toString(accessor.exec(core)));
                } else {
                    tableAccess = new TableAccessOp(op, accessor);
                }
                if (parser.getCurrToken() != Token.ARRAY_CLOSE) {
                    throw new ParserException(" ']' expected", parser);
                }
                parser.getToken();
                return namePostOp(tableAccess);
            }
            case OPEN_BRACKET: // Вызов функций
                if (!functionName) {
                    Token temp = currCloseBlock;
                    currCloseBlock = Token.CLOSE_BRACKET;
                    Operation[] argsOp = buildScript();
                    currCloseBlock = temp;
                    if (parser.getCurrToken() != Token.CLOSE_BRACKET) {
                        throw new ParserException(" ')' expected", parser);
                    }
                    parser.getToken();
                    if (argsOp.length == 0) argsOp = null;
                    return namePostOp(new FunctionCallOp(op, argsOp));
                } else return op;
            case INCRESE:
                return new IncreseOp(check(op, "can't apply increse operation to static value"));
            case DECRESE:
                return new DecreseOp(check(op, "can't apply decrese operation to static value"));
            default:
                return op;
        }
    }

    private Operation unaryOperator(UnaryOperation op) throws ParserException {
        if (parser.getToken() != Token.OPEN_BRACKET) {
            throw new ParserException(" '(' expected", parser);
        }
        Operation operand = expression(false);
        if (parser.getCurrToken() != Token.CLOSE_BRACKET) {
            throw new ParserException(" ')' expected", parser);
        }
        parser.getToken();
        return optimazeUnary(operand, op);
    }

    private Operation operator(boolean isFirst) throws ParserException {
        switch (parser.getOperator()) {
            case IF: {
                if (!isFirst) throw new ParserException("must be first in expression", parser);
                if (parser.getToken()!= Token.OPEN_BRACKET) {
                    throw new ParserException(" '(' expected", parser);
                }
                Operation condition = expression(false);
                if (parser.getCurrToken() != Token.CLOSE_BRACKET) {
                    throw new ParserException(" ')' expected", parser);
                }
                Operation operand1 = expression(true);
                Operation operand2 = null;
                //
                if (parser.getCurrToken() == Token.OPERATOR
                        && parser.getOperator() == Operator.ELSE) {
                    operand2 = expression(true);
                } else {
                    ParserState pState = parser.saveState();
                    if (parser.getToken() == Token.OPERATOR
                        && parser.getOperator() == Operator.ELSE) {
                        operand2 = expression(true);
                    } else parser.loadState(pState);
                }
                if (condition.isConstOp(core)) {
                    return core.toBoolean(condition.exec(core)) ? operand1 : operand2;
                }
                return new TernarIfOp(condition, operand1, operand2);
            }
            case WHILE: {
                if (!isFirst) throw new ParserException("must be first in expression", parser);
                if (parser.getToken() != Token.OPEN_BRACKET) {
                    throw new ParserException(" '(' expected", parser);
                }
                Operation condition = expression(false);
                if (condition == null) {
                    throw new ParserException("condition expected", parser);
                }
                if (parser.getCurrToken() != Token.CLOSE_BRACKET) {
                    throw new ParserException(" ')' expected", parser);
                }
                boolean temp = canBreakAndContinue;
                canBreakAndContinue = true;
                Operation expression = expression(true);
                canBreakAndContinue = temp;
                return new WhileCycleOp(condition, expression);
            }
            case FOR: {
                if (!isFirst) throw new ParserException("must be first in expression", parser);
                if (parser.getToken() != Token.OPEN_BRACKET) {
                    throw new ParserException(" '(' expected", parser);
                }
                Token temp = currCloseBlock;
                currCloseBlock = Token.CLOSE_BRACKET;
                boolean tbool = forEachTwoDots;
                forEachTwoDots = true;
                Operation init = expression(true);
                forEachTwoDots = tbool;
                if (parser.getCurrToken() == Token.TWO_DOTS) {
                    // ForEach operator
                    VariableReturnOp var = check(init, "can't be static value in forEach operator");
                    Operation table = expression(false);
                    currCloseBlock = temp;
                    if (parser.getCurrToken() != Token.CLOSE_BRACKET) {
                        throw new ParserException(" ')' expected", parser);
                    }
                    tbool = canBreakAndContinue;
                    canBreakAndContinue = true;
                    Operation expression = expression(true);
                    canBreakAndContinue = tbool;
                    return new ForEachOp(var, table, expression);
                }
                if (parser.getCurrToken() != Token.VALUE_END) {
                    throw new ParserException(" ';' expected", parser);
                }
                Operation condition = expression(false);
                if (parser.getCurrToken() != Token.VALUE_END) {
                    throw new ParserException(" ';' expected", parser);
                }
                Operation[] postOps = buildScript();
                currCloseBlock = temp;
                if (parser.getCurrToken() != Token.CLOSE_BRACKET) {
                    throw new ParserException(" ')' expected", parser);
                }
                tbool = canBreakAndContinue;
                canBreakAndContinue = true;
                Operation expression = expression(true);
                canBreakAndContinue = tbool;
                if (condition == null) {
                    condition = new ConstantValueOp(true);
                }
                return new ForCycleOp(init, condition, postOps, expression);
            }
            case DO: {
                if (!isFirst) throw new ParserException("must be first in expression", parser);
                boolean temp = canBreakAndContinue;
                canBreakAndContinue = true;
                Operation expression = expression(true);
                canBreakAndContinue = temp;
                if (expression == null) {
                    throw new ParserException("expression expected", parser);
                }
                if (parser.getToken() != Token.OPERATOR &&
                        parser.getOperator() != Operator.WHILE) {
                    throw new ParserException("'while' operator expected", parser);
                }
                if (parser.getToken() != Token.OPEN_BRACKET) {
                    throw new ParserException(" '(' expected", parser);
                }
                Operation condition = expression(false);
                if (condition == null) {
                    throw new ParserException("condition expected", parser);
                }
                if (parser.getCurrToken() != Token.CLOSE_BRACKET) {
                    throw new ParserException(" ')' expected", parser);
                }
                parser.getToken();
                return new DoWhileOp(expression, condition);
            }
            case VAR: {
                    // Список инициализации локальных переменных
                if (!isFirst) throw new ParserException("must be first in expression", parser);
                Stack<String> listNames = new Stack<String>();
                Stack<Operation> operations = new Stack<Operation>();
                do {
                    if (parser.getToken() != Token.NAME) {
                        throw new ParserException("name of variable expected", parser);
                    }
                    listNames.push(parser.getName());
                    if (parser.getToken() == Token.ASSIGN) {
                        operations.push(expression(false));
                    } else {
                        operations.push(null);
                    }
                } while (parser.getCurrToken() == Token.COMMA);
                String[] temp1 = new String[listNames.size()];
            Operation[] temp2 = new Operation[operations.size()];
                return new LocalInitilaizerOp(listNames.toArray(temp1), operations.toArray(temp2));
            }
            case RETURN:
                if (!isFirst) throw new ParserException("must be first in expression", parser);
                if (!canReturn) {
                    throw new ParserException("'return' operator not a statement", parser);
                }
                return new ReturnOp(expression(false));
            case BREAK:
                if (!isFirst) throw new ParserException("must be first in expression", parser);
                if (!canBreakAndContinue) {
                    throw new ParserException("can't use without DO/WHILE/FOR operator", parser);
                }
                parser.getToken();
                return core.baseOps.BreakOp;
            case CONTINUE:
                if (!isFirst) throw new ParserException("must be first in expression", parser);
                if (!canBreakAndContinue) {
                    throw new ParserException("can't use without DO/WHILE/FOR operator", parser);
                }
                parser.getToken();
                return core.baseOps.ContinueOp;
            case FUNCTION: {
                boolean tbool1 = functionName;
                functionName = true;
                Operation name = expression(false);
                functionName = tbool1;
                if (name != null) {
                    name = (Operation) check(name, "function name can't be a static value");
                }
                if (parser.getCurrToken() != Token.OPEN_BRACKET) {
                    throw new ParserException(" '(' expected", parser);
                }
                Stack<String> argsList = new Stack<String>();
                while (parser.getToken() != Token.CLOSE_BRACKET) {
                    if (parser.getCurrToken() != Token.NAME) {
                        throw new ParserException("argument name expected", parser);
                    }
                    argsList.push(parser.getName());
                    if (parser.getToken() != Token.COMMA) break;
                }
                if (parser.getCurrToken() != Token.CLOSE_BRACKET) {
                    throw new ParserException(" ')' expected", parser);
                }
                if (parser.getToken() != Token.BLOCK_START) {
                    throw new ParserException(" '{' expected", parser);
                }
                Token temp = currCloseBlock;
                currCloseBlock = Token.BLOCK_END;
                tbool1 = canReturn;
                boolean tbool2 = canBreakAndContinue;
                canReturn = true;
                canBreakAndContinue = false;
                Operation[] opList = buildScript();
                canBreakAndContinue = tbool2;
                canReturn = tbool1;
                currCloseBlock = temp;
                if (parser.getCurrToken() != Token.BLOCK_END) {
                    throw new ParserException(" '}' expected", parser);
                }
                if (!isFirst) parser.getToken();
                else {
                    if (temp == Token.BLOCK_END) dontExit = true;
                }
                String[] args = new String[argsList.size()];
                Operation op = new ConstantValueOp(new Function(argsList.toArray(args), opList));
                if (name != null) return new AssignOp((VariableReturnOp) name, op);
                return op;
            }
            case NEW: {
                Object function = expression(false);
                if (!(function instanceof FunctionCallOp)) {
                    throw new ParserException("after 'new' operator must be function call", parser);
                }
                return new NewObjectOp((FunctionCallOp) function);
            }
            case EXTENDS: {
                if (!canReturn) {
                    throw new ParserException("'extends' operator not a statement", parser);
                }
                Object function = expression(false);
                if (!(function instanceof FunctionCallOp)) {
                    throw new ParserException("after 'extends' operator must be function call", parser);
                }
                return new ExtendsOp((FunctionCallOp) function);
            }
            case SIZEOF: return unaryOperator(core.baseOps.sizeof);
            case IS_NUMBER: return unaryOperator(core.baseOps.is_number);
            case IS_INTEGER: return unaryOperator(core.baseOps.is_integer);
            case IS_DOUBLE: return unaryOperator(core.baseOps.is_double);
            case IS_BOOLEAN: return unaryOperator(core.baseOps.is_boolean);
            case IS_STRING: return unaryOperator(core.baseOps.is_string);
            case IS_TABLE: return unaryOperator(core.baseOps.is_table);
            case IS_FUNCTION: return unaryOperator(core.baseOps.is_function);
            case IMPORT: {
                if (!isFirst) throw new ParserException("must be first in expression", parser);
                if (blockLevel != 1) {
                    throw new ParserException("'import' operator must be in main block", parser);
                }
                Operation operand = expression(false);
                if (!operand.isConstOp(core)) {
                    throw new ParserException("must be static value", parser);
                }
                core.loadScriptFromFile(core.toString(operand.exec(core)));
                Operation[] operations = core.getOperations();
                if (operations != null) {
                    for (Operation op : operations) {
                        currOperations.push(op);
                    }
                }
                return null;
            }
            case ELSE:
                throw new ParserException("'else' operator not a statement. 'if' expected", parser);
            default:
                throw new ParserException("not implemented operator yet", parser);
        }
    }

        // Основные и унарные операции
    private Operation primary(boolean isFirst) throws ParserException {
        switch (parser.getToken()) {
            case OPERATOR:
                return operator(isFirst);
            case INT_NUM:
                Operation op = new ConstantValueOp(parser.getIntValue());
                parser.getToken();
                return op;
            case DOUBLE_NUM:
                op = new ConstantValueOp(parser.getDoubleValue());
                parser.getToken();
                return op;
            case TRUE:
                op = new ConstantValueOp(true);
                parser.getToken();
                return op;
            case FALSE:
                op = new ConstantValueOp(false);
                parser.getToken();
                return op;
            case NULL:
                op = new ConstantValueOp(null);
                parser.getToken();
                return op;
            case STRING:
                op = new ConstantValueOp(parser.getString());
                parser.getToken();
                return op;
            case NAME: {
                String name = parser.getName();
                if (parser.getToken() == Token.TWO_DOTS && isFirst && !forEachTwoDots) {
                    // Список инициализации локальных переменных
                    Stack<String> listNames = new Stack<String>();
                    Stack<Operation> operations = new Stack<Operation>();
                    listNames.push(name);
                    operations.push(expression(false));
                    while (parser.getCurrToken() == Token.COMMA) {
                        if (parser.getToken() != Token.NAME) {
                            throw new ParserException("name of variable expected", parser);
                        }
                        listNames.push(parser.getName());
                        if (parser.getToken() != Token.TWO_DOTS) {
                            throw new ParserException(" ':' expected", parser);
                        }
                        operations.push(expression(false));
                    }
                    String[] temp1 = new String[listNames.size()];
                    Operation[] temp2 = new Operation[operations.size()];
                    return new LocalInitilaizerOp(listNames.toArray(temp1), operations.toArray(temp2));
                }
                op = new VariableValueOp(new Variable(name));
                return namePostOp(op);
            }
            case OPEN_BRACKET:
                if (!functionName) {
                    op = expression(false);
                    if (parser.getCurrToken() != Token.CLOSE_BRACKET) {
                        throw new ParserException(" ')' expected", parser);
                    }
                    parser.getToken();
                    return namePostOp(op);
                } else return null;
            case BLOCK_START: {
                Token temp = currCloseBlock;
                currCloseBlock = Token.BLOCK_END;
                Operation[] opList = buildScript();
                currCloseBlock = temp;
                if (parser.getCurrToken() != Token.BLOCK_END) {
                    throw new ParserException(" '}' expected", parser);
                }
                if (!isFirst) parser.getToken();
                else {
                    if (temp == Token.BLOCK_END) dontExit = true;
                }
                op = new BlockOperations(opList);
                if (op.isConstOp(core)) {
                    op = new ConstantValueOp(op.exec(core));
                }
                return namePostOp(op);
            }
            case ARRAY_OPEN: {
                Token temp = currCloseBlock;
                currCloseBlock = Token.ARRAY_CLOSE;
                Operation[] opList = buildScript();
                currCloseBlock = temp;
                if (parser.getCurrToken() != Token.ARRAY_CLOSE) {
                    throw new ParserException(" ']' expected", parser);
                }
                op = new ArrayInitListOp(opList);
                parser.getToken();
                if (op.isConstOp(core)) {
                    op = new ConstantValueOp(op.exec(core));
                }
                return namePostOp(op);
            }
            case INCRESE:
                return new IncreseOp(check(primary(false), "can't apply increse operation to static value"));
            case DECRESE:
                return new DecreseOp(check(primary(false), "can't apply decrese operation to static value"));
            case MINUS:
                return optimazeUnary(primary(false), core.baseOps.minus);
            case PLUS:
                return optimazeUnary(primary(false), core.baseOps.plus);
            case NOT:
                return optimazeUnary(primary(false), core.baseOps.not);
            case BOOL_NOT:
                return optimazeUnary(primary(false), core.baseOps.bool_not);
            case CLOSE_BRACKET:
                if (currCloseBlock != Token.CLOSE_BRACKET) {
                    throw new ParserException(" '(' expected", parser);
                }
                return null;
            case ARRAY_CLOSE:
                if (currCloseBlock != Token.ARRAY_CLOSE) {
                    throw new ParserException(" '[' expected", parser);
                }
                return null;
            case TWO_DOTS:
                if (currCloseBlock != Token.TWO_DOTS) {
                    throw new ParserException("<expression> or '?' expected", parser);
                }
                return null;
            case BLOCK_END:
                if (currCloseBlock != Token.BLOCK_END) {
                    throw new ParserException(" '{' expected", parser);
                }
            case VALUE_END:
            case END:
                return null;
            default:
                throw new ParserException("primary expected", parser);
        }
    }

        // Операции деления/умножения
    private Operation prior5(boolean isFirst) throws ParserException {
        Operation left = primary(isFirst);
        for (;;) {
            switch (parser.getCurrToken()) {
                case DIV:
                    left = optimaze2arg(left, primary(false), core.baseOps.div);
                    break;
                case MUL:
                    left = optimaze2arg(left, primary(false), core.baseOps.mul);
                    break;
                case MOD:
                    left = optimaze2arg(left, primary(false), core.baseOps.mod);
                    break;
                default:
                    return left;
            }
        }
    }

        // Операции сложения/вычитания
    private Operation prior4(boolean isFirst) throws ParserException {
        Operation left = prior5(isFirst);
        for (;;) {
            switch (parser.getCurrToken()) {
                case PLUS:
                    left = optimaze2arg(left, prior5(false), core.baseOps.add);
                    break;
                case MINUS:
                    left = optimaze2arg(left, prior5(false), core.baseOps.sub);
                    break;
                default:
                    return left;
            }
        }
    }

        // Логические операции и сдвиги
    private Operation prior3(boolean isFirst) throws ParserException {
        Operation left = prior4(isFirst);
        for (;;) {
            switch (parser.getCurrToken()) {
                case AND:
                    left = optimaze2arg(left, prior4(false), core.baseOps.and);
                    break;
                case OR:
                    left = optimaze2arg(left, prior4(false), core.baseOps.or);
                    break;
                case XOR:
                    left = optimaze2arg(left, prior4(false), core.baseOps.xor);
                    break;
                case SHIFT_R:
                    left = optimaze2arg(left, prior4(false), core.baseOps.shr);
                    break;
                case SHIFT_L:
                    left = optimaze2arg(left, prior4(false), core.baseOps.shl);
                    break;
                default:
                    return left;
            }
        }
    }

    private VariableReturnOp check(Operation op, String msg) throws ParserException {
        if (!(op instanceof VariableReturnOp)) {
            throw new ParserException(msg, parser);
        }
        return (VariableReturnOp) op;
    }

    private static final String ASSIGN_EXCEPTION = "can't apply assign operation to static value";

        // Операции присваивания
    private Operation prior2(boolean isFirst) throws ParserException {
        Operation left = prior3(isFirst);
        for (;;) {
            switch (parser.getCurrToken()) {
                case ASSIGN:
                    left = new AssignOp(check(left, ASSIGN_EXCEPTION), expression(false));
                    break;
                case ASSIGN_ADD:
                    left = new AssignDoOp(check(left, ASSIGN_EXCEPTION),
                            prior3(false), core.baseOps.add);
                    break;
                case ASSIGN_SUB:
                    left = new AssignDoOp(check(left, ASSIGN_EXCEPTION),
                            expression(false), core.baseOps.sub);
                    break;
                case ASSIGN_MUL:
                    left = new AssignDoOp(check(left, ASSIGN_EXCEPTION),
                            expression(false), core.baseOps.mul);
                    break;
                case ASSIGN_DIV:
                    left = new AssignDoOp(check(left, ASSIGN_EXCEPTION),
                            expression(false), core.baseOps.div);
                    break;
                case ASSIGN_MOD:
                    left = new AssignDoOp(check(left, ASSIGN_EXCEPTION),
                            expression(false), core.baseOps.mod);
                    break;
                case ASSIGN_AND:
                    left = new AssignDoOp(check(left, ASSIGN_EXCEPTION),
                            expression(false), core.baseOps.and);
                    break;
                case ASSIGN_OR:
                    left = new AssignDoOp(check(left, ASSIGN_EXCEPTION),
                            expression(false), core.baseOps.or);
                    break;
                case ASSIGN_XOR:
                    left = new AssignDoOp(check(left, ASSIGN_EXCEPTION),
                            expression(false), core.baseOps.xor);
                    break;
                case ASSIGN_SHL:
                    left = new AssignDoOp(check(left, ASSIGN_EXCEPTION),
                            expression(false), core.baseOps.shl);
                    break;
                case ASSIGN_SHR:
                    left = new AssignDoOp(check(left, ASSIGN_EXCEPTION),
                            expression(false), core.baseOps.shr);
                    break;
                default:
                    return left;
            }
        }
    }
    
        // Операции сравнения
    private Operation prior1(boolean isFirst) throws ParserException {
        Operation left = prior2(isFirst);
        for (;;) {
            switch (parser.getCurrToken()) {
                case EQUALS:
                    left = optimaze2arg(left, prior2(false), core.baseOps.equls);
                    break;
                case NOT_EQUALS:
                    left = optimaze2arg(left, prior2(false), core.baseOps.not_equls);
                    break;
                case REF_EQUALS:
                    left = optimaze2arg(left, prior2(false), core.baseOps.ref_equls);
                    break;
                case REF_NOT_EQUALS:
                    left = optimaze2arg(left, prior2(false), core.baseOps.ref_not_equls);
                    break;
                case BIGGER:
                    left = optimaze2arg(left, prior2(false), core.baseOps.bigger);
                    break;
                case BIGGER_EQUAL:
                    left = optimaze2arg(left, prior2(false), core.baseOps.bigger_equal);
                    break;
                case SMALLER:
                    left = optimaze2arg(left, prior2(false), core.baseOps.smaller);
                    break;
                case SMALLER_EQUAL:
                    left = optimaze2arg(left, prior2(false), core.baseOps.smaller_equal);
                    break;
                default:
                    return left;
            }
        }
    }

        // Логические булевые операции с высшим приоритетом
    private Operation expression(boolean isFirst) throws ParserException {
        Operation left = prior1(isFirst);
        for (;;) {
            switch (parser.getCurrToken()) {
                case DOUBLE_AND:
                    left = optimaze2exec(left, prior1(false), core.baseOps.bool_and);
                    break;
                case DOUBLE_OR:
                    left = optimaze2exec(left, prior1(false), core.baseOps.bool_or);
                    break;
                case DOUBLE_XOR:
                    left = optimaze2arg(left, prior1(false), core.baseOps.bool_xor);
                    break;
                case TERNAR_OP: {
                    Operation op1 = expression(false);
                    if (parser.getCurrToken() != Token.TWO_DOTS) {
                        throw new ParserException(" ':' expected", parser);
                    }
                    //parser.getToken();
                    Operation op2 = expression(false);
                    if (left.isConstOp(core)) {
                        return core.toBoolean(left.exec(core)) ? op1 : op2;
                    }
                    left = new TernarIfOp(left, op1, op2);
                    break;
                }
                case OPERATOR:
                case INCRESE:
                case DECRESE:
                case TWO_DOTS:
                case VALUE_END:
                case COMMA:
                case END:
                case OPEN_BRACKET:
                case CLOSE_BRACKET:
                case ARRAY_CLOSE:
                case BLOCK_END:
                    return left;
                default:
                    throw new ParserException("unknown separator", parser);
            }
        }
    }
}

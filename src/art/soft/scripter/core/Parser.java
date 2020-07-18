package art.soft.scripter.core;

import java.util.HashMap;
import java.util.Stack;

/**
 *
 * @author Артём Святоха
 */
class Parser {

    enum Token {
        NAME, INT_NUM, DOUBLE_NUM, END,
        TRUE, FALSE, NULL,
        PLUS, MINUS, MUL, DIV, MOD,
        AND, OR, XOR, NOT, BOOL_NOT,
        SHIFT_R, SHIFT_L,
        DOUBLE_AND, DOUBLE_OR, DOUBLE_XOR,
        EQUALS, NOT_EQUALS,
        REF_EQUALS, REF_NOT_EQUALS,
        
        BIGGER, SMALLER, // Больше (>), меньше (<)
        BIGGER_EQUAL, SMALLER_EQUAL, // Больше равно (>=), меньше равно (<)

        TERNAR_OP, TWO_DOTS,
        OPEN_BRACKET, CLOSE_BRACKET, // Скобки '(' и ')'
        
        ARRAY_OPEN, ARRAY_CLOSE, // Прямоугольные скобки '[' и ']'
        DOT, COMMA, VALUE_END,
        ASSIGN, ASSIGN_ADD, ASSIGN_SUB,
        ASSIGN_MUL, ASSIGN_DIV, ASSIGN_MOD,
        ASSIGN_AND, ASSIGN_OR, ASSIGN_XOR,
        ASSIGN_SHL, ASSIGN_SHR,
        INCRESE, DECRESE,
        OPERATOR, STRING,
        BLOCK_START, BLOCK_END
    }

    enum Operator {
        IMPORT,
        SIZEOF,
        VAR,
        FUNCTION, RETURN,
        IF, ELSE,
        FOR, DO, WHILE,
        BREAK, CONTINUE,
        /*SWITCH, CASE, DEFAULT,*/
        NEW, EXTENDS,

        IS_NUMBER, IS_INTEGER, IS_DOUBLE,
        IS_BOOLEAN, IS_STRING, IS_TABLE,
        IS_FUNCTION
    }

    private final static HashMap<String, Operator> operators = new HashMap<String, Operator>();

    static void Init() {
        operators.put("function", Operator.FUNCTION);
        operators.put("return", Operator.RETURN);
        operators.put("if", Operator.IF);
        operators.put("else", Operator.ELSE);
        operators.put("for", Operator.FOR);
        operators.put("do", Operator.DO);
        operators.put("while", Operator.WHILE);
        operators.put("break", Operator.BREAK);
        operators.put("continue", Operator.CONTINUE);
        /*operators.put("switch", Operator.SWITCH);
        operators.put("case", Operator.CASE);
        operators.put("default", Operator.DEFAULT);*/
        operators.put("var", Operator.VAR);
        operators.put("new", Operator.NEW);
        operators.put("extends", Operator.EXTENDS);
        operators.put("sizeof", Operator.SIZEOF);
        operators.put("import", Operator.IMPORT);
        // Операторы проверки типов
        operators.put("isNumber", Operator.IS_NUMBER);
        operators.put("isInteger", Operator.IS_INTEGER);
        operators.put("isDouble", Operator.IS_DOUBLE);
        operators.put("isBoolean", Operator.IS_BOOLEAN);
        operators.put("isString", Operator.IS_STRING);
        operators.put("isTable", Operator.IS_TABLE);
        operators.put("isFunction", Operator.IS_FUNCTION);
    }

    private final Stack<String> stringsPool = new Stack<String>();
    private final StringBuffer stringBuffer = new StringBuffer();

    private String script;
    private int lineNum;
    private int lineStart;
    private int wordStart;
    private int currPos;

    void setScript(String script) {
        intValue = lineNum = lineStart = wordStart = currPos = 0;
        doubleValue = 0.0;
        symbol = null;
        this.script = script;
    }

    String getScript() {
        return script;
    }

    int getWordStart() {
        return wordStart - lineStart;
    }

    int getLineNum() {
        return lineNum;
    }

    String getLine() {
        int n = currPos;
        while (n < script.length() && script.charAt(n) != '\n') n ++;
        if (n > script.length()) n = script.length();
        return script.substring(lineStart, n);
    }

    private void toNextLine() {
        int c;
        do {
            c = getNextChar();
        } while (c != '\n' && c != -1);
        lineNum ++;
        lineStart = currPos;
    }

    private int getNextChar() {
        int c = currPos < script.length() ? script.charAt(currPos) : -1;
        currPos ++;
        return c;
    }
 
    private int nextChar() {
        int c = getNextChar();
        if (c == '/') {
            int b = c;
            c = getNextChar();
            if (c == '/') { // Однострочный коментарий
                toNextLine();
                c = getNextChar();
            } else
            if (c == '*') {
                do {
                    c = getNextChar();
                    if (c == '*') {
                        c = getNextChar();
                        if (c == '/') {
                            break;
                        }
                    }
                } while (c != -1);
                c = getNextChar();
            } else {
                charBack();
                c = b;
            }
        }
        return c;
    }

    private void charBack() {
        currPos --;
    }

    Token nextToken() throws ParserException {
        int c;
        while (Character.isWhitespace(c = nextChar())) {
            if (c == '\n') {
                lineNum ++;
                lineStart = currPos;
            }
        }
        if (c == -1) return Token.END;
        wordStart = currPos - 1;
        switch (c) {
            case '+':
                c = nextChar();
                if (c == '+') return Token.INCRESE;
                if (c == '=') return Token.ASSIGN_ADD;
                charBack();
                return Token.PLUS;
            case '-':
                c = nextChar();
                if (c == '-') return Token.DECRESE;
                if (c == '=') return Token.ASSIGN_SUB;
                charBack();
                return Token.MINUS;
            case '*':
                c = nextChar();
                if (c == '=') return Token.ASSIGN_MUL;
                charBack();
                return Token.MUL;
            case '/':
                c = nextChar();
                if (c == '=') return Token.ASSIGN_DIV;
                /*if (c == '/') { // Однострочный коментарий
                    toNextLine();
                    return nextToken();
                }
                if (c == '*') {
                    do {
                        c = nextChar();
                        if (c == '*') {
                            c = nextChar();
                            if (c == '/') break;
                        }
                    } while (c != -1);
                    return nextToken();
                }*/
                charBack();
                return Token.DIV;
            case '%':
                c = nextChar();
                if (c == '=') return Token.ASSIGN_MOD;
                charBack();
                return Token.MOD;
            case '~': return Token.NOT;
            case '!':
                c = nextChar();
                if (c == '=') {
                    c = nextChar();
                    if (c == '=') return Token.REF_NOT_EQUALS;
                    charBack();
                    return Token.NOT_EQUALS;
                }
                charBack();
                return Token.BOOL_NOT;
            case '&':
                c = nextChar();
                if (c == '&') return Token.DOUBLE_AND;
                if (c == '=') return Token.ASSIGN_AND;
                charBack();
                return Token.AND;
            case '|':
                c = nextChar();
                if (c == '|') return Token.DOUBLE_OR;
                if (c == '=') return Token.ASSIGN_OR;
                charBack();
                return Token.OR;
            case '^':
                c = nextChar();
                if (c == '^') return Token.DOUBLE_XOR;
                if (c == '=') return Token.ASSIGN_XOR;
                charBack();
                return Token.XOR;
            case '(': return Token.OPEN_BRACKET;
            case ')': return Token.CLOSE_BRACKET;
            case '[': return Token.ARRAY_OPEN;
            case ']': return Token.ARRAY_CLOSE;
            case '{': return Token.BLOCK_START;
            case '}': return Token.BLOCK_END;
            case '?': return Token.TERNAR_OP;
            case ':': return Token.TWO_DOTS;
            case '=':
                c = nextChar();
                if (c == '=') {
                    c = nextChar();
                    if (c == '=') return Token.REF_EQUALS;
                    charBack();
                    return Token.EQUALS;
                }
                charBack();
                return Token.ASSIGN;
            case '>':
                c = nextChar();
                if (c == '=') return Token.BIGGER_EQUAL;
                if (c == '>') {
                    c = nextChar();
                    if (c == '=') return Token.ASSIGN_SHR;
                    charBack();
                    return Token.SHIFT_R;
                }
                charBack();
                return Token.BIGGER;
            case '<':
                c = nextChar();
                if (c == '=') return Token.SMALLER_EQUAL;
                if (c == '<') {
                    c = nextChar();
                    if (c == '=') return Token.ASSIGN_SHL;
                    charBack();
                    return Token.SHIFT_L;
                }
                charBack();
                return Token.SMALLER;
            case '\"':
                /* Реализация строки */
                stringBuffer.delete(0, stringBuffer.length());
                while (true) {
                    c = readStringChar();
                    if (c == -2) break;
                    stringBuffer.append((char) c);
                }
                symbol = stringBuffer.toString();
                return Token.STRING;
            case '\'':
                /* Реализация символа */
                c = readStringChar();
                intValue = c;
                if (c == -2) throw new ParserException("unexpected character '\"'", this);
                c = nextChar();
                if (c == -1) throw new ParserException(" \"'\" expected", this);
                if (c != '\'') throw new ParserException("unexpected character '" + c + '\'', this);
                return Token.INT_NUM;
            case ';': return Token.VALUE_END;
            case ',': return Token.COMMA;
            case '.':
                c = nextChar();
                charBack();
                if (Character.isDigit(c)) return getDigit(c);
                return Token.DOT;
            default:
                if (Character.isDigit(c)) {
                    return getDigit(c);
                } else
                if (Character.isJavaIdentifierStart(c)) {
                    return getNameIndetifier(c);
                } else throw new ParserException("unknown symbol", this);
        }
    }

    private int readStringChar() throws ParserException {
        int c = getNextChar();
        if (c == -1) throw new ParserException(" '\"' expected", this);
        if (c == '\"') return -2;
        if (c == '\\') {
            c = nextChar();
            switch (c) {
                case 'n': return '\n';
                case 'r': return '\r';
                case 't': return '\t';
                case -1:
                    throw new ParserException(" '\"' expected", this);
            }
        }
        return c;
    }

    private Token currToken;

    Token getToken() throws ParserException {
        return currToken = nextToken();
    }

    Token getCurrToken() {
        return currToken;
    }

    private Token getNameIndetifier(int c) {
        String symName = getSymbolName(c);
        if (symName.equals(ScriptCore.NULL_POINTER)) return Token.NULL;
        if (symName.equals(ScriptCore.BOOLEAN_TRUE)) return Token.TRUE;
        if (symName.equals(ScriptCore.BOOLEAN_FALSE)) return Token.FALSE;
        operator = operators.get(symName);
        if (operator != null) return Token.OPERATOR;
        symbol = symName;
        return Token.NAME;
    }

    private Token getDigit(int c) throws ParserException {
        String symbolName = getNumber(c);
        try {
            intValue = Integer.decode(symbolName);
            return Token.INT_NUM;
        } catch (NumberFormatException nfe) {}
        try {
            doubleValue = Double.valueOf(symbolName);
            return Token.DOUBLE_NUM;
        } catch (NumberFormatException nfe) {}
        throw new ParserException("unknown number format", this);
    }

    private String getSymbolName(int c) {
        stringBuffer.setLength(0);
        stringBuffer.append((char) c);
        do {
            c = nextChar();
            if (Character.isJavaIdentifierPart(c)) {
                stringBuffer.append((char) c);
            } else break;
        } while (true);
        charBack();
        String temp = stringBuffer.toString();
            // Запулить строку если таковая отсутвует
        for (String pool : stringsPool) {
            if (pool.equals(temp)) return pool;
        }
        stringsPool.push(temp);
        return temp;
    }

    private String getNumber(int c) {
        stringBuffer.setLength(0);
        stringBuffer.append((char) c);
        do {
            c = nextChar();
            if (Character.isJavaIdentifierPart(c) || c == '.') {
                stringBuffer.append((char) c);
            } else break;
        } while (true);
        charBack();
        return stringBuffer.toString();
    }

    private boolean nextIsWhitespace() {
        return Character.isWhitespace(nextChar());
    }

    private Integer intValue;

    private Double doubleValue;

    private String symbol;

    private Operator operator;

    int getIntValue() {
        return intValue;
    }

    double getDoubleValue() {
        return doubleValue;
    }

    String getName() {
        return symbol;
    }

    String getString() {
        return symbol;
    }

    Operator getOperator() {
        return operator;
    }

    ParserState saveState() {
        return new ParserState(this);
    }

    void loadState(ParserState state) {
        lineNum = state.lineNum;
        lineStart = state.lineStart;
        wordStart = state.wordStart;
        currPos = state.currPos;
        currToken = state.currToken;
        operator = state.operator;
        intValue = state.intValue;
        doubleValue = state.doubleValue;
        symbol = state.symbol;
    }

    class ParserState {
        private final int lineNum;
        private final int lineStart;
        private final int wordStart;
        private final int currPos;
        private final Token currToken;
        private final Operator operator;
        private final Integer intValue;
        private final Double doubleValue;
        private final String symbol;

        private ParserState(Parser parser) {
            this.lineNum = parser.lineNum;
            this.lineStart = parser.lineStart;
            this.wordStart = parser.wordStart;
            this.currPos = parser.currPos;
            this.currToken = parser.currToken;
            this.operator = parser.operator;
            this.intValue = parser.intValue;
            this.doubleValue = parser.doubleValue;
            this.symbol = parser.symbol;
        }
    }

    String getErrorMessage(String message) {
        stringBuffer.setLength(0);
        stringBuffer.append("at line [");
        stringBuffer.append(lineNum + 1);
        stringBuffer.append(',');
        stringBuffer.append(getWordStart() + 1);
        stringBuffer.append("]:\n");
        stringBuffer.append(getLine().replace('\t', ' '));
        stringBuffer.append('\n');
        for (int i = wordStart - lineStart; i > 0; i --) {
            stringBuffer.append(' ');
        }
        stringBuffer.append("^\n    ");
        stringBuffer.append(message);
        return stringBuffer.toString();
    }
}

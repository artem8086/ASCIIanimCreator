package art.soft.scripter.core;

/**
 *
 * @author Артём Святоха
 */
public class ParserException extends Throwable {

    private final String message;
    private final String line;
    private final int lineNum;
    private final int wordStart;

    public ParserException(String message, Parser parser) {
        super(parser.getErrorMessage(message));
        wordStart = parser.getWordStart();
        lineNum = parser.getLineNum();
        line = parser.getLine();
        this.message = message;
    }

    public int getLineNum() {
        return lineNum;
    }

    public int getWordStart() {
        return wordStart;
    }

    public String getLine() {
        return line;
    }

    public String getMessageText() {
        return message;
    }
}

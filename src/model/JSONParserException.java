package model;

/**
 * Exception, die beim Parsen von JSON auftreten kann
 */
public class JSONParserException extends Exception {

    public JSONParserException(String msg) {
        super(msg);
    }
}

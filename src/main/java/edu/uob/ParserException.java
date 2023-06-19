package edu.uob;

import java.io.Serial;

public class ParserException extends Exception {
    @Serial
    private static final long serialVersionUID = 1;

    public ParserException(String message) {
        super(message);
    }
    public static class NoPlayerNameException extends ParserException {
        @Serial private static final long serialVersionUID = 1;
        public NoPlayerNameException() {
            super("No user name is specified");
        }
    }
    public static class WrongOrderCommandException extends ParserException {
        @Serial private static final long serialVersionUID = 1;
        public WrongOrderCommandException() {
            super("Command out of order");
        }
    }
    public static class TooManyCommandException extends ParserException {
        @Serial private static final long serialVersionUID = 1;
        public TooManyCommandException() {
            super("Trying to perform more than one commands");
        }
    }
    public static class TooManyActionException extends ParserException {
        @Serial private static final long serialVersionUID = 1;
        public TooManyActionException() {
            super("Trying to perform more than one actions");
        }
    }
    public static class InvalidPlayerNameException extends ParserException {
        @Serial private static final long serialVersionUID = 1;
        public InvalidPlayerNameException(String name) {
            super(name + " is not a valid name");
        }
    }
}

package edu.uob;

import java.io.Serial;

public class GameException extends Exception {
    @Serial
    private static final long serialVersionUID = 1;

    public GameException(String message) {
        super(message);
    }

    public static class NoLocationFoundToAddPathException extends GameException {
        @Serial private static final long serialVersionUID = 1;
        public NoLocationFoundToAddPathException(String location) {
            super("Location " + location + " does not exist");
        }
    }
}

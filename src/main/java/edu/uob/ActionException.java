package edu.uob;

import java.io.Serial;

public class ActionException extends Exception {
    @Serial
    private static final long serialVersionUID = 1;

    public ActionException(String message) {
        super(message);
    }
    public static class NotSufficientSubjectsException extends ActionException {
        @Serial private static final long serialVersionUID = 1;
        public NotSufficientSubjectsException() {
            super("You don't have sufficient subjects to perform this action");
        }
    }
    public static class NotSufficientArtefactException extends ActionException {
        @Serial private static final long serialVersionUID = 1;
        public NotSufficientArtefactException() {
            super("Some items are not available at this time");
        }
    }
    public static class TooManyActionsException extends ActionException {
        @Serial private static final long serialVersionUID = 1;
        public TooManyActionsException() {
            super("Trying to perform more than one actions");
        }
    }

    public static class NoActionFoundException extends ActionException {
        @Serial private static final long serialVersionUID = 1;
        public NoActionFoundException() {
            super("No matched action");
        }
    }
    public static class HealthLevelToZeroException extends ActionException {
        @Serial private static final long serialVersionUID = 1;
        public HealthLevelToZeroException() {
            super("Running out of health");
        }
    }
    public static class GameOverException extends ActionException {
        @Serial private static final long serialVersionUID = 1;
        public GameOverException() {
            super("You died and lost all of your items, you must return to the start of the game");
        }
    }
}

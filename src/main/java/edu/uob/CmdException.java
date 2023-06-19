package edu.uob;

import java.io.Serial;

public class CmdException extends Exception {
    @Serial
    private static final long serialVersionUID = 1;

    public CmdException(String message) {
        super(message);
    }
    public static class InventoryNotExistInLocationException extends CmdException {
        @Serial private static final long serialVersionUID = 1;
        public InventoryNotExistInLocationException(String location, String artefact) {
            super(artefact + " does not exist in " + location );
        }
    }
    public static class InventoryNotExistInInventoryException extends CmdException {
        @Serial private static final long serialVersionUID = 1;
        public InventoryNotExistInInventoryException(String artefact) {
            super(artefact + " does not exist in your inventory");
        }
    }

    public static class NoPathFoundException extends CmdException {
        @Serial private static final long serialVersionUID = 1;
        public NoPathFoundException(String location, String path) {
            super("There is no path to " + path + " from " + location);
        }
    }

    public static class ExtraneousEntitiesException extends CmdException {
        @Serial private static final long serialVersionUID = 1;
        public ExtraneousEntitiesException() {
            super("Provided too many subjects to this command");
        }
    }

    public static class InsufficientEntitiesException extends CmdException {
        @Serial private static final long serialVersionUID = 1;
        public InsufficientEntitiesException(String missingEntity) {
            super("Missing subject: " + missingEntity);
        }
    }
}

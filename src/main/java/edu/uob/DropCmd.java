package edu.uob;

import edu.uob.CmdException.InventoryNotExistInInventoryException;
import edu.uob.CmdException.ExtraneousEntitiesException;
import edu.uob.CmdException.InsufficientEntitiesException;

public class DropCmd extends Command {
    public DropCmd() {
        super("drop");
    }
    @Override
    public String execute(PlayerEntity player, String... args) throws InventoryNotExistInInventoryException, ExtraneousEntitiesException, InsufficientEntitiesException {
        validateEntities(args);
        ArtefactEntity artefact = player.getInventoryByName(args[0]);
        if (artefact == null) {
            throw new InventoryNotExistInInventoryException(args[0]);
        }
        LocationEntity location = player.getCurrentLocation();
        location.addEntity(artefact);
        return "";
    }
    @Override
    public void validateEntities(String... args) throws CmdException.InsufficientEntitiesException, ExtraneousEntitiesException {
        if (args.length == 0) {
            throw new CmdException.InsufficientEntitiesException("The artefact you want to put down");
        }
        if (args.length > 1) {
            throw new ExtraneousEntitiesException();
        }
    }
}
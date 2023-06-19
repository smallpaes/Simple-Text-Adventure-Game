package edu.uob;

import edu.uob.CmdException.InventoryNotExistInLocationException;
import edu.uob.CmdException.ExtraneousEntitiesException;
import edu.uob.CmdException.InsufficientEntitiesException;

public class GetCmd extends Command {
    public GetCmd() {
        super("get");
    }
    @Override
    public String execute(PlayerEntity player, String... args) throws InventoryNotExistInLocationException, ExtraneousEntitiesException, InsufficientEntitiesException {
        validateEntities(args);
        LocationEntity location = player.getCurrentLocation();
        ArtefactEntity artefact = location.getArtefactByName(args[0]);
        if (artefact == null) {
            throw new InventoryNotExistInLocationException(location.getName(), args[0]);
        }
        player.addInventory(artefact);
        return "You picked up a(an) " + artefact.getName();
    }
    @Override
    public void validateEntities(String... args) throws CmdException.InsufficientEntitiesException, ExtraneousEntitiesException {
        if (args.length == 0) {
            throw new CmdException.InsufficientEntitiesException("The artefact you want to pick up");
        }
        if (args.length > 1) {
            throw new ExtraneousEntitiesException();
        }
    }
}

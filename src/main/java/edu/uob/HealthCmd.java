package edu.uob;

import edu.uob.CmdException.InventoryNotExistInInventoryException;
import edu.uob.CmdException.ExtraneousEntitiesException;

public class HealthCmd extends Command {
    public HealthCmd() {
        super("health");
    }
    @Override
    public String execute(PlayerEntity player, String... args) throws InventoryNotExistInInventoryException, ExtraneousEntitiesException {
        validateEntities(args);
        return Integer.toString(player.getHealthLevel());
    }
    @Override
    public void validateEntities(String... args) throws ExtraneousEntitiesException {
        if (args.length > 0) {
            throw new ExtraneousEntitiesException();
        }
    }
}

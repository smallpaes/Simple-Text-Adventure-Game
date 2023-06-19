package edu.uob;

import edu.uob.CmdException.ExtraneousEntitiesException;

public class InventoryCmd extends Command {
    public InventoryCmd() {
        super("inventory");
    }
    @Override
    public String execute(PlayerEntity player, String... args) throws ExtraneousEntitiesException {
        validateEntities(args);
        String[] artefacts = player.getInventoryNames();
        return String.join( "\n", artefacts);
    }
    @Override
    public void validateEntities(String... args) throws ExtraneousEntitiesException {
        if (args.length > 0) {
            throw new ExtraneousEntitiesException();
        }
    }
}

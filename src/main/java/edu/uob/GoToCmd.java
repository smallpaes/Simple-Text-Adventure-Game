package edu.uob;

import edu.uob.CmdException.NoPathFoundException;
import edu.uob.CmdException.ExtraneousEntitiesException;
import edu.uob.CmdException.InsufficientEntitiesException;

public class GoToCmd extends Command {
    public GoToCmd() {
        super("goto");
    }
    @Override
    public String execute(PlayerEntity player, String... args) throws NoPathFoundException, ExtraneousEntitiesException, InsufficientEntitiesException {
        validateEntities(args);
        LocationEntity location = player.getCurrentLocation();
        LocationEntity path = location.getPathByName(args[0]);
        if(path == null) {
            throw new NoPathFoundException(location.getName(), args[0]);
        }
        location.removeCharacter(player);
        path.addCharacter(player);
        player.setCurrentLocation(path);
        return LookCmd.printLook(path, player);
    }
    @Override
    public void validateEntities(String... args) throws InsufficientEntitiesException, ExtraneousEntitiesException {
        if (args.length == 0) {
            throw new InsufficientEntitiesException("The location you want to go");
        }
        if (args.length > 1) {
            throw new ExtraneousEntitiesException();
        }
    }
}
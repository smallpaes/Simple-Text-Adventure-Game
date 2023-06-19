package edu.uob;

import java.util.ArrayList;

import edu.uob.CmdException.ExtraneousEntitiesException;

public class LookCmd extends Command {
    public LookCmd() {
        super("look");
    }
    @Override
    public String execute(PlayerEntity player, String... args) throws ExtraneousEntitiesException {
        validateEntities(args);
        LocationEntity location = player.getCurrentLocation();
        return printLook(location, player);
    }
    @Override
    public void validateEntities(String... args) throws ExtraneousEntitiesException {
        if (args.length > 0) {
            throw new ExtraneousEntitiesException();
        }
    }

    public static String printLook(LocationEntity location, PlayerEntity player) {
        StringBuilder result = new StringBuilder();
        result.append("You are in ")
                .append(location.getDescription()).append("\n")
                .append("You can now see: \n");

        ArrayList<GameEntity> entities = new ArrayList<>();
        entities.addAll(location.getOtherPlayers(player));
        entities.addAll(location.getArtefacts());
        entities.addAll(location.getFurnitures());
        for (GameEntity entity : entities) {
            result.append(entity.getDescription()).append("\n");
        }
        result.append("You can access from here:  \n");
        for (LocationEntity path : location.getPaths()) {
            result.append(path.getName()).append("\n");
        }
        return result.toString();
    }
}
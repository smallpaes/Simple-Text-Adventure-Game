package edu.uob;

import java.util.Arrays;
import java.util.List;

import edu.uob.ActionException.NotSufficientSubjectsException;
import edu.uob.ActionException.NotSufficientArtefactException;
import edu.uob.ActionException.HealthLevelToZeroException;
import edu.uob.ActionException.GameOverException;

public class GameAction
{
    String[] names;
    String[] neededItems;
    String[] consumedItems;
    String[] producedItems;
    String message;
    public GameAction(String[] names, String[] neededItems, String[] consumedItems, String[] producedItems, String message) {
        this.names = names.clone();
        this.neededItems = neededItems.clone();
        this.consumedItems = consumedItems.clone();
        this.producedItems = producedItems.clone();
        this.message = message;
    }

    public String execute(Game game, PlayerEntity player) {
        try {
            LocationEntity location = player.getCurrentLocation();
            checkExecutable(game, location, player);
            if (consumedItems.length > 0) {
                handleConsumptions(game, location, player);
            }
            if (producedItems.length > 0) {
                handleProductions(game, location, player);
            }
            return getMessage();
        } catch (NotSufficientSubjectsException | NotSufficientArtefactException | GameOverException e) {
            return e.getMessage();
        }
    }
    private void handleConsumptions(Game game, LocationEntity location, PlayerEntity player) throws GameOverException {
        try {
            for (String item : consumedItems) {
                handleConsumption(game, location, player, item);
            }
        } catch (HealthLevelToZeroException e) {
            dropPlayerItems(location, player);
            player.resetPlayer(game.getStartLocation());
            throw new GameOverException();
        }
    }

    private void handleConsumption(Game game, LocationEntity location, PlayerEntity player, String item) throws HealthLevelToZeroException {
        boolean isHealthCmd = CmdType.parse(item) == CmdType.HEALTH;
        if (isHealthCmd) {
            player.decrementHealthLevel();
            return;
        }
        boolean isInInventory = player.hasInventoryByName(item);
        if (isInInventory) {
            game.getStoreroom().addEntity(player.getInventoryByName(item));
            return;
        }
        boolean isInLocation = location.isItemHereByName(item);
        if (isInLocation) {
            game.getStoreroom().addEntity(location.removeEntityByName(item));
            return;
        }
        LocationEntity entityLocation = game.findMovableEntity(item);
        if (entityLocation != null) {
            game.getStoreroom().addEntity(entityLocation.removeEntityByName(item));
        }
    }

    private void dropPlayerItems(LocationEntity location, PlayerEntity player) {
        List<ArtefactEntity> items = player.getInventories();
        if (items.isEmpty()) {
            return;
        }
        location.addArtefacts(items);
    }

    private void handleProductions(Game game, LocationEntity location, PlayerEntity player) {
        for (String item : producedItems) {
            handleProduction(game, location, player, item);
        }
    }

    private void handleProduction(Game game, LocationEntity location, PlayerEntity player, String item) {
        boolean isHealthCmd = CmdType.parse(item) == CmdType.HEALTH;
        if (isHealthCmd) {
            player.incrementHealthLevel();
            return;
        }
        LocationEntity destination = game.getLocationByName(item);
        if (destination != null) {
            location.addEntity(destination);
            return;
        }
        boolean isInStorage = game.getStoreroom().isItemHereByName(item);
        if (isInStorage) {
            GameEntity entity = game.getStoreroom().removeEntityByName(item);
            location.addEntity(entity);
            return;
        }
        LocationEntity entityLocation = game.findMovableEntity(item);
        if (entityLocation != null) {
            location.addEntity(entityLocation.removeEntityByName(item));
        }
    }

    private boolean isPossessAllSubjects(LocationEntity location, PlayerEntity player) {
        for (String item : neededItems) {
            boolean isHealthCmd = CmdType.parse(item) == CmdType.HEALTH;
            boolean isInInventory = player.hasInventoryByName(item);
            boolean isInLocation = location.isItemHereByName(item);
            if (!isHealthCmd && !isInInventory && !isInLocation) {
                return false;
            }
        }
        return true;
    }
    private void checkExecutable(Game game, LocationEntity location, PlayerEntity player) throws NotSufficientArtefactException, NotSufficientSubjectsException {
        if (!isPossessAllSubjects(location, player)) {
            throw new NotSufficientArtefactException();
        }
        if (producedItems.length == 0) {
            return;
        }
        checkProduceable(game);
    }
    private void checkProduceable(Game game) throws NotSufficientSubjectsException {
        boolean areProduceable = false;
        for (String item : producedItems) {
            boolean isProduceable = checkOneProduceable(game, item);
            if (isProduceable) {
                areProduceable = true;
            }
        }
        if (!areProduceable) {
            throw new NotSufficientSubjectsException();
        }
    }

    private boolean checkOneProduceable(Game game, String item)  {
        boolean isHealthCmd = CmdType.parse(item) == CmdType.HEALTH;
        Storage storeroom = game.getStoreroom();
        boolean isPath = game.getLocationByName(item) != null;
        boolean isInStorage = storeroom.isItemHereByName(item);
        boolean isArtefactSomewhere = game.findMovableEntity(item) != null;
        return isHealthCmd || isPath || isInStorage || isArtefactSomewhere;
    }

    public boolean isMatchedAction(List<String> phrases, List<String> subjects) {
        boolean isValidNamesList = validateInTargetList(this.names, phrases);
        boolean isValidItemList = validateInTargetList(this.neededItems, subjects);
        return isValidItemList && isValidNamesList;
    }

    private boolean validateInTargetList(String[] targetList, List<String> givenList) {
        if (givenList.isEmpty()) {
            return false;
        }
        if (givenList.size() > targetList.length) {
            return false;
        }
        List<String> listItems = Arrays.asList(targetList);
        for (String item : givenList) {
            if (!listItems.contains(item.toLowerCase())) {
                return false;
            }
        }
        return true;
    }
    public String getMessage() {
        return message;
    }
}

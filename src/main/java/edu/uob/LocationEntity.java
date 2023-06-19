package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class LocationEntity extends GameEntity {
    private final List<LocationEntity> paths;
    private final List<CharacterEntity> characters;
    private final List<ArtefactEntity> artefacts;
    private final List<FurnitureEntity> furnitures;

    public LocationEntity(String name, String description) {
        super(name, description);
        this.paths = new ArrayList<>();
        this.characters = new ArrayList<>();
        this.artefacts = new ArrayList<>();
        this.furnitures = new ArrayList<>();
    }

    public void addFurniture(FurnitureEntity furniture) {
        furnitures.add(furniture);
    }
    public void addCharacter(CharacterEntity character) {
        characters.add(character);
    }
    public void removeCharacter(CharacterEntity character) {
        characters.remove(character);
    }
    public void addPath(LocationEntity path) {
        paths.add(path);
    }

    private GameEntity removeItemFromTargetListByName(List<? extends GameEntity> targetList, String name) {
        for (GameEntity entity : targetList) {
            if (entity.getName().equalsIgnoreCase(name)) {
                targetList.remove(entity);
                return entity;
            }
        }
        return null;
    }

    public LocationEntity getPathByName(String name) {
        for (LocationEntity path : paths) {
            if (path.getName().equalsIgnoreCase(name)) {
                return path;
            }
        }
        return null;
    }
    public void addArtefact(ArtefactEntity artefact) {
        artefacts.add(artefact);
    }
    public void addArtefacts(List<ArtefactEntity> artefact) {
        artefacts.addAll(artefact);
    }
    public void addEntity(GameEntity entity) {
        if (entity instanceof LocationEntity) {
            addPath((LocationEntity) entity);
        } else if (entity instanceof FurnitureEntity) {
            addFurniture((FurnitureEntity) entity);
        } else if (entity instanceof CharacterEntity) {
            addCharacter((CharacterEntity) entity);
        } else if (entity instanceof ArtefactEntity) {
            addArtefact((ArtefactEntity) entity);
        }
    }
    public GameEntity removeEntityByName(String name) {
        boolean isInPath = hasItemInTargetListByName(paths, name);
        if (isInPath) {
            removeItemFromTargetListByName(paths, name);
            return null;
        }
        boolean isInCharacters = hasItemInTargetListByName(characters, name);
        if (isInCharacters) {
           return removeItemFromTargetListByName(characters, name);
        }
        boolean isInArtefacts = hasItemInTargetListByName(artefacts, name);
        if (isInArtefacts) {
            return removeItemFromTargetListByName(artefacts, name);
        }
        boolean isInFurnitures = hasItemInTargetListByName(furnitures, name);
        if (isInFurnitures) {
            return removeItemFromTargetListByName(furnitures, name);
        }
        return null;
    }

    public boolean isItemHereByName(String name) {
        boolean isCurrentLocation = name.equalsIgnoreCase(this.getName());
        boolean isInPath = hasItemInTargetListByName(paths, name);
        boolean isInCharacters = hasItemInTargetListByName(characters, name);
        boolean isInArtefacts = hasItemInTargetListByName(artefacts, name);
        boolean isInFurnitures = hasItemInTargetListByName(furnitures, name);
        return isInPath || isInCharacters || isInArtefacts || isInFurnitures || isCurrentLocation;
    }
    public boolean hasItemInTargetListByName(List<? extends GameEntity> targetList, String itemName) {
        for (GameEntity entity : targetList) {
            if (entity.getName().equalsIgnoreCase(itemName)) {
                return true;
            }
        }
        return false;
    }
    public boolean hasArtefactByName(String name) {
        return hasItemInTargetListByName(artefacts, name);
    }
    public boolean hasFurnitureByName(String name) {
        return hasItemInTargetListByName(furnitures, name);
    }
    public boolean hasCharacterByName(String name) {
        return hasItemInTargetListByName(characters, name);
    }
    public ArtefactEntity getArtefactByName(String name) {
        for (ArtefactEntity artefact : artefacts) {
            if (artefact.getName().equalsIgnoreCase(name)) {
                this.artefacts.remove(artefact);
                return artefact;
            }
        }
        return null;
    }

    public List<CharacterEntity> getOtherPlayers(PlayerEntity excludePlayer) {
        List<CharacterEntity> otherPlayers = new ArrayList<>();
        for (CharacterEntity character : characters) {
            if (character.equals(excludePlayer)) {
                continue;
            }
            otherPlayers.add(character);
        }
        return otherPlayers;
    }
    public List<ArtefactEntity> getArtefacts() {
        return artefacts;
    }
    public List<FurnitureEntity> getFurnitures() {
        return furnitures;
    }
    public List<LocationEntity> getPaths() {
        return paths;
    }
}

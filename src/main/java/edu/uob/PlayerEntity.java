package edu.uob;

import java.util.ArrayList;
import java.util.List;

import edu.uob.ActionException.HealthLevelToZeroException;

public class PlayerEntity extends CharacterEntity {
    private List<ArtefactEntity> inventories;
    private LocationEntity currentLocation;
    private int healthLevel;
    public PlayerEntity(String name, LocationEntity startLocation) {
        super(name, "A player named: " + name);
        this.inventories = new ArrayList<>();
        this.currentLocation = startLocation;
        this.healthLevel = 3;
    }
    public int getHealthLevel() {
        return healthLevel;
    }
    public void incrementHealthLevel() {
        if (healthLevel == 3) {
            return;
        }
        healthLevel++;
    }
    public void decrementHealthLevel() throws HealthLevelToZeroException {
        healthLevel--;
        if (healthLevel == 0) {
            throw new HealthLevelToZeroException();
        }
    }

    public void resetPlayer(LocationEntity startLocation) {
        inventories = new ArrayList<>();
        currentLocation = startLocation;
        healthLevel = 3;
    }

    public LocationEntity getCurrentLocation() {
        return currentLocation;
    }
    public void setCurrentLocation(LocationEntity newLocation) {
        currentLocation = newLocation;
    }

    public String[] getInventoryNames() {
        String[] names = new String[inventories.size()];
        for (int i = 0; i < inventories.size(); i++) {
            names[i] = inventories.get(i).getName();
        }
        return names;
    }
    public ArtefactEntity getInventoryByName(String name) {
        for (ArtefactEntity inventory : inventories) {
            if (inventory.getName().equalsIgnoreCase(name)) {
                this.inventories.remove(inventory);
                return inventory;
            }
        }
        return null;
    }
    public boolean hasInventoryByName(String name) {
        for (ArtefactEntity inventory : inventories) {
            if (inventory.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
    public void addInventory(ArtefactEntity inventory) {
        inventories.add(inventory);
    }
    public List<ArtefactEntity> getInventories() {
        return inventories;
    }
}

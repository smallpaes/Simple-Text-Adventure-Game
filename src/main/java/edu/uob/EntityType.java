package edu.uob;

public enum EntityType {
    ARTEFACT,
    FURNITURE,
    CHARACTER,
    STORAGE;


    public static EntityType parse(String type) {
        return switch (type.toUpperCase()) {
            case "CHARACTERS" -> CHARACTER;
            case "ARTEFACTS" -> ARTEFACT;
            case "FURNITURE" -> FURNITURE;
            case "STOREROOM" -> STORAGE;
            default -> null;
        };
    }
}

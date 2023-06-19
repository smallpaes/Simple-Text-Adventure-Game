package edu.uob;

public class Storage extends LocationEntity {
    public Storage(String name, String description) {
        super(name, description);
    }
    @Override
    public void addPath(LocationEntity path) {
        return;
    }
}

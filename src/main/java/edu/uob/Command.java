package edu.uob;

public abstract class Command {
    private final String name;
    public Command(String name) { this.name = name; }
    public abstract String execute(PlayerEntity player, String... args) throws CmdException;
    public abstract void validateEntities(String... args) throws CmdException;
}

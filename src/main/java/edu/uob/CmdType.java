package edu.uob;

public enum CmdType {
    INV,
    INVENTORY,
    GET,
    DROP,
    GOTO,
    LOOK,
    HEALTH;


    public static CmdType parse(String type) {
        return switch (type.toUpperCase()) {
            case "INVENTORY", "INV" -> INVENTORY;
            case "GET" -> GET;
            case "DROP" -> DROP;
            case "GOTO" -> GOTO;
            case "LOOK" -> LOOK;
            case "HEALTH" -> HEALTH;
            default -> null;
        };
    }
    public static CmdType[] getAllCommands() {
        return CmdType.values();
    }
    public static Command createCmd(CmdType type) {
        return switch (type) {
            case INVENTORY, INV -> new InventoryCmd();
            case GET -> new GetCmd();
            case DROP -> new DropCmd();
            case GOTO -> new GoToCmd();
            case LOOK -> new LookCmd();
            case HEALTH -> new HealthCmd();
        };
    }
}

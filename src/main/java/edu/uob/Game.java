package edu.uob;

import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.uob.ActionException.TooManyActionsException;
import edu.uob.ActionException.NoActionFoundException;
import edu.uob.GameException.NoLocationFoundToAddPathException;

public class Game {
    HashMap<String, PlayerEntity> players;
    HashMap<String, LocationEntity> locations;
    HashMap<String, HashSet<GameAction>> actions;
    HashMap<CmdType, Command> commands;
    HashSet<String> subjectList;
    Storage storeroom;
    String entry;

    public Game() {
        this.players = new HashMap<>();
        this.locations = new HashMap<>();
        this.actions = new HashMap<>();
        this.subjectList = new HashSet<>();
    }

    public void initGame(List<Graph> entities, NodeList actions) throws NoLocationFoundToAddPathException {
        createEntities(entities);
        createActions(actions);
        initCommands();
    }

    private void initCommands() {
        this.commands = new HashMap<>();
        for (CmdType cmdType : CmdType.getAllCommands()) {
            Command command = CmdType.createCmd(cmdType);
            this.commands.put(cmdType, command);
        }
    }

    private void createEntities(List<Graph> entities) throws NoLocationFoundToAddPathException {
        handleLocations(entities.get(0).getSubgraphs());
        handlePaths(entities.get(1).getEdges());
    }

    private void handleLocations(List<Graph> locations) {
        for (int i = 0; i < locations.size(); i++) {
            handleLocation(locations.get(i), i);
        }
        if (this.storeroom == null) {
            this.storeroom = new Storage("Storeroom", "Placeholder Storeroom");
        }
    }
    private void addToSubjectList(String name) {
        subjectList.add(name);
    }

    private void handleLocation(Graph location, int index) {
        Node locationDetails = location.getNodes(false).get(0);
        String name = locationDetails.getId().getId().toLowerCase();
        String description = locationDetails.getAttribute("description");
        addToSubjectList(name);
        boolean isEntrance = index == 0;
        if (isEntrance) {
            entry = name;
        }
        LocationEntity newGameEntity;
        if (EntityType.parse(name) == EntityType.STORAGE) {
            newGameEntity = new Storage(name, description);
        } else {
            newGameEntity = new LocationEntity(name, description);
        }
        addLocationEntities(location.getSubgraphs(), newGameEntity);
        if (newGameEntity instanceof Storage) {
            this.storeroom = (Storage) newGameEntity;
        } else {
            this.locations.put(name, newGameEntity);
        }
    }

    public LocationEntity getStartLocation() {
        return locations.get(entry);
    }

    private void addLocationEntities(List<Graph> entityGroups, LocationEntity gameEntity) {
        for (Graph entityGroup : entityGroups) {
            EntityType entityType = EntityType.parse(entityGroup.getId().getId());
            if (entityGroup.getNodes(false).size() == 0) {
                continue;
            }
            addEntityToLocation(entityGroup, entityType, gameEntity);
        }
    }

    private void addEntityToLocation(Graph entityGroup, EntityType entityType, LocationEntity gameEntity) {
        for (Node entity : entityGroup.getNodes(false)) {
            String entityName = entity.getId().getId().toLowerCase();
            String entityDescription = entity.getAttribute("description");
            addToSubjectList(entityName);
            gameEntity.addEntity(createEntityByType(entityType, entityName, entityDescription));
        }
    }

    private GameEntity createEntityByType(EntityType type, String name, String description) {
        return switch (type) {
            case CHARACTER -> new CharacterEntity(name, description);
            case ARTEFACT -> new ArtefactEntity(name, description);
            case FURNITURE -> new FurnitureEntity(name, description);
            default -> null;
        };
    }

    private void handlePaths(List<Edge> paths) throws NoLocationFoundToAddPathException {
        for (Edge path : paths) {
            Node fromLocation = path.getSource().getNode();
            String fromName = fromLocation.getId().getId();
            Node toLocation = path.getTarget().getNode();
            String toName = toLocation.getId().getId();
            addPathToLocation(fromName, toName);
        }
    }

    private void addPathToLocation(String location, String path) throws NoLocationFoundToAddPathException {
        if (getLocationByName(location) == null) {
            throw new NoLocationFoundToAddPathException(location);
        }
        if (getLocationByName(path) == null) {
            throw new NoLocationFoundToAddPathException(path);
        }
        locations.get(location).addPath(locations.get(path));
    }

    private void createActions(NodeList actions) {
        for (int i = 0; i < actions.getLength(); i++) {
            if (i % 2 == 0) {
                continue;
            }
            createAction((Element) actions.item(i));
        }
    }

    private void createAction(Element action) {
        Element triggers = (Element) action.getElementsByTagName("triggers").item(0);
        Element subjects = (Element) action.getElementsByTagName("subjects").item(0);
        Element consumed = (Element) action.getElementsByTagName("consumed").item(0);
        Element produced = (Element) action.getElementsByTagName("produced").item(0);
        Element narration = (Element) action.getElementsByTagName("narration").item(0);
        String message = narration.getTextContent();
        String[] phases = getPhrases(triggers);
        String[] neededItems = getEntity(subjects);
        String[] consumedItems = getEntity(consumed);
        String[] producedItems = getEntity(produced);
        GameAction newAction = new GameAction(phases, neededItems, consumedItems, producedItems, message);
        addActionToMap(phases, newAction);
    }

    private void addActionToMap(String[] phases, GameAction newAction) {
        for (String phase : phases) {
            if (actions.containsKey(phase)) {
                actions.get(phase).add(newAction);
                continue;
            }
            HashSet<GameAction> actionsHashSet = new HashSet<>();
            actionsHashSet.add(newAction);
            actions.put(phase, actionsHashSet);
        }
    }

    private String[] getPhrases(Element triggers) {
        NodeList parsedPhrased = triggers.getElementsByTagName("keyphrase");
        String[] phrases = new String[parsedPhrased.getLength()];
        for (int i = 0; i < parsedPhrased.getLength(); i++) {
            phrases[i] = parsedPhrased.item(i).getTextContent().toLowerCase();
        }
        return phrases;
    }

    private String[] getEntity(Element triggers) {
        NodeList parsedEntities = triggers.getElementsByTagName("entity");
        String[] entities = new String[parsedEntities.getLength()];
        for (int i = 0; i < parsedEntities.getLength(); i++) {
            entities[i] = parsedEntities.item(i).getTextContent().toLowerCase();
        }
        return entities;
    }

    public Storage getStoreroom() {
        return storeroom;
    }

    public LocationEntity getLocationByName(String name) {
        if (!locations.containsKey(name)) {
            return null;
        }
        return locations.get(name);
    }

    public LocationEntity findMovableEntity(String name) {
        for (HashMap.Entry<String, LocationEntity> set : locations.entrySet()) {
            LocationEntity location = set.getValue();
            boolean hasFurniture = location.hasFurnitureByName(name);
            boolean hasArtefact = location.hasArtefactByName(name);
            boolean hasCharacter = location.hasCharacterByName(name);
            if (hasCharacter || hasArtefact || hasFurniture) {
                return location;
            }
        }
        return null;
    }
    public boolean isCommand(String name) {
        CmdType cmdType = CmdType.parse(name);
        if (cmdType == null) {
            return false;
        }
        return commands.containsKey(cmdType);
    }
    public Command getCommandByName(String name) {
        if (!isCommand(name)) {
            return null;
        }
        CmdType cmdType = CmdType.parse(name);
        return commands.get(cmdType);
    }
    public boolean isValidSubject(String name) {
        return subjectList.contains(name);
    }
    public PlayerEntity matchPlayerByName(String name) {
        if (players.containsKey(name)) {
            return players.get(name);
        }
        PlayerEntity newPlayer = new PlayerEntity(name, locations.get(entry));
        players.put(name, newPlayer);
        return newPlayer;
    }

    public GameAction matchAction(List<String> actionList, List<String> subjects) throws TooManyActionsException, NoActionFoundException {
        List<GameAction> foundActions = checkActionList(actionList, subjects);
        if (foundActions.size() > 1) {
            throw new TooManyActionsException();
        }
        if (foundActions.isEmpty()) {
            throw new NoActionFoundException();
        }
        return foundActions.get(0);
    }
    private List<GameAction> checkActionList(List<String> actionList, List<String> subjects) {
        HashSet<GameAction> potentialActions = actions.get(actionList.get(0));
        List<GameAction> foundActions = new ArrayList<>();
        for (GameAction action : potentialActions) {
            boolean isMatchedAction = action.isMatchedAction(actionList, subjects);
            if (!isMatchedAction) {
                continue;
            }
            foundActions.add(action);
        }
        return foundActions;
    }
    public String[] getActionNames() {
        return actions.keySet().toArray(new String[0]);
    }
}
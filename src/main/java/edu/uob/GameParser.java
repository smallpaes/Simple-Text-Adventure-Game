package edu.uob;

import edu.uob.ParserException.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GameParser {
    String player;
    List<String> actions;
    List<String> commands;
    List<String> subjects;
    public GameParser() {
        this.actions = new ArrayList<>();
        this.subjects = new ArrayList<>();
        this.commands = new ArrayList<>();
    }

    public void parseCommand(Game game, String command) throws NoPlayerNameException, WrongOrderCommandException, TooManyCommandException, TooManyActionException, InvalidPlayerNameException {
        if (!command.contains(":")) {
            throw new NoPlayerNameException();
        }
        String[] commandArr = command.split(":");
        validateName(commandArr[0]);
        player = commandArr[0].toLowerCase();
        String[] cmdArrNoName = Arrays.copyOfRange(commandArr, 1, commandArr.length);
        String cmdNoName = String.join(":", cmdArrNoName);
        String remainingCmd = retrieveActionsFromCmd(game, cmdNoName).toLowerCase();
        parseTokens(game, remainingCmd);
    }

    private void parseTokens(Game game, String command) throws WrongOrderCommandException, TooManyCommandException, TooManyActionException {
        for (String token : command.split("\\s+")) {
            if (game.isCommand(token)) {
                validateCommand();
                commands.add(token);
            } else if (game.isValidSubject(token)) {
                subjects.add(token);
            }
        }
    }

    private String retrieveActionsFromCmd(Game game, String command) {
        String updatedCommand = command.toLowerCase();
        String[] actionList = game.getActionNames();
        for (String action : actionList) {
            String regex = "\\b" + action + "\\b";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(updatedCommand);
            boolean matchFound = matcher.find();
            if (matchFound) {
                actions.add(action);
            }
            updatedCommand = matcher.replaceAll("");
        }
        return updatedCommand;
    }

    private void validateName(String name) throws InvalidPlayerNameException {
        boolean isValidName = name.matches("^[a-zA-Z\\s'-]*$");
        if (isValidName) {
            return;
        }
        throw new InvalidPlayerNameException(name);
    }
    private void validateCommand() throws WrongOrderCommandException, TooManyActionException, TooManyCommandException {
        if (commands.size() == 1) {
            throw new TooManyCommandException();
        }
        if (!actions.isEmpty()) {
            throw new TooManyActionException();
        }
        if (!subjects.isEmpty()) {
            throw new WrongOrderCommandException();
        }
    }

    public String getPlayerName() {
        return player;
    }
    public List<String> getActions() {
        return actions;
    }
    public List<String> getSubjects() {
        return subjects;
    }
    public List<String> getCommands() {
        return commands;
    }
}

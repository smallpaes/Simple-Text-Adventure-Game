package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.objects.Graph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import edu.uob.ActionException.TooManyActionsException;
import edu.uob.ActionException.NoActionFoundException;
import edu.uob.ParserException.*;
import edu.uob.GameException.NoLocationFoundToAddPathException;

/** This class implements the STAG server. */
public final class GameServer {
    Game game;
    FileReader entityFileReader;


    private static final char END_OF_TRANSMISSION = 4;

    public static void main(String[] args) throws IOException {
        File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.GameServer(File, File)}) otherwise we won't be able to mark
    * your submission correctly.
    *
    * <p>You MUST use the supplied {@code entitiesFile} and {@code actionsFile}
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    *
    */
    public GameServer(File entitiesFile, File actionsFile) {
        try {
            ArrayList<Graph> entities = parseEntitiesFile(entitiesFile);
            NodeList actions = parseActionsFile(actionsFile);
            game = new Game();
            game.initGame(entities, actions);
            entityFileReader.close();
        } catch (NoLocationFoundToAddPathException ne) {
            System.out.println(ne.getMessage());
        } catch (ParseException pe) {
            System.out.println("Something went wrong while parsing");
        } catch (FileNotFoundException fe) {
            System.out.println("Cannot find the file to parse");
        } catch(ParserConfigurationException | SAXException pse) {
            System.out.println("Cannot parse the action file");
        } catch(IOException ioe) {
            System.out.println("Failed reading in action file");
        } catch (Exception e) {
            System.out.println("Failed while handling files");
        }
    }

    private NodeList parseActionsFile(File actionsFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(actionsFile);
        Element root = document.getDocumentElement();
        return root.getChildNodes();
    }

    private ArrayList<Graph> parseEntitiesFile(File entitiesFile) throws FileNotFoundException, ParseException {
        Parser parser = new Parser();
        entityFileReader = new FileReader(entitiesFile);
        parser.parse(entityFileReader);
        Graph wholeDocument = parser.getGraphs().get(0);
        return wholeDocument.getSubgraphs();
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.GameServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming game commands and carries out the corresponding actions.
    */
    public String handleCommand(String command) {
        try {
            GameParser parser = new GameParser();
            parser.parseCommand(game, command);

            int actionNumber = parser.getActions().size();
            int commandNumber = parser.getCommands().size();
            if (actionNumber == 0 && commandNumber == 0) {
                throw new NoActionFoundException();
            }
            String[] subjects = parser.getSubjects().toArray(new String[0]);
            PlayerEntity player = game.matchPlayerByName(parser.getPlayerName());
            if (commandNumber > 0) {
                Command cmd = game.getCommandByName(parser.getCommands().get(0));
                return cmd.execute(player, subjects);
            } else  {
                GameAction action = game.matchAction(parser.getActions(), parser.getSubjects());
                return action.execute(game, player);
            }
        } catch (ParserException.NoPlayerNameException | TooManyActionsException |
                 NoActionFoundException | CmdException | WrongOrderCommandException |
                 TooManyCommandException |
                 TooManyActionException | InvalidPlayerNameException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Something went wrong while handling this action";
        }
    }

    //  === Methods below are there to facilitate server related operations. ===

    /**
    * Starts a *blocking* socket server listening for new connections. This method blocks until the
    * current thread is interrupted.
    *
    * <p>This method isn't used for marking. You shouldn't have to modify this method, but you can if
    * you want to.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Handles an incoming connection from the socket server.
    *
    * <p>This method isn't used for marking. You shouldn't have to modify this method, but you can if
    * * you want to.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                System.out.println("Received message from " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}

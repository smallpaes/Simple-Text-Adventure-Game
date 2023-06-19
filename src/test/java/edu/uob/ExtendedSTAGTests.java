package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

class ExtendedSTAGTests {
    private GameServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    void setup() {
        File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
    }

    String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    // A lot of tests will probably check the game state using 'look' - so we better make sure 'look' works well!
    @Test
    void testLook() {
        String response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("cabin"), "Did not see the name of the current room in response to look");
        assertTrue(response.contains("sharp axe"), "Did not see a description of an artefacts 'axe' in response to look");
        assertTrue(response.contains("magic potion"), "Did not see a description of an artefacts 'potion' in response to look");
        assertTrue(response.contains("silver coin"), "Did not see description of an artefacts 'coin' in response to look");
        assertTrue(response.contains("wooden trapdoor"), "Did not see description of a furniture 'trapdoor' in response to look");
        assertTrue(response.contains("forest"), "Did not see available paths in response to look");
    }
    @Test
    void testLookRedundantSubject()
    {
        String response = sendCommandToServer("Mike: look forest");
        response = response.toLowerCase();
        assertTrue(response.contains("too many subjects"), "Did not get any warning message when performing look action with redundant subjects");
    }

    @Test
    void testLookRedundantWords()
    {
        String response = sendCommandToServer("Mike: hey mike look there");
        response = response.toLowerCase();
        assertTrue(response.contains("cabin"), "Did not see the name of the current room in response to look");
        assertTrue(response.contains("sharp axe"), "Did not see a description of an artefacts 'axe' in response to look");
        assertTrue(response.contains("magic potion"), "Did not see a description of an artefacts 'potion' in response to look");
        assertTrue(response.contains("silver coin"), "Did not see description of an artefacts 'coin' in response to look");
        assertTrue(response.contains("wooden trapdoor"), "Did not see description of a furniture 'trapdoor' in response to look");
        assertTrue(response.contains("forest"), "Did not see available paths in response to look");
    }

    // Test that we can pick something up and that it appears in our inventory
    @Test
    void testGet()
    {
        String response;
        sendCommandToServer("Mike: get potion");
        response = sendCommandToServer("Mike: inv");
        response = response.toLowerCase();
        assertTrue(response.contains("potion"), "Did not see the potion in the inventory after an attempt was made to get it");
        response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertFalse(response.contains("potion"), "Potion is still present in the room after an attempt was made to get it");
    }

    @Test
    void testValidName() {
        String response = sendCommandToServer("m i-k'e: look");
        response = response.toLowerCase();
        assertTrue(response.contains("cabin"), "Did not see the name of the current room in response to look");
        assertTrue(response.contains("sharp axe"), "Did not see a description of an artefacts 'axe' in response to look");
        assertTrue(response.contains("magic potion"), "Did not see a description of an artefacts 'potion' in response to look");
        assertTrue(response.contains("silver coin"), "Did not see description of an artefacts 'coin' in response to look");
        assertTrue(response.contains("wooden trapdoor"), "Did not see description of a furniture 'trapdoor' in response to look");
        assertTrue(response.contains("forest"), "Did not see available paths in response to look");

        sendCommandToServer("m i-k'e: get potion");
        response = sendCommandToServer("m i-k'e: inv");
        response = response.toLowerCase();
        assertTrue(response.contains("potion"), "Did not see the potion in the inventory after an attempt was made to get it");
    }
    @Test
    void testNoName() {
        String response = sendCommandToServer("look");
        response = response.toLowerCase();
        assertTrue(response.contains("no user name"), "No warning message was returned when no name was provided");
    }

    @Test
    void testInvalidName() {
        String response = sendCommandToServer("mi*ke: look");
        response = response.toLowerCase();
        assertTrue(response.contains("not a valid name"), "No warning message was returned when an invalid name was provided");

        response = sendCommandToServer("mi/ke: look");
        response = response.toLowerCase();
        assertTrue(response.contains("not a valid name"), "No warning message was returned when an invalid name was provided");

        response = sendCommandToServer("mi,ke: look");
        response = response.toLowerCase();
        assertTrue(response.contains("not a valid name"), "No warning message was returned when an invalid name was provided");
    }

    // Test that we can goto a different location (we won't get very far if we can't move around the game !)
    @Test
    void testGoto()
    {
        sendCommandToServer("Mike: goto forest");
        String response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("key"), "Failed attempt to use 'goto' command to move to the forest - there is no key in the current location");
        assertTrue(response.contains("pine tree"), "Failed attempt to use 'goto' command to move to the forest - there is no tree in the current location");
    }
    @Test
    void testGotoNoSubject()
    {
        String response = sendCommandToServer("Mike: goto");
        response = response.toLowerCase();
        assertTrue(response.contains("missing subject"), "Did not get any warning message when performing goto action without a subject");
    }
    @Test
    void testGotoRedundantSubject()
    {
        String response = sendCommandToServer("Mike: goto forest cabin");
        response = response.toLowerCase();
        assertTrue(response.contains("too many subjects"), "Did not get any warning message when performing goto action with redundant subjects");
    }
    @Test
    void testGotoNoPath()
    {
        String response = sendCommandToServer("Mike: goto riverbank");
        response = response.toLowerCase();
        assertTrue(response.contains("no path to riverbank"), "Did not get any warning message when performing goto action while the path doesn't exist'");
    }
    @Test
    void testGotoForProducedLocation()
    {
        sendCommandToServer("Mike: get coin");
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: open trapdoor key axe");
        sendCommandToServer("Mike: goto cellar");

        String response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("elf"), "Elf is not in the cellar");
        assertTrue(response.contains("cabin"), "Did not see the path to cabin in the cellar");
    }

    @Test
    void testBasicChopTree()
    {
        sendCommandToServer("Mike: get axe");
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: chop tree axe");
        String response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("log"), "Failed attempt to use 'chop' command to chop down the tree with axe - there is no log produced");
        assertFalse(response.contains("tree"), "Failed attempt to use 'chop' command to chop down the tree with axe - tree was not consumed");
    }
    @Test
    void testChopTreeWithPartialSubjectMatched()
    {
        sendCommandToServer("Mike: get axe");
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: chop axe");
        String response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("log"), "Failed attempt to use 'chop' command to chop down the tree with axe - there is no log produced");
        assertFalse(response.contains("tree"), "Failed attempt to use 'chop' command to chop down the tree with axe - tree was not consumed");
    }
    @Test
    void testChopTreeWithMultipleValidPhrases()
    {
        sendCommandToServer("Mike: get axe");
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: chop cut tree axe");
        String response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("log"), "Failed attempt to use 'chop' command to chop down the tree with axe - there is no log produced");
        assertFalse(response.contains("tree"), "Failed attempt to use 'chop' command to chop down the tree with axe - tree was not consumed");
    }
    @Test
    void testChopTreeWithRedundantPhrases()
    {
        sendCommandToServer("Mike: get axe");
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: chop cut open tree axe");
        String response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertFalse(response.contains("log"), "Failed attempt to use 'chop' command with redundant phrases - log should not be produced");
        assertTrue(response.contains("tree"), "Failed attempt to use 'chop' command with redundant phrases - tree should not be consumed");
    }
    @Test
    void testChopTreeWithoutSubjects()
    {
        sendCommandToServer("Mike: get axe");
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: chop");
        String response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertFalse(response.contains("log"), "Failed attempt to use 'chop' command without any subject - log should not be produced");
        assertTrue(response.contains("tree"), "Failed attempt to use 'chop' command without any subject - tree should not be consumed");
    }
    @Test
    void testChopTreeWithInvalidSubjects()
    {
        sendCommandToServer("Mike: get axe");
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: chop key");
        String response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertFalse(response.contains("log"), "Failed attempt to use 'chop' command with invalid subjects - log should not be produced");
        assertTrue(response.contains("tree"), "Failed attempt to use 'chop' command with invalid subjects  - tree should not be consumed");
    }
    @Test
    void testChopTreeWithTooManySubjects()
    {
        sendCommandToServer("Mike: get axe");
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: chop tree axe potion");
        String response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertFalse(response.contains("log"), "Failed attempt to use 'chop' command with too many subject - log should not be produced");
        assertTrue(response.contains("tree"), "Failed attempt to use 'chop' command with too many subject - tree should not be consumed");
    }

    @Test
    void testBasicOpenPath()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: get axe");
        sendCommandToServer("Mike: open trapdoor key and axe");

        String response = sendCommandToServer("Mike: inv");
        response = response.toLowerCase();
        assertFalse(response.contains("key"), "Still find the key in the inventory after an action that would consume the key");

        response = sendCommandToServer("Mike: look");
        assertTrue(response.contains("cellar"), "Did not see available path to cellar in response to look");
    }

    @Test
    void testOpenPathActionWithSpace()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: get axe");
        sendCommandToServer("Mike: knock down trapdoor open key and axe");

        String response = sendCommandToServer("Mike: inv");
        response = response.toLowerCase();
        assertFalse(response.contains("key"), "Still find the key in the inventory after an action that would consume the key");

        response = sendCommandToServer("Mike: look");
        assertTrue(response.contains("cellar"), "Did not see available path to cellar in response to look");
    }

    @Test
    void testOpenPathDecoratedCommands()
    {
        sendCommandToServer("Mike: please goto that forest");
        sendCommandToServer("Mike: get that key");
        sendCommandToServer("Mike: goto that weird cabin ok");
        sendCommandToServer("Mike: get that big axe");
        sendCommandToServer("Mike: need to open that cute trapdoor with my key and YOUR axe");

        String response = sendCommandToServer("Mike: check your inv if exists");
        response = response.toLowerCase();
        assertFalse(response.contains("key"), "Still find the key in the inventory after an action that would consume the key");

        response = sendCommandToServer("Mike: hey look at that");
        assertTrue(response.contains("cellar"), "Did not see available path to cellar in response to look");
    }

    @Test
    void testOpenPathReordering()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: get axe");
        sendCommandToServer("Mike: use key and axe to open trapdoor");

        String response = sendCommandToServer("Mike: inv");
        response = response.toLowerCase();
        assertFalse(response.contains("key"), "Still find the key in the inventory after an action that would consume the key");

        response = sendCommandToServer("Mike: look");
        assertTrue(response.contains("cellar"), "Did not see available path to cellar in response to look");
    }

    @Test
    void testOpenPathWithArtefactInPlace()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: open trapdoor key and axe");

        String response = sendCommandToServer("Mike: inv");
        response = response.toLowerCase();
        assertFalse(response.contains("key"), "Still find the key in the inventory after an action that would consume the key");

        response = sendCommandToServer("Mike: look");
        assertTrue(response.contains("cellar"), "Did not see available path to cellar in response to look");
    }

    @Test
    void testOpenPathAmbiguous()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: get axe");
        sendCommandToServer("Mike: open trapdoor");

        String response = sendCommandToServer("Mike: inv");
        response = response.toLowerCase();
        assertTrue(response.contains("key"), "An ambiguous command is made, but the artefact 'key' is gone");
        assertTrue(response.contains("axe"), "An ambiguous command is made, but the artefact 'axe' is gone");

        response = sendCommandToServer("Mike: look");
        assertFalse(response.contains("cellar"), "An ambiguous command is made, but the path to the cellar is there");
    }
    @Test
    void testOpenPathWithKeyInOtherPlayersInv()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("John: get axe");
        String response = sendCommandToServer("Mike: open trapdoor key and axe");
        response = response.toLowerCase();
        assertTrue(response.contains("items are not available"), "Did not get any warning message when performing an action with other player possessing the item");
    }
    @Test
    void testBasicDrop()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: drop key");

        String response = sendCommandToServer("Mike: inv");
        response = response.toLowerCase();
        assertFalse(response.contains("key"), "Still find the key in the inventory after an action to drop it");

        response = sendCommandToServer("Mike: look");
        assertTrue(response.contains("key"), "Did not see key in response to look after an action to drop it");
    }
    @Test
    void testDropNotInInventory()
    {
        String response = sendCommandToServer("Mike: drop key");
        assertTrue(response.contains("not exist"), "Does not see a warning message when dropping something that is not in inventory");
        response = sendCommandToServer("Mike: look");
        assertFalse(response.contains("key"), "Saw a key being dropped, but it wasn't in player's inventory");
    }
    @Test
    void testDropNoSubject()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: drop");

        String response = sendCommandToServer("Mike: inv");
        response = response.toLowerCase();
        assertTrue(response.contains("key"), "Key is not in the inventory after an invalid drop without a subject");

        response = sendCommandToServer("Mike: look");
        assertFalse(response.contains("key"), "Key is in the current location after an invalid drop without a subject");
    }
    @Test
    void testDropRedundantSubject()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: drop key axe");

        String response = sendCommandToServer("Mike: inv");
        response = response.toLowerCase();
        assertTrue(response.contains("key"), "Key is not in the inventory after an invalid drop with redundant subjects");

        response = sendCommandToServer("Mike: look");
        assertFalse(response.contains("key"), "Key is in the current location after an invalid drop with redundant subjects");
    }

    @Test
    void testBasicGet()
    {
        sendCommandToServer("Mike: get axe");
        String response = sendCommandToServer("Mike: inv");
        response = response.toLowerCase();
        assertTrue(response.contains("axe"), "Axe is not in the inventory after an action to get it");

        response = sendCommandToServer("Mike: look");
        assertFalse(response.contains("axe"), "Axe is still in the location after an action to get it");
    }
    @Test
    void testGetFurniture()
    {
        String response = sendCommandToServer("Mike: get trapdoor");
        response = response.toLowerCase();
        assertTrue(response.contains("does not exist"), "Did not show a warning message when trying to pick up an artefacts");
    }
    @Test
    void testGetNotExistingSubject()
    {
        String response = sendCommandToServer("Mike: get key");
        response = response.toLowerCase();
        assertTrue(response.contains("key does not exist"), "Did not get any warning message when performing get command without a subject");
    }
    @Test
    void testGetNoSubject()
    {
        String response = sendCommandToServer("Mike: get");
        response = response.toLowerCase();
        assertTrue(response.contains("missing subject"), "Did not get any warning message when performing get command without a subject");
    }
    @Test
    void testGetRedundantSubject()
    {
        String response = sendCommandToServer("Mike: get axe key");
        response = response.toLowerCase();
        assertTrue(response.contains("too many subjects"), "Did not get any warning message when performing get command with redundant subjects");
    }
    @Test
    void testInventoryRedundantSubject()
    {
        String response = sendCommandToServer("Mike: inventory key");
        response = response.toLowerCase();
        assertTrue(response.contains("too many subjects"), "Did not get any warning message when performing inventory command with redundant subjects");
    }

    @Test
    void testBasicInventory()
    {
        sendCommandToServer("Mike: get axe");
        String response = sendCommandToServer("Mike: inventory");
        response = response.toLowerCase();
        assertTrue(response.contains("axe"), "Axe is not in the inventory after an action to get it");

        response = sendCommandToServer("Mike: look");
        assertFalse(response.contains("axe"), "Axe is still in the location after an action to get it");
    }

    @Test
    void testProposeWithUnavailableProducedItem()
    {
        sendCommandToServer("Mike: propose coin");
        String response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("ring"), "Did not see ring after propose");
        sendCommandToServer("Mike: get ring");
        response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertFalse(response.contains("ring"), "Saw the ring after getting it");

        response = sendCommandToServer("John: propose coin");
        assertTrue(response.contains("don't have sufficient subjects"), "No warning message was returned after proposing with unavailable items produced");
    }
    @Test
    void testProposeWithProducedItemDroppedElseWhere()
    {
        sendCommandToServer("Mike: propose coin");
        String response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("ring"), "Did not see ring after propose");
        sendCommandToServer("Mike: get ring");
        response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertFalse(response.contains("ring"), "Saw the ring after getting it");

        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: drop ring");
        response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("ring"), "Ring is not found in the current location after dropping it");
        response = sendCommandToServer("Mike: inventory");
        response = response.toLowerCase();
        assertFalse(response.contains("ring"), "Ring is still in the inventory after dropping it");

        sendCommandToServer("Mike: goto cabin");
        response = sendCommandToServer("John: propose coin");
        response = response.toLowerCase();
        assertTrue(response.contains("proposed successfully"), "Did not get a successful message after proposing");
        response = sendCommandToServer("John: look");
        response = response.toLowerCase();
        assertTrue(response.contains("ring"), "Did not see ring after propose");
    }

    @Test
    void testHealth()
    {
        String response = sendCommandToServer("Mike: health");
        assertTrue(response.contains("3"), "Failed attempt to use 'health' command to get health level - there is no number being returned");
    }
    @Test
    void testHealthRedundantSubject()
    {
        String response = sendCommandToServer("Mike: health key");
        response = response.toLowerCase();
        assertTrue(response.contains("too many subjects"), "Did not get any warning message when performing health command with redundant subjects");
    }
    @Test
    void testDecreaseHealthLevel()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: open trapdoor key axe");
        sendCommandToServer("Mike: goto cellar");
        String response = sendCommandToServer("Mike: hit elf");
        assertTrue(response.contains("You attack the elf, but he fights back and you lose some health"), "Did not get a response message from the action to hit elf");
        response = sendCommandToServer("Mike: health");
        assertTrue(response.contains("2"), "Health level should be 2 after an action that would cause the health level to decrease");
    }
    @Test
    void testIncreaseHealthLevel()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: open trapdoor key axe");
        sendCommandToServer("Mike: goto cellar");
        sendCommandToServer("Mike: hit elf");
        String response = sendCommandToServer("Mike: health");
        assertTrue(response.contains("2"), "Health level should be 2 after an action that would cause the health level to decrease");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: drink potion");
        response = sendCommandToServer("Mike: health");
        assertTrue(response.contains("3"), "Health level should be 3 after an action that would cause the health level to increase");
    }
    @Test
    void testIncreaseHealthLevelAlreadyCap()
    {
        sendCommandToServer("Mike: drink potion");
        String response = sendCommandToServer("Mike: health");
        assertTrue(response.contains("3"), "Health level should be 3 after an action that would cause the health level to increase while it's already 3");
    }
    @Test
    void testGameOver()
    {
        sendCommandToServer("Mike: get coin");
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: open trapdoor key axe");
        sendCommandToServer("Mike: goto cellar");
        sendCommandToServer("Mike: hit elf");
        sendCommandToServer("Mike: hit elf");
        String response = sendCommandToServer("Mike: health");
        assertTrue(response.contains("1"), "Health level should be 2 after an action that would cause the health level to decrease");

        response = sendCommandToServer("Mike: hit elf");
        assertTrue(response.contains("You died and lost all of your items"), "Did not receive game over message when life level is down to 0");
        response = sendCommandToServer("Mike: health");
        assertTrue(response.contains("3"), "Health level should be reset to 3 after game over");
        response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("cabin"), "Player is not being brought back to initial place after game over");
        sendCommandToServer("Mike: goto cellar");
        response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("coin"), "Items in the player's inventory was not being dropped at the location they died");
    }
    @Test
    void testProduceItemInOtherLocations()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: goto riverbank");
        String response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("horn"), "Did not see horn after going to riverbank");
        sendCommandToServer("Mike: blow horn");
        response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("burly wood cutter"), "Did not see lumberjack after blowing the horn");

        sendCommandToServer("Mike: get horn");
        response = sendCommandToServer("Mike: inv");
        response = response.toLowerCase();
        assertTrue(response.contains("horn"), "Did not see the horn in the inventory after an attempt was made to get it");
        sendCommandToServer("Mike: goto forest");
        response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        response = response.toLowerCase();
        assertTrue(response.contains("tree"), "Did not see a tree in the forest");
        assertFalse(response.contains("burly wood cutter"), "Should not see lumberjack in the forest");
        sendCommandToServer("Mike: blow horn");
        response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("burly wood cutter"), "Did not see lumberjack after blowing the horn");
    }

    @Test
    void testBlockPath()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: block riverbank");
        String response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertFalse(response.contains("riverbank"), "Should not see the path to riverbank after blocking the path");
    }
    @Test
    void testConsumeItemInOtherLocations()
    {
        sendCommandToServer("Mike: get coin");
        String response = sendCommandToServer("Mike: inv");
        response = response.toLowerCase();
        assertTrue(response.contains("coin"), "Did not see the coin in the inventory after an attempt was made to get it");

        response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertFalse(response.contains("coin"), "Still see coin in the cabin after picking it up");
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: drop coin");
        response = sendCommandToServer("Mike: inv");
        response = response.toLowerCase();
        assertFalse(response.contains("coin"), "Still see the coin in the inventory after an attempt was made to drop it");
        sendCommandToServer("Mike: goto cabin");
        response = sendCommandToServer("Mike: earn potion");
        assertTrue(response.contains("money"), "Should get some monet after an attempt to earn potion");

        response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("million dollar"), "Did not see money in the cabin after an attempt to earn potion");
    }

    @Test
    void testNoActionAndSubjects()
    {
        String response = sendCommandToServer("Mike: hello there");
        response = response.toLowerCase();
        assertTrue(response.contains("no matched action"), "Did not see any warning message for commands without action and subject specified");
    }
    @Test
    void testMultipleCommands()
    {
        String response = sendCommandToServer("Mike: inv get drop");
        response = response.toLowerCase();
        assertTrue(response.contains("more than one commands"), "Did not see any warning message when going with multiple commands");
    }
    @Test
    void testCommandWithAction()
    {
        String response = sendCommandToServer("Mike: inv open");
        response = response.toLowerCase();
        assertTrue(response.contains("more than one actions"), "Did not see any warning message when going with one command and one action");
    }
    @Test
    void testCaseInsensitiveCommand()
    {
        String response = sendCommandToServer("Mike: LoOk");
        response = response.toLowerCase();
        assertTrue(response.contains("cabin"), "Did not see the name of the current room in response to LoOk");

        sendCommandToServer("Mike: GeT axe");
        response = sendCommandToServer("Mike: InVenTory");
        response = response.toLowerCase();
        assertTrue(response.contains("axe"), "Axe is not in the inventory after an action to GeT it");

        sendCommandToServer("Mike: DroP axe");
        response = sendCommandToServer("Mike: INV");
        response = response.toLowerCase();
        assertFalse(response.contains("axe"), "Still find the axe in the inventory after an action to DroP it");

        sendCommandToServer("Mike: GOTO forest");
        response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("key"), "Failed attempt to use 'GOTO' command to move to the forest - there is no key in the current location");
    }

    @Test
    void testCaseInsensitiveAction()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: get axe");
        sendCommandToServer("Mike: OpEn trapdoor key and axe");
        String response = sendCommandToServer("Mike: look");
        assertTrue(response.contains("cellar"), "Did not see available path to cellar in response to look");
    }
    @Test
    void testCaseInsensitiveLocation()
    {
        sendCommandToServer("Mike: goto fOreSt");
        String response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("key"), "Failed attempt to use 'goto' command to move to the forest - there is no key in the current location");
        assertTrue(response.contains("pine tree"), "Failed attempt to use 'goto' command to move to the forest - there is no tree in the current location");
    }
    @Test
    void testCaseInsensitiveGet()
    {
        sendCommandToServer("Mike: get aXE");
        String response = sendCommandToServer("Mike: inv");
        response = response.toLowerCase();
        assertTrue(response.contains("axe"), "Axe is not in the inventory after an action to get it");
    }
    @Test
    void testCaseInsensitiveSubject()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: get axe");
        sendCommandToServer("Mike: open tRapdOor kEy and AXE");
        String response = sendCommandToServer("Mike: look");
        assertTrue(response.contains("cellar"), "Did not see available path to cellar in response to look");
    }

    @Test
    void testCommandOutOfOrder()
    {
        String response = sendCommandToServer("Mike: look");
        response = response.toLowerCase();
        assertTrue(response.contains("cabin"), "Did not see the name of the current room in response to LoOk");

        response = sendCommandToServer("Mike: axe get");
        response = response.toLowerCase();
        assertTrue(response.contains("out of order"), "Did not see an warning message in response to an unordered command");
        response = sendCommandToServer("Mike: inv");
        response = response.toLowerCase();
        assertFalse(response.contains("axe"), "Should not see an axe when an unordered command is performed");
    }
    @Test
    void testUseReservedWord()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: open trapdoor key axe");
        sendCommandToServer("Mike: goto cellar");
        String response = sendCommandToServer("Mike: fight elf health");
        assertTrue(response.contains(" more than one actions"), "Did not get a warning message when using reserved words for an action");
        response = sendCommandToServer("Mike: health fight elf");
        assertTrue(response.contains("more than one actions"), "Did not get a warning message when an action comes after a command");
    }
    @Test
    void testMultiPlayers()
    {
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("John: get axe");
        String response = sendCommandToServer("Mike: open trapdoor key and axe");
        response = response.toLowerCase();
        assertTrue(response.contains("items are not available"), "Did not get any warning message when performing an action with other player possessing the item");
    }

    @Test
    void testGameOverMultiPlayers()
    {
        sendCommandToServer("Mike: get coin");
        sendCommandToServer("Mike: get potion");
        sendCommandToServer("Mike: goto forest");
        sendCommandToServer("Mike: get key");
        sendCommandToServer("Mike: goto cabin");
        sendCommandToServer("Mike: open trapdoor key axe");
        String response = sendCommandToServer("Mike: inv");
        assertFalse(response.contains("key"), "Key is still in the inventory after an action that would consume it");
        sendCommandToServer("Mike: goto cellar");
        sendCommandToServer("Mike: hit elf");
        response = sendCommandToServer("John: goto cellar");
        assertTrue(response.contains("dusty cellar"), "Not in a cellar after an action to go there");
        sendCommandToServer("john:  hit elf");
        response = sendCommandToServer("JohN: health");
        assertTrue(response.contains("2"), "Health level should be 2 after an action that would cause the health level to decrease");
        sendCommandToServer("Mike: hit elf");
        response = sendCommandToServer("Mike: health");
        assertTrue(response.contains("1"), "Health level should be 1 after an action that would cause the health level to decrease");

        response = sendCommandToServer("Mike: hit elf");
        assertTrue(response.contains("You died and lost all of your items"), "Did not receive game over message when life level is down to 0");

        response = sendCommandToServer("john: look");
        response = response.toLowerCase();
        assertTrue(response.contains("potion"), "Did not find potion after Mike is game over");

        response = sendCommandToServer("john: go potion to drink that");
        response = response.toLowerCase();
        assertTrue(response.contains("health improves"), "Did not get a message indicating health is improved after an action that would increase it");

        response = sendCommandToServer("JohN: health");
        assertTrue(response.contains("3"), "Health level should be 2 after an action that would cause the health level to decrease");
    }
}

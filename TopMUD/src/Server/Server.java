package Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Commands.Command;
import Environment.GameObject;
import Environment.Mobs.Mob;
import Environment.Mobs.Friendly.Player;
import Environment.World.Room;
import Environment.World.World;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.*;
import java.nio.file.FileAlreadyExistsException;
import java.time.LocalDateTime;

public class Server {
    private EventHandler cManager;
    private static World loadedWorld;
    private static Map<String, Command> commandMap;
    private static String SAVE_DIR;
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    /** The standard method for creating a server object.
     * 
     * @param dir The root path for the directory where files should be saved
     * @param toLoad The name of the world file to be loaded on initialization
     */
    public Server(String dir, String toLoad)
    {
        SAVE_DIR = dir+"\\";
        LOGGER.log(Level.INFO, SAVE_DIR);
        cManager = new EventHandler();
        cManager.start();
        commandMap = new HashMap<String, Command>();
        commandsInit();
        loadWorld(toLoad);
    }

    /** Call for the start of a new server. This begins operation for eventhandling and send/recieve functions
     * 
     * @throws IOException
     */
    public void start() throws IOException
    {
        LOGGER.log(Level.INFO, "Server starting...");
        try (ServerSocket serverSocket = new ServerSocket(7778);
        ) {
            while (true) {
                Connection c = new Connection(serverSocket.accept());
                cManager.connect(c);
                c.start(); // starts respond to the client on another thread

                LOGGER.log(Level.INFO, "New connection started...");
            }

        } catch(IOException e) {
            LOGGER.log(Level.SEVERE, "Error on server start", e);
            saveFile(loadedWorld, SAVE_DIR+"World\\"+loadedWorld.getName()+".world");
            cManager.stopRunning();
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /** Primary getter method so other objects can use the sole logger (streamlines the logging system)
     * 
     * @return The primary logger
     */
    public static Logger getLogger(){return LOGGER;}

    /** Gets only the world currently loaded to the server. This is useful for getting world data in command handling
     * 
     * @return The currently loaded world object
     */
    public static World getWorld(){return loadedWorld;}
    
    private class EventHandler extends Thread
    {
        private List<Connection> connections;
        private HashMap<Mob, List<String>> commandStack;
        private boolean isRunning;

        public EventHandler() 
        {
            connections = new ArrayList<Connection>();
            commandStack = new HashMap<Mob, List<String>>();
            isRunning = true;
        }

        public void run() {
            while (isRunning) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Failed to sleep.", e);
                    e.printStackTrace();
                }
    
                if (!commandStack.isEmpty()) {
                    List<Map.Entry<Mob, List<String>>> entriesToRemove = new ArrayList<>();
    
                    for (Map.Entry<Mob, List<String>> entry : commandStack.entrySet()) {
                        Mob executor = entry.getKey();
                        List<String> commands = entry.getValue();
    
                        for (String current : commands) {
                            LOGGER.log(Level.INFO, "Handling command " + current);
    
                            String[] args = current.split(" ", 2);
    
                            if (commandMap.containsKey(args[0]))
                                commandMap.get(args[0]).execute(executor, args[1].split(""));
                            else
                                LOGGER.log(Level.WARNING, "Invalid command syntax::" + current);
                        }
    
                        entriesToRemove.add(entry);
                    }
    
                    for (Map.Entry<Mob, List<String>> entry : entriesToRemove) {
                        commandStack.remove(entry.getKey());
                    }
                }
            }
        }

        // Close the event handler. This terminates the ability to perform IO
        public void stopRunning()
        {
            isRunning = false;
        }

        /** Recognize a new connection as a valid IO host.
         * 
         * @param c The connection object to be included in the valid list
         */
        public void connect(Connection c)
        {
            connections.add(c);
        }

        /** Add a new command request to the available stack of commands. This has since been updated to handle the command as it is added, so there might be name
         * clarification needed
         * 
         * TODO find a better name for this method
         * 
         * @param other The mob who called the command. Primarly a Player in this early version, though all mobs should be able to call for a combat implementation
         * @param toCall The command string to be parsed and handled. This is split into a call and a list of arguments
         */
        public void addToStack(Mob other, String toCall) {
            LOGGER.log(Level.INFO, "Handling command " + toCall);
    
            String[] args = toCall.split(" ", 2);
            if(args.length < 2) args = new String[]{args[0], ""};
    
            if (commandMap.containsKey(args[0]))
                commandMap.get(args[0]).execute(other, args[1].split(" "));
            else
                LOGGER.log(Level.WARNING, "Invalid command syntax::" + toCall);
        }

        /** Get the index of a given player in the connecitons list by name
         * 
         * @param name The name of the player to retrieve
         * @return The index representing the location of a player with the corresponding name in the list
         */
        public int getIndex(String name)
        {
            for(int i = 0; i < connections.size(); i++) if(name.equals(connections.get(i).getPlayer().getName())) return i;
            return -1;
        }

        /** Get the index of a given player in the connecitons list by object
         * 
         * @param player The Player object to retrieve the index of
         * @return The index representing the location of a player with the corresponding name in the list
         */
        public int getIndex(Player player)
        {
            for(int i = 0; i < connections.size(); i++) if(player.equals(connections.get(i).getPlayer())) return i;
            return -1;
        }

        /** Get the connection object at a player's index in the list by name
         * 
         * @param name The name of the player belonging to the desired connection
         * @return The desired connection
         */
        public Connection getConnection(String name)
        {
            int i = getIndex(name);
            if(i>=0) return connections.get(i);
            return null;
        }
        
        /** Get the connection object at a player's index in the list by object
         * 
         * @param player The player object belonging to the desired connection
         * @return The desired connection
         */
        public Connection getConnection(Player player)
        {   
            int i = getIndex(player);
            if(i>=0) return connections.get(i);
            return null;
        }

        /** Get a list of all connections in the server. This proves especially useful for global command handling
         * 
         * @return The list of every connection currently available on the active server
         */
        public List<Connection> getAllConnections()
        {
            return connections;
        }

        /* This is unimplemented since it can cause bugs when a player forcefully disconnects that I didn't have time to fix.
        public List<String> getAllConnectionNames()
        {
            List<String> names = new ArrayList<String>();
            for (Connection c : connections) {
                names.add(c.getPlayer().getName());
            }
            return names;
        }
        */

        /** Handle the disconnect of and then remove from the list the connection of a player by index
         * 
         * @param i The index in the connections list of the player to be disconnected
         * @throws IOException
         */
        public void disconnect(int i) throws IOException
        {
            connections.get(i).saveAndQuit();
            connections.remove(i);
        }
    }

    // This class aims to streamline any input and output for a player on the server. Each connection is handled by the EventHandler of a Server
    private class Connection extends Thread
    {
        // The current state of this connection. This represents a players progress through the login sequence
        private ConnectionStates cs = ConnectionStates.AWAITING_NAME;

        private Socket s;
        private String ms;
        private String inms;
        private Player thisPlayer;

        private BufferedReader is;
        private PrintWriter os;

        /** Create a new connection to handle information to and from the client
         * 
         * @param s The socket by which the client has connected to the server
         */
        private Connection(Socket s) 
        {
            this.s = s;
            try{
                is = new BufferedReader(new InputStreamReader(s.getInputStream()));
                os = new PrintWriter(s.getOutputStream(), true);
            } catch(IOException e){}
        }

        @Override
        public void run()
        {
            LOGGER.log(Level.INFO, "Connection thread running...");
            String input="";
            LOGGER.log(Level.INFO, "Waiting for login");
            os.println("Please input your username (\"new\" to generate a new character): ");
            try {
                while((input = is.readLine()) != null && cs != ConnectionStates.PLAYING) //handle this login while the server is recieving input
                {
                    switch(cs)
                    {
                        case AWAITING_NAME:
                            checkUser(input);
                            break;

                        case AWAITING_PASSWORD:
                            checkPass(input);
                            break;
                                
                        case AWAITING_NEW_NAME:
                            checkNewName(input);
                                break;
                                
                        case AWAITING_NEW_PASSWORD:
                            checkNewPass(input);
                            break;
                        /* This state is unused. During further development, add a character creator by classes

                        case AWAITING_NEW_CHARACTER: 
                            checkNewCharacter(input);
                            break;

                         */ 
                        default:
                            LOGGER.log(Level.WARNING, "Current thread error. Closing...");
                            savePlayer();
                            break;
                    }
                }

                while(cs == ConnectionStates.PLAYING) playing(); //run the game for a player once they are logged in

            } catch (ClassNotFoundException | IOException e) {
                LOGGER.log(Level.FINER, "Connection Closing.");
                e.printStackTrace();
                savePlayer();
            }
        }

        // Serialize this player's data in their own .player file. The file name is the same as the player name
        private void savePlayer()
        {
            saveFile(thisPlayer, SAVE_DIR+"Users\\"+thisPlayer.getName().toLowerCase()+".player");
        }

        /** Save a player's data to the server machine and quit the experience
         * 
         * @throws IOException
         */
        private void saveAndQuit() throws IOException
        {
            savePlayer();
            s.close();
            is.close();
            os.close();
        }

        /** Take input from the user to attempt to find a viable player by a given name
         * 
         * @param input The input from the client. This needs to be a viable name or "new" to progress
         * @throws FileNotFoundException
         * @throws IOException
         * @throws ClassNotFoundException
         */
        private void checkUser(String input) throws FileNotFoundException, IOException, ClassNotFoundException
        {
            LOGGER.log(Level.INFO, input);
            File f;
            if("new".equals(input)) // Create a new character
            {
                cs = ConnectionStates.AWAITING_NEW_NAME;
                LOGGER.log(Level.INFO, "Generating new character");
                os.println("Please enter a name for your new character (\"back\" to return):");
            }
            
            // Progress login into this account
            else if((f = new File(SAVE_DIR+"Users\\"+input.toLowerCase()+".player")).isFile())  
            {
                cs = ConnectionStates.AWAITING_PASSWORD;
                FileInputStream fis = new FileInputStream(f);
                ObjectInputStream ois = new ObjectInputStream(fis);
                    thisPlayer = (Player) ois.readObject();
                    ois.close();
                    fis.close();
                os.println("Please input your password (back to return): ");
            }
            
            else // The username does not belong to a player save or the desired player is already connected
            {
                LOGGER.log(Level.WARNING, "Invalid user");
                os.println("Invalid username - please try again!\n");
            }
        }

        /** Check to see if the given password corresponds to the desired player file
         * 
         * @param input The input from the client. This needs to be the correct password to progress. It can be "back" to regress
         * @throws IOException
         * @throws FileAlreadyExistsException
         */
        private void checkPass(String input) throws IOException, FileAlreadyExistsException
        {
            
            if("back".equals(input)) // regress to name check
            {
                cs = ConnectionStates.AWAITING_NAME;
                savePlayer();
                thisPlayer = null;
                os.println("Please input your username (\"new\" to generate a new character): ");
            }
        
            else if(input.equals(thisPlayer.getPassword())) // proceed to the game
            {
                LOGGER.log(Level.INFO, "Player "+thisPlayer.getName()+" logged::"+LocalDateTime.now());
                cs = ConnectionStates.PLAYING;
                os.println("Welcome! Press enter to continue.");
            }
        
            else // The password is incorrect or wrongly formatted
                os.println("Invalid password - please try again!\n");
        }

        /** Create a new character
         * 
         * @param input The input from the client. Must be a valid, unused name to progress. Can be "back" to regress
         */
        private void checkNewName(String input)
        {
            if("back".equals(input)) // regress to name check
            {
                cs = ConnectionStates.AWAITING_NAME;
                os.println("Please input your username (\"new\" to generate a new character): ");
            }   

            else if((new File("C:\\Users\\Toppe\\Documents\\GitHub\\TopMUD\\TopMUD\\rsc\\Users\\"+input.toLowerCase()+".player")).isFile()) // name is taken
            {
                os.println("Character already exists.");
                checkNewName("back");
            }

            else // valid name - create a new character
            {
                thisPlayer = new Player(input);
                cs = ConnectionStates.AWAITING_NEW_PASSWORD;
                os.println("New character \""+input.toUpperCase()+"\" created. Please set a password: ");
            }
        }

        /** Assign a password to a new character and save the character as valid
         * 
         * @param input The input from the client. Can be any String
         * TODO consider adding password limits?
         */
        private void checkNewPass(String input)
        {
            thisPlayer.setPassword(input);
            os.println("Save for later - NAME: "+thisPlayer.getName()+", PASS: "+thisPlayer.getPassword());
            os.println("Welcome! Press enter to continue.");
            savePlayer();
            cs=ConnectionStates.PLAYING;
        }

        /* This is the method for handling the unused character creator

        private void checkNewCharacter(String input)
        {
            savePlayer();
            cs=ConnectionStates.PLAYING;
        }
        */

         /** Run the game for this character. Recieve messages from the clientside and send them out as well
          * 
          * @throws IOException
          */
        private void playing() throws IOException
        {
            //if(is.ready()) inms = is.readLine();
            if(is.ready()) inms = is.readLine();
            if(!"".equals(inms) && inms != null)
            {
                LOGGER.log(Level.INFO, "From " + thisPlayer.getName() + "::" + inms);
                cManager.addToStack(thisPlayer, inms);
                inms=null;
            } 

            if(pendingSend()) 
            {
                os.println(ms);
                ms="";
            }
        }

        /** Get the player assigned to this connection
         * 
         * @return This current player
         */
        public Player getPlayer()
        {
            return thisPlayer;
        }

        /** Assign a message to be sent out to the client. Will only be sent to the player during the "PLAYING" state
         * 
         * @param msg The message to be sent
         */
        public void outMessage(String msg)
        {
            ms = msg;
        }

        /** Check if there is a message to be sent to the client
         * 
         * @return True if there is a non-empty String queued
         */
        private boolean pendingSend()
        {
            return !("".equals(ms) || ms == null);
        }
    }

    /** Initialize all of the valid commands for a player on the server
     * TODO privatize admin commands?
     */
    private void commandsInit()
    {
        /** Save and quit the player from the game
         * 
         *  NO ARGS
         */
        commandMap.put("quit", (player, args)-> {
            try {
                cManager.disconnect(cManager.getIndex((Player) player));
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        });

        /** Send the player a String copy of their inventory
         * 
         *  NO ARGS
         */
        commandMap.put("inv", (player, args)-> cManager.getConnection((Player) player).outMessage(((Player) player).getInventory()));

        /** Make the desired player an operator
         * 
         *  ARGS
         *  *0 - The player to be made operator. This must be a valid name of a player on the server to execute
         */
        commandMap.put("op", (player, args) -> {
            if(args[0].equals(""))((Player) player).op(true);
            else if(cManager.getConnection(args[0]) != null) cManager.getConnection(args[0]).getPlayer().op(true);
            else cManager.getConnection((Player) player).outMessage("Could not find player "+args[0]);
        });

        /** Remove operator priveleges from a player
         * 
         *  ARGS
         *  *0 - The player whose operator status should be removed. Must be the valid name of a player on the server to execute
         */
        commandMap.put("deop", (player, args) -> {
            if(args[0].equals(""))((Player) player).op(false);
            else if(cManager.getConnection(args[0]) != null) cManager.getConnection(args[0]).getPlayer().op(false);
            else cManager.getConnection((Player) player).outMessage("Could not find player "+args[0]);
        });

        /** Move the player around the map
         * 
         *  ARGS
         *  0 - The direction for the player to move. Only [n, e, s, w] are valid directions
         */
        commandMap.put("move", (player, args) -> {
            
                if("n".equals(args[0])) ((Player) player).move(0, -1, loadedWorld.getLimit());
           else if("e".equals(args[0])) ((Player) player).move(1, 0, loadedWorld.getLimit());
           else if("s".equals(args[0])) ((Player) player).move(0, 1, loadedWorld.getLimit());
           else if("w".equals(args[0])) ((Player) player).move(-1, 0, loadedWorld.getLimit());
            else cManager.getConnection((Player) player).outMessage("Invalid direction. Valid: [n, e, s, w]");
        });

        /** Look at the player's surroundings
         * 
         *  NO ARGS
         */
        commandMap.put("look", (player, args) -> {
            loadedWorld.getRoom(((Player) player).getPosition()).getView();
        });

        /** Save the player's data to their player object file
         * 
         * NO ARGS
         */
        commandMap.put("save", (player, args) -> {cManager.getConnection((Player) player).savePlayer();});

        /** Message another player on the server
         * 
         *  ARGS
         *  0 - The player to send the message to
         *  1... - The message to be sent
         */
        commandMap.put("msg", (player, args) ->
        {
            Connection exe = cManager.getConnection((Player) player);
            Connection out = cManager.getConnection(args[0]);
            if(args.length < 2) exe.outMessage("Invalid Syntax");
            else if(out != null) 
            {
                String s = "";
                for(int i = 1; i < args.length; i++) s+=args[i]+" ";
                out.outMessage(s);
            }
            else exe.outMessage("Could not find player "+args[0]);
        });

        /** Message all other players on the server
         * 
         *  ARGS
         *  0... - The message to be sent
         */
        commandMap.put("shout", (player, args) ->
        {
            if(args[0].equals("")) cManager.getConnection((Player) player).outMessage("Invalid Syntax");
            else 
            {
                String s = "";
                for(int i = 0; i < args.length; i++) s+=args[i]+" ";
                for(Connection c : cManager.getAllConnections()) c.outMessage("From "+player.getName()+": "+s);
            }
        });

        /** Save the world to its file. OPERATOR
         * 
         *  NO ARGS
         */
        commandMap.put("savew", (player, args) ->
        {
            Player p = (Player) player;
            Connection c = cManager.getConnection(p);
            if(p.isOp()) 
            {
                saveFile(loadedWorld, SAVE_DIR+"World\\"+loadedWorld.getName()+".world");
                c.outMessage("World saved");
            }
            else c.outMessage("This command is operator only");
        });

        /** Spawn an object to a desired room. OPERATOR
         * 
         *  ARGS
         *  0 - The room coordinates in which to spawn, <x>:<y>
         *  1 - The name of the object
         *  2 - The description of the object
         *  3 - The view of the object
         * 
         *  TODO make ARGS [1,2,3] work with more than one word
         */
        commandMap.put("addObject", (player, args) ->
        {
            Player p = (Player) player;
            Connection c = cManager.getConnection(p);
            if(!p.isOp()) {c.outMessage("This command is operator only");return;}
            if(args.length < 4) {c.outMessage("Invalid Syntax");return;}

            String[] pos = args[0].split(":");
            if(pos.length < 2) {c.outMessage("Invalid Syntax");return;}

            int x, y;
            try{
                x = Integer.parseInt(pos[0]);
                y = Integer.parseInt(pos[1]);
            }catch(Exception e){c.outMessage("Invalid Syntax");return;}

            if(!loadedWorld.isValidPosition(new int[]{y,x})){c.outMessage("Choose a valid location");return;}
            
            for(int i = 1; i <= 3; i++) args[i].replace("_", " ");
            Room r = loadedWorld.getRoom(new int[]{y, x});
            if(r == null){c.outMessage("Room invalid - assign room a value first");return;}
            loadedWorld.getRoom(new int[]{y, x}).addEntity(new GameObject(args[1], args[2], args[3]));
            c.outMessage("Created!");
        });

        /** Set the room conditions at a given coordinate. OPERATOR
         * 
         *  ARGS
         *  0 - The room coordinates, <x>:<y>
         *  1 - The name of the room
         *  2 - The description of the room
         *  3 - The view of the room
         * 
         * TODO make ARGS [1,2,3] work with more than one word
         */
        commandMap.put("setRoom", (player, args) ->
        {
            Player p = (Player) player;
            Connection c = cManager.getConnection(p);
            
            if(!p.isOp()) {c.outMessage("This command is operator only");return;}
            if(args.length < 4) {c.outMessage("Invalid Syntax");return;}

            String[] pos = args[0].split(":");
            if(pos.length < 2) {c.outMessage("Invalid Syntax");return;}

            int x, y;
            try{
                x = Integer.parseInt(pos[0]);
                y = Integer.parseInt(pos[1]);
            }catch(Exception e){c.outMessage("Invalid Syntax");return;}

            if(!loadedWorld.isValidPosition(new int[]{y,x})){c.outMessage("Choose a valid location");return;}

            for(int i = 1; i <= 3; i++) args[i].replace("_", " ");
            loadedWorld.writeRoom(new Room(args[1], args[2], args[3]), y, x);
            c.outMessage("Created!");
        });

        /** Get one's current coordinates
         *  
         *  NO ARGS
         */
        commandMap.put("getPos", (player, args) ->
        {
            Player p = (Player) player;
            cManager.getConnection(p).outMessage("Your coordinates are "+p.getPosition()[0]+":"+p.getPosition()[1]);
        });

        /** Send the player a valid list of commands
         * 
         *  NO ARGS
         *  TODO make sure that op commands are not listed as valid for the regular player
         */
        commandMap.put("help", (player, args) ->
        {
            String s = "\nVALID COMMANDS\n\n";
            for(String com : commandMap.keySet()) s += com+"\n";
            cManager.getConnection((Player) player).outMessage(s);
        });
    }

    /** Load a world to be played on the server by name
     * 
     * @param toLoad The name of the world file to be loaded
     */
    private void loadWorld(String toLoad) 
    {
        File f;
        if((f = new File(SAVE_DIR+"World\\"+toLoad+".world")).isFile())
        {
            LOGGER.log(Level.INFO, "World "+toLoad+" found. Loading...");
            loadedWorld = (World) loadFile(f);
        }
        else 
        {
            loadedWorld = new World(toLoad, 10, 10);
            saveFile(loadedWorld, SAVE_DIR+"World\\"+toLoad+".world");
            LOGGER.log(Level.INFO, "Could not find desired file. Created new world "+toLoad);
        }
    }

    /** Load any file to an Object using object input streams
     * 
     * @param f The file to be deserialized
     * @return The loaded object
     */
    private static Object loadFile(File f)
    {
        try (FileInputStream fis = new FileInputStream(f); ObjectInputStream ois = new ObjectInputStream(fis)) {
                    Object o = ois.readObject();
                    ois.close();
                    fis.close();
            return o;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Save any object to a file using object output streams
     * 
     * @param o The object to be serialized
     * @param path The path where the file should be written to
     */
    private static void saveFile(Object o, String path)
    {
        try (FileOutputStream fos = new FileOutputStream(path); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(o);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
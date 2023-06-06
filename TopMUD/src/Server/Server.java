package Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Commands.Command;
import Environment.Mobs.Mob;
import Environment.Mobs.Friendly.Player;
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

    public static Logger getLogger(){return LOGGER;}

    public static World getWorld(){return loadedWorld;}
    
    private class EventHandler extends Thread
    {
        private List<Connection> connections;
        private HashMap<Mob, String> commandStack;
        private boolean isRunning;

        public EventHandler()
        {
            connections = new ArrayList<Connection>();
            commandStack = new HashMap<Mob, String>();
            isRunning = true;
        }

        public void run()
        {
            while(isRunning)
            {
                try {Thread.sleep(100);} catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Failed to sleep.");
                    e.printStackTrace();
                }

                if(!commandStack.isEmpty())
                {
                    Map.Entry<Mob, String> entry = commandStack.entrySet().iterator().next();
                    Mob executor = entry.getKey();
                    String current = entry.getValue();
                    
                    LOGGER.log(Level.INFO,"Handling command "+current);

                    String[] args = current.split(" ", 2);
                    if(args.length < 2) args = new String[]{args[0],""};

                    if(commandMap.get(args[0]) != null)
                    {
                        try{
                            commandMap.get(args[0]).execute(executor, args[1].split(""));
                        }catch(IOError e){
                            LOGGER.log(Level.WARNING, "Invalid command syntax::"+current);
                        }
                    }

                    else LOGGER.log(Level.WARNING, "Invalid command syntax::"+current);

                    commandStack.remove(executor);
                }
            }
        }

        public void stopRunning()
        {
            isRunning = false;
        }

        public void connect(Connection c)
        {
            connections.add(c);
        }

        public void addToStack(Mob other, String toCall)
        {
            commandStack.put(other, toCall);
            LOGGER.log(Level.INFO,"adding to stack "+toCall+". Stack is now length "+commandStack.size());
        }

        public int getIndex(String name)
        {
            for(int i = 0; i < connections.size(); i++) if(name.equals(connections.get(i).getPlayer().getName())) return i;
            return -1;
        }

        public int getIndex(Player player)
        {
            for(int i = 0; i < connections.size(); i++) if(player.equals(connections.get(i).getPlayer())) return i;
            return -1;
        }

        public Connection getConnection(String name)
        {
            int i = getIndex(name);
            if(i>=0) return connections.get(i);
            return null;
        }
        
        public Connection getConnection(Player player)
        {   
            int i = getIndex(player);
            if(i>=0) return connections.get(i);
            return null;
        }

        public void disconnect(int i) throws IOException
        {
            connections.get(i).saveAndQuit();
            connections.remove(i);
        }
    }

    private class Connection extends Thread
    {
        private ConnectionStates cs = ConnectionStates.AWAITING_NAME;

        private Socket s;
        private String ms;
        private String inms;
        private Player thisPlayer;

        private BufferedReader is;
        private PrintWriter os;

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
                while((input = is.readLine()) != null && cs != ConnectionStates.PLAYING)
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
                        /*
                        case AWAITING_NEW_CHARACTER: This state is unused. During further development, add a character creator by classes
                            checkNewCharacter(input);
                            break;
                         */ 
                        default:
                            LOGGER.log(Level.WARNING, "Current thread error. Closing...");
                            savePlayer();
                            break;
                    }
                }

                while(cs == ConnectionStates.PLAYING) playing();

            } catch (ClassNotFoundException | IOException e) {
                LOGGER.log(Level.FINER, "Connection Closing.");
                e.printStackTrace();
                savePlayer();
            }
        }

        private void savePlayer()
        {
            saveFile(thisPlayer, SAVE_DIR+"Users\\"+thisPlayer.getName().toLowerCase()+".player");
        }

        private void saveAndQuit() throws IOException
        {
            savePlayer();
            s.close();
            is.close();
            os.close();
        }

        private void checkUser(String input) throws FileNotFoundException, IOException, ClassNotFoundException
        {
            LOGGER.log(Level.INFO, input);
            File f;
            if("new".equals(input)) 
            {
                cs = ConnectionStates.AWAITING_NEW_NAME;
                LOGGER.log(Level.INFO, "Generating new character");
                os.println("Please enter a name for your new character (\"back\" to return):");
            }
                                    
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
            
            else
            {
                LOGGER.log(Level.WARNING, "Invalid user");
                os.println("Invalid username - please try again!\n");
            }
        }

        private void checkPass(String input) throws IOException, FileAlreadyExistsException
        {
            
            if("back".equals(input)) 
            {
                cs = ConnectionStates.AWAITING_NAME;
                savePlayer();
                thisPlayer = null;
                os.println("Please input your username (\"new\" to generate a new character): ");
            }
        
            else if(input.equals(thisPlayer.getPassword()))
            {
                LOGGER.log(Level.INFO, "Player "+thisPlayer.getName()+" logged::"+LocalDateTime.now());
                cs = ConnectionStates.PLAYING;
                os.println("Welcome! Press enter to continue.");
            }
        
            else
                os.println("Invalid password - please try again!\n");
        }

        private void checkNewName(String input)
        {
            if("back".equals(input)) 
            {
                cs = ConnectionStates.AWAITING_NAME;
                os.println("Please input your username (\"new\" to generate a new character): ");
            }   

            else if((new File("C:\\Users\\Toppe\\Documents\\GitHub\\TopMUD\\TopMUD\\rsc\\Users\\"+input.toLowerCase()+".player")).isFile())
            {
                os.println("Character already exists.");
                checkNewName("back");
            }

            else
            {
                thisPlayer = new Player(input);
                cs = ConnectionStates.AWAITING_NEW_PASSWORD;
                os.println("New character \""+input.toUpperCase()+"\" created. Please set a password: ");
            }
        }

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

        private void playing() throws IOException
        {
            //if(is.ready()) inms = is.readLine();
            inms = is.readLine();
            if(!"".equals(inms))
            {
                LOGGER.log(Level.INFO, "From " + thisPlayer.getName() + "::" + inms);
                cManager.addToStack(thisPlayer, inms);
                inms="";
            } 

            if(pendingSend()) 
            {
                os.println(ms);
                ms="";
            }
        }

        public Player getPlayer()
        {
            return thisPlayer;
        }

        public void outMessage(String msg)
        {
            ms = msg;
        }

        private boolean pendingSend()
        {
            return !("".equals(ms) || ms == null);
        }

        public Socket getSocket() 
        {
            return s;
        }
    }

    private void commandsInit()
    {
        commandMap.put("quit", (player, args)-> {
            try {
                cManager.disconnect(cManager.getIndex((Player) player));
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        });

        commandMap.put("inv", (player, args)-> cManager.getConnection((Player) player).outMessage(((Player) player).getInventory()));

        commandMap.put("op", (player, args) -> {
            if(args[0] == null)((Player) player).op(true);
            else if(cManager.getConnection(args[0]) != null) cManager.getConnection(args[0]).getPlayer().op(true);
            else cManager.getConnection((Player) player).outMessage("Could not find player");
        });

        commandMap.put("deop", (player, args) -> ((Player) player).op(false));

        commandMap.put("move", (player, args) -> {
                if("n".equals(args[0])) ((Player) player).move(0, -1, loadedWorld.getLimit());
           else if("e".equals(args[0])) ((Player) player).move(1, 0, loadedWorld.getLimit());
           else if("s".equals(args[0])) ((Player) player).move(0, 1, loadedWorld.getLimit());
           else if("w".equals(args[0])) ((Player) player).move(-1, 0, loadedWorld.getLimit());
            else cManager.getConnection((Player) player).outMessage("Invalid direction. Valid: [n, e, s, w]");
        });

        commandMap.put("look", (player, args) -> {
            loadedWorld.getRoom(((Player) player).getPosition()).getView();
        });

        commandMap.put("save", (player, args) -> {cManager.getConnection((Player) player).savePlayer();});

        commandMap.put("help", (player, args) -> {
            String ret = "Valid command list: \n";
            for (String s : commandMap.keySet()) ret += s + "\n";
            cManager.getConnection((Player) player).outMessage(ret);
        });
    }
    
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
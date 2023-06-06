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

        public List<Connection> getConnections()
        {
            return connections;
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

        public int getIndexByPlayer(Player player)
        {
            for(int i = 0; i < connections.size(); i++) if(player.equals(connections.get(i).getPlayer()))  return i;
            return -1;
        }
        
        public Connection getConnectionByPlayer(Player player)
        {
            return connections.get(getIndexByPlayer(player));
        }

        public void disconnect(int i) throws IOException
        {
            connections.get(i).saveAndQuit();
            connections.remove(i);
        }

        public Connection getConnection(int index)
        {
            return connections.get(index);
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

                        case AWAITING_NEW_CHARACTER:
                            checkNewCharacter(input);
                            break;

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
            try {
                saveFile(thisPlayer, SAVE_DIR+"Users\\"+thisPlayer.getName().toLowerCase()+".player");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void saveAndQuit() throws IOException
        {
            savePlayer();
            is.close();
            os.close();
            s.close();
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
                FileOutputStream fos = new FileOutputStream(thisPlayer.getName().toLowerCase()+".player");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(this.thisPlayer);
                    oos.flush();
                    oos.close();
                    fos.close();
            }
        
            else if(input.equals(thisPlayer.getPassword()))
                {LOGGER.log(Level.INFO, "Player "+thisPlayer.getName()+" logged::"+LocalDateTime.now());
                cs = ConnectionStates.PLAYING;}
        
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
            os.println("Pick a class for your character: [F]ighter, [T]hief, [R]anger");
            cs=ConnectionStates.AWAITING_NEW_CHARACTER;
        }

        private void checkNewCharacter(String input)
        {
            savePlayer();
            cs=ConnectionStates.PLAYING;
        }

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

        public String getInMessage()
        {
            return inms;
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
                cManager.disconnect(cManager.getIndexByPlayer((Player) player));
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        });

        commandMap.put("inv", (player, args)->
        {
            //cManager.getConnection(cManager.getIndexByPlayer((Player) player)).outMessage(((Player) player).getInventory());
        });

        commandMap.put("oppme", (player, args) -> ((Player) player).op(true));

        commandMap.put("deoppme", (player, args) -> ((Player) player).op(false));

        commandMap.put("move", (player, args) -> {});
    }

    private void loadWorld(String toLoad) 
    {
        File f;
        if((f = new File(SAVE_DIR+"World\\"+toLoad+".world")).isFile())
        {
            LOGGER.log(Level.INFO, "World "+toLoad+" found. Loading...");
            try {
                loadedWorld = (World) loadFile(f);
            } catch (ClassNotFoundException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else 
        {
            loadedWorld = new World(toLoad, 10, 10);
            loadedWorld.save(SAVE_DIR);
            LOGGER.log(Level.INFO, "Could not find desired file. Created new world "+toLoad);
        }
    }

    private static Object loadFile(File f) throws IOException, ClassNotFoundException
    {
        FileInputStream fis = new FileInputStream(f);
        ObjectInputStream ois = new ObjectInputStream(fis);
                Object o = ois.readObject();
                ois.close();
                fis.close();
        return o;
    }

    private static void saveFile(Object o, String path) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(path);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(o);
        oos.flush();
        oos.close();
    }
}
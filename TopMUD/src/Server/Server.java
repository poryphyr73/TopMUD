package Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Commands.Command;
import Environment.Mobs.Mob;
import Environment.Mobs.Friendly.Player;

import java.io.*;
import java.net.*;
import java.nio.file.FileAlreadyExistsException;

public class Server {
    private List<Client> playerList;
    private static EventHandler cManager;
    private Map<String, Command> commandMap;

    public Server()
    {
        cManager = new EventHandler();
        cManager.start();
        commandMap = new HashMap<String, Command>();

        /*   //GENERAL FORMAT FOR COMMAND INIT && CALL
        commandMap.put("n", (c)->
        {
            if(c.getClass().equals(Player.class)) c.getName();
            else c.getDesc();
        });
        commandMap.get("n").execute(a);
        commandMap.get("n").execute(b);
        */
    }

    public void start() throws IOException
    {
        try (ServerSocket serverSocket = new ServerSocket(7778);
        ) {
            while (true) {
                Connection c = new Connection(serverSocket.accept());
                cManager.connect(c);
                c.start(); // starts respond to the client on another thread
            }

        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    private class EventHandler extends Thread
    {
        private List<Connection> connections;
        private HashMap<Mob, String> commandStack;

        public EventHandler()
        {
            connections = new ArrayList<Connection>();
            commandStack = new HashMap<Mob, String>();
        }

        public void run()
        {
            if(commandStack.keySet().size() > 0)
            {
                Mob executor = (Mob) commandStack.keySet().toArray()[0];
                String current = commandStack.get(executor);

                String[] args = current.split(" ", 3);
                try{
                    commandMap.get(args[0]).execute(executor, executor.getRoom().getEntitiesByIndex(Integer.parseInt(args[1])), args[2]);
                }catch(IOError e){
                    commandMap.get("ERROR").execute(executor, executor, current);
                }
            }
        }

        public void connect(Connection c)
        {
            connections.add(c);
        }

        public void addToStack(Mob other, String toCall)
        {
            commandStack.put(other, toCall);
        }

        public void disconnect(int i)
        {
            connections.remove(i);
        }

        public void disconnect(Connection c)
        {
            connections.remove(connections.indexOf(c));
        }
    }

    private class Connection extends Thread
    {
        private ConnectionStates cs = ConnectionStates.AWAITING_NAME;
        private Socket s;
        private String ms;
        private String inms;
        private Player thisPlayer;
        private Connection(Socket s) {this.s = s;}
        private boolean isLoggingIn = true;

        @Override
        public void run()
        {
            //SPECIFY DIRECTORY FOR FILEPATH???
            while(true)
            {
                try(
                    DataInputStream is = new DataInputStream(s.getInputStream());
                    DataOutputStream os = new DataOutputStream(s.getOutputStream());
                ){
                    while(isLoggingIn)
                    {
                        switch(cs)
                        {
                            case AWAITING_NAME:
                                os.writeUTF("Please input your username (\"new\" to generate a new character): ");
                                String attempt = is.readUTF();
                                File f;
                                if("new".equals(attempt)) 
                                {
                                    cs = ConnectionStates.AWAITING_NEW_NAME;
                                    
                                }
                                
                                
                                else if((f = new File("C:\\Users\\Toppe\\Documents\\GitHub\\TopMUD\\TopMUD\\rsc\\Users\\"+attempt.toLowerCase()+".player")).isFile()) 
                                {
                                    cs = ConnectionStates.AWAITING_PASSWORD;
                                    try(FileInputStream fis = new FileInputStream(f);
                                    ObjectInputStream ois = new ObjectInputStream(fis);)
                                    {thisPlayer = (Player) ois.readObject();}catch(ClassNotFoundException e){}
                                }
        
                                else
                                    os.writeUTF("Invalid username - please try again!\n");

                                break;

                            case AWAITING_PASSWORD:
                                os.writeUTF("Please input your password (back to return): ");
                                String pass = is.readUTF();
                                if("back".equals(pass)) 
                                {
                                    cs = ConnectionStates.AWAITING_NAME;
                                    try(FileOutputStream fos = new FileOutputStream(thisPlayer.getName().toLowerCase()+".player");
                                    ObjectOutputStream oos = new ObjectOutputStream(fos);){oos.writeObject(this.thisPlayer);
                                        oos.flush();
                                        oos.close();}catch(FileAlreadyExistsException fae){}
                                }
        
                                else if(pass.equals(thisPlayer.getPassword()))
                                    cs = ConnectionStates.PLAYING;
        
                                else
                                    //os.writeUTF("Invalid password - please try again!\n");
                                    isLoggingIn = false;
                                break;
                            
                            case AWAITING_NEW_NAME:
                                os.writeUTF("hi");
                                cs = ConnectionStates.PLAYING;
                                break;
                            
                            case AWAITING_NEW_PASSWORD:
                                break;
                            
                            case PLAYING:
                                isLoggingIn = false;
                                break;
                            
                            //Finish this login state machine. it sucks but just do better
                        }
                    }

                    while(cs == ConnectionStates.PLAYING)
                    {
                        if(pendingSend()) 
                        {
                            os.writeUTF(ms);
                            ms="";
                        }

                        if("".equals(is.readUTF()))
                        {
                            System.out.println("From " + thisPlayer.getName() + ":: " + (inms = is.readUTF()));
                            cManager.addToStack(thisPlayer, inms);
                        }  
                    }
                    s.close();
                    is.close();
                    os.close();
                }catch(IOException e){
                    //TODO
                }finally{
                    try {
                        FileOutputStream fos = new FileOutputStream(thisPlayer.getName().toLowerCase()+".player");
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(this.thisPlayer);
                        oos.flush();
                        oos.close();
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
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
    }
}
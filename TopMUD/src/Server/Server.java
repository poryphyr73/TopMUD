package Server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Commands.Command;
import Environment.GameObject;
import Environment.Mobs.Friendly.Player;

import java.io.*;
import java.net.*;

public class Server {
    private List<Client> playerList;
    private List<Connection> connections;
    private ConnectionHandler cManager;
    private Map<String, Command> commandMap;

    public Server()
    {
        cManager = new ConnectionHandler();
        commandMap = new HashMap<String, Command>();

        /*  GENERAL FORMAT FOR COMMAND INIT && CALL
        commandMap.put("n", (b)->b.getName());
        commandMap.put("m", (b)->b.getDesc());
        commandMap.put("s", (b)->b.getView());
        commandMap.get("n").execute(a);
        commandMap.get("m").execute(a);
        commandMap.get("s").execute(a);
        */
    }

    public void start() throws IOException
    {
        try (ServerSocket serverSocket = new ServerSocket(7777);
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
    
    private class ConnectionHandler
    {
        private List<Connection> connections;

        public void connect(Connection c)
        {
            connections.add(c);
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
        private Socket s;
        private String ms;
        private String inms;
        private Player thisPlayer;
        private Connection(Socket s) {this.s = s;}

        @Override
        public void run()
        {
            
            while(true)
            {
                try(
                    DataInputStream is = new DataInputStream(s.getInputStream());
                    DataOutputStream os = new DataOutputStream(s.getOutputStream());
                ){
                    while(!ms.equals("super_quit"))
                    {
                        if(pendingSend()) 
                        {
                            os.writeUTF(ms);
                            ms="";
                        }

                        if("".equals(is.readUTF()))
                            System.out.println(inms = is.readUTF());
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
            return !"".equals(ms);
        }

        public boolean pendingRecieve()
        {
            return !"".equals(inms);
        }

        public String getInMessage()
        {
            return inms;
        }
    }
}
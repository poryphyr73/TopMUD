package Server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Commands.Command;
import Environment.GameObject;

import java.io.*;
import java.net.*;

public class Server {
    private List<Client> playerList;
    private List<Connection> connections;
    private Map<String, Command> commandMap;
    private GameObject a = new GameObject("Dave", "This is dave", "I am dave");

    public Server()
    {
        commandMap = new HashMap<String, Command>();
        commandMap.put("n", (b)->b.getName());
        commandMap.put("m", (b)->b.getDesc());
        commandMap.put("s", (b)->b.getView());
        commandMap.get("n").execute(a);
        commandMap.get("m").execute(a);
        commandMap.get("s").execute(a);

    }

    public void start() throws IOException
    {
        try (ServerSocket serverSocket = new ServerSocket(7777);
        ) {
            while (true) {
                Connection c = new Connection(serverSocket.accept());
                connections.add(c);
                c.start(); // starts respond to the client on another thread
            }

        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static class Connection extends Thread
    {
        private Socket s;
        private String ms;
        private String inms;
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
                        if(pendingMsg()) 
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
                }
            }
        }

        public void outMessage(String msg)
        {
            ms = msg;
        }

        private boolean pendingMsg()
        {
            return !"".equals(ms);
        }
    }
}
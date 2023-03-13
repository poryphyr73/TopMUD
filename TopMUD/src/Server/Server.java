package Server;

import java.net.ServerSocket;
import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) throws Exception
    {
        try (ServerSocket serverSocket = new ServerSocket(7777);
        ) {
            while (true) {
                ConnectionManager manager = new ConnectionManager(serverSocket.accept());
                manager.start(); // starts respond to the client on another thread
            }

        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static class ConnectionManager extends Thread
    {
        private Socket s;
        private ConnectionManager(Socket s) {this.s = s;}

        @Override
        public void run()
        {
            while(true)
            {
                try(
                    DataInputStream is = new DataInputStream(s.getInputStream());
                    DataOutputStream os = new DataOutputStream(s.getOutputStream());
                ){
                    String ms;
                    while(!(ms = is.readUTF()).equals("super_quit"))
                    {
                        os.writeUTF(ms+", sent from "+s.getInetAddress());
                        System.out.println(ms);
                    }
                    s.close();
                    is.close();
                    os.close();
                }catch(IOException e){
                    //TODO
                }
            }
        }
    }
}
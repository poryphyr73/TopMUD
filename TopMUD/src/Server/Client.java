package Server;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Thread
{
    //info of the player's connection
    private int port;
    private int sock;
    private String ad;

    //runtime info
    private boolean isClosing;

    //I/O
    private static Scanner kb = new Scanner(System.in);

    Client(int _sock, int _port, String _ad)
    {
        sock = _sock; port = _port; ad = _ad;
    }
    
    public static void main(String[] args) {
        try(
            Socket s = new Socket("127.0.0.1", 7778);
            DataInputStream is = new DataInputStream(s.getInputStream());
            DataOutputStream os = new DataOutputStream(s.getOutputStream());
        ){
            Listener lis = new Listener(is);
            lis.start();

            String ms = "";
            while(true){
                ms = kb.nextLine();
                try {os.writeUTF(ms);} catch (IOException e) {
                    // TODO
                    e.printStackTrace();
                    kb.close();
                }
            }
        }catch(IOException e) {
            //TODO
        }
    }

    private static class Listener extends Thread
    {
        private DataInputStream is;
        public Listener(DataInputStream is){this.is = is;}
        @Override
        public void run()
        {
            try {
                String rec;
                rec = is.readUTF();
                if(!"".equals(rec))System.out.println(rec);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public int getSocket() {return sock;}
}
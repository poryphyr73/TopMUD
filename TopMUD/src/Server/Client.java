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
    private ConnectionStates cState;

    //runtime info
    private String prompt, playerName, pass;
    private int room, badPassCount;
    private boolean isClosing;

    //buffered I/O messages
    private String out;
    private String in;
    private static Scanner kb = new Scanner(System.in);

    Client(int _sock, int _port, String _ad)
    {
        sock = _sock; port = _port; ad = _ad;
        init();
    }

    private void init()
    {
        cState = ConnectionStates.AWAITNG_NAME;

        room = 1000;

        prompt = "Enter username. Enter 'new' to create a new character ... ";
    }
    
    public static void main(String[] args) {
        try(
            Socket s = new Socket("127.0.0.1", 7777);
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
                while(true) if(!"".equals(rec = is.readUTF()))System.out.println(rec);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public int getSocket() {return sock;}
    public boolean isPlaying() {return cState == ConnectionStates.PLAYING && !isClosing;}
    public boolean outputPending() {return !"".equals(out);}
}
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
            BufferedReader is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter os = new PrintWriter(s.getOutputStream(), true);
        ){
            Listener lis = new Listener(is);
            lis.start();

            String ms = "";
            while(true){
                ms = kb.nextLine();
                try {os.println(ms);} catch (Exception e) {
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
        private BufferedReader is;
        public Listener(BufferedReader is){this.is = is;}
        @Override
        public void run()
        {
            try {
                String rec;
                rec = is.readLine();
                while(!"".equals(rec)&&rec!=null)System.out.println(rec);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("hi");
        }
    }

    public int getSocket() {return sock;}
}
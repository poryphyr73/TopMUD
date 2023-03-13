package Server;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Thread
{
    final static int PORT = 7777;
    
    public static void main(String[] args) {
        try(
            Socket s = new Socket("127.0.0.1", 7777);
            DataInputStream is = new DataInputStream(s.getInputStream());
            DataOutputStream os = new DataOutputStream(s.getOutputStream());
        ){
            System.out.println("Connected!");
            Listener lis = new Listener(is);
            lis.start();
            System.out.println("Created!");

            String ms = "";
            Scanner kb = new Scanner(System.in);
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
}
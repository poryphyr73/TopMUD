package Server;

import java.io.IOException;

public class Launcher
{
    public static void main(String[] args) {
        Server s = new Server(args[0], args[1]);
        try {
            s.start();
            System.exit(0);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
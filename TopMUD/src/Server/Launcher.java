package Server;

import java.io.IOException;

public class Launcher
{
    public static void main(String[] args) {
        Server s = new Server();
        try {
            s.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
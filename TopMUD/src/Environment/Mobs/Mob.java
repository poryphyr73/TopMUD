package Environment.Mobs;

import java.io.Serializable;

import Environment.GameObject;
import Environment.World.Room;

public class Mob extends GameObject implements Serializable
{
    private Room room;

    public Mob(String a, String b, String c)
    {
        super(a,b,c);
    }

    public Room getRoom()
    {
        return room;
    }
}

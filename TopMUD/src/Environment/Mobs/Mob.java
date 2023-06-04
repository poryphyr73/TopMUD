package Environment.Mobs;

import Environment.GameObject;
import Environment.World.Room;

public class Mob extends GameObject
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

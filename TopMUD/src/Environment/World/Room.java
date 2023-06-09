package Environment.World;

import java.util.ArrayList;
import java.util.List;

import Environment.GameObject;

/** A room is simply a GameObject that houses entities. A player can visit a room.
 *  Enemies, objects, and other entities could be in the room.
 */
public class Room extends GameObject
{
    private List<GameObject> entities;

    public Room()
    {
        entities = new ArrayList<GameObject>();
    }

    public Room(String name, String desc, String view)
    {
        super(name, desc, view);
        entities = new ArrayList<GameObject>();
    }

    public GameObject getEntitiesByIndex(int i)
    {
        return entities.get(i);
    }

    public void addEntity(GameObject toAdd)
    {
        if(!toAdd.getClass().equals(Room.class))
        {
            entities.add(toAdd);
        }
    }

    public String toString()
    {
        return super.toString();
    }
}

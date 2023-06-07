package Environment.World;

import java.util.ArrayList;
import java.util.List;

import Environment.GameObject;

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

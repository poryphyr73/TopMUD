package Environment.World;

import java.io.Serializable;
import java.util.List;

import Environment.GameObject;

public class Room implements Serializable
{
    private List<GameObject> entities;

    public GameObject getEntitiesByIndex(int i)
    {
        return entities.get(i);
    }

    public void addEntity(GameObject toAdd)
    {

    }

    public String toString()
    {
        return super.toString();
    }
}

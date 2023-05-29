package Environment.World;

import java.util.List;

import Environment.GameObject;

public class Room 
{
    private List<GameObject> entities;

    public GameObject getEntitiesByIndex(int i)
    {
        return entities.get(i);
    }
}

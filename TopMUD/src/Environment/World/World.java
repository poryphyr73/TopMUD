package Environment.World;
import java.io.*;
import java.io.Serializable;

public class World implements Serializable
{
    private Room[][] level;
    private String name;

    public World(String _name, int width, int height)
    {
        level = new Room[height][width];
        name = _name;
    }

    public void writeRoom(Room toAdd, int row, int col)
    {
        level[row][col] = toAdd;
    }

    public void writeRoom(File getFrom, int row, int col)
    {
        //TODO
    }

    public Room getRoom(int[] coords)
    {
        return level[coords[0]][coords[1]];
    }

    public int[] getLimit()
    {
        return new int[]{level.length - 1, level[0].length - 1};
    }

    public String getName()
    {
        return name;
    }

    public String toString()
    {
        String str = "Map of "+name+"\n\n\t";
        for(int i = 1; i <= level[i].length; i++) str+=i+"\t";
        for(int i = 0; i <= level.length; i++)
        {
            str+="\n";
            str+=(i+1);
            for(int j = 0; j < level[i].length; j++) str += (level[i][j] == null ? "[ ]\t" : "\t");
        }
        return str;
    }
}

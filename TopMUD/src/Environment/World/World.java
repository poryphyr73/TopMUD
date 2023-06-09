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

    /** Write data to a room in the level
     * 
     * @param toAdd The room object to add to the level
     * @param row The y coordinate of the room
     * @param col The x coordinate of the room
     */
    public void writeRoom(Room toAdd, int row, int col)
    {
        level[row][col] = toAdd;
    }

    // This method should write a room with data from a text file like the CYOA game did. It is unimplemented because it may be redundant
    public void writeRoom(File getFrom, int row, int col)
    {
        //TODO
    }

    /** Get a room in the level
     * 
     * @param coords The coords of the room to retrieve, [0] = y, [1] = x
     * @return The room at the given coordinates
     */
    public Room getRoom(int[] coords)
    {
        return level[coords[0]][coords[1]];
    }

    /** Get the upper boundary of the level
     * 
     * @return The upper boundary coordinates of the level
     */
    public int[] getLimit()
    {
        return new int[]{level.length - 1, level[0].length - 1};
    }

    /** Determines if the coordinates passed in are valid in the level, or if they exceed the valid boundaries
     * 
     * @param coords The coordinates to check
     * @return True if the desired position is within the level's boundaries
     */
    public boolean isValidPosition(int[] coords)
    {
        return coords[0] <= getLimit()[0] && coords[1] <= getLimit()[1] && coords[0] >= 0 && coords[1] >= 0;
    }

    public String getName()
    {
        return name;
    }

    // The map is displayed as a grid of rooms. If the room isn't null, the room is displayed as [ ]
    public String toString()
    {
        String str = "Map of "+name+"\n\n\t";
        for(int i = 1; i <= level[i].length; i++) str+=i+"\t";
        for(int i = 0; i <= level.length; i++)
        {
            str+="\n";
            str+=(i+1);
            for(int j = 0; j < level[i].length; j++) str += (level[i][j] != null ? "[ ]\t" : "\t");
        }
        return str;
    }
}

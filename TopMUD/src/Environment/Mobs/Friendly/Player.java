package Environment.Mobs.Friendly;

import Environment.Mobs.Mob;

/** A player is a special mob controlled by a user.
 *  These mobs have to be user controlled.
 */
public class Player extends Mob
{
    private String password;
    private int xpos, ypos;
    private boolean isOp;

    private int hp, maxHp;

    public Player(String name, String desc, String view, String _password)
    {
        super(name,desc,view);
        password = _password;
    }

    public Player(String name)
    {
        super(name,"player::"+name,name);
    }

    public void setPassword(String _password)
    {
        password = _password;
    }

    public String getPassword()
    {
        return password;
    }

    public String getInventory()
    {
        //TODO
        return "";
    }

    public void op(boolean isOp) {this.isOp = isOp;}

    public boolean isOp() {return isOp;}

    /** Move a player by updating its coordinates.
     * 
     * @param x The amount to increase the x coordinate by
     * @param y The amount to increase the y coordinate by
     * @param limit The upper bounds of the room
     */
    public void move(int x, int y, int[] limit)
    {
        xpos += x;
        ypos += y;

        if(xpos < 0) xpos = 0;
        if(ypos < 0) ypos = 0;
        if(ypos > limit[0]) ypos = limit[0];
        if(xpos > limit[1]) xpos = limit[1];
    }

    /** The position of the player as an array
     * 
     * @return The player's position
     */
    public int[] getPosition()
    {
        return new int[]{ypos, xpos};
    }

    /** Increment or decrement health. This method demonstrates a capacity for a combat system to be implemented
     * 
     * @param k The value by which the player's health should change
     */
    public void updateHealth(int k)
    {
        hp += k;
        if(hp>maxHp) hp=maxHp;
        else if(hp <= 0); //TODO
    }

    @Override
    public boolean equals(Object other)
    {
        Player p = (Player) other;
        return (p.getPassword().equals(password) && p.getName().equals(this.getName()));
    }
}

package Environment.Mobs.Friendly;

import Environment.Mobs.Mob;

public class Player extends Mob
{
    private String password;
    private PlayerClasses pClass;
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

    public void setClass(PlayerClasses _pClass)
    {
        pClass=_pClass;
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

    public void move(int x, int y)
    {
        xpos += x;
        ypos += y;
    }

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

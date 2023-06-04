package Environment.Mobs.Friendly;

import java.io.Serializable;

import Environment.Mobs.Mob;

public class Player extends Mob
{

    private String password;
    private PlayerClasses pClass;
    private int[] skills = new int[3]; //heavy weapon, light weapon, ranged

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

        if(pClass == PlayerClasses.FIGHTER) skills = new int[]{3,1,1};
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

    @Override
    public boolean equals(Object other)
    {
        Player p = (Player) other;
        return (p.getPassword().equals(password) && p.getName().equals(this.getName()));
    }
}

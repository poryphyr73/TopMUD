package Environment.Mobs.Friendly;

import Environment.Mobs.Mob;

public class Player extends Mob{
    private String password;

    public Player(String a, String b, String c)
    {
        super(a,b,c);
    }

    public String getPassword()
    {
        return password;
    }
}

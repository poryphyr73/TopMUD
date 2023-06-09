package Environment;

import java.io.Serializable;

/** The GameObject class is very rudimentary, but it should serve as a framework for any entity viable for the game.
 *  It has a name, a description, and a "view" (physical appearcance tag). All entities extend the GameObject
 * 
 *  TODO maybe this could just be called "Entity"? Though GameObject is more frequently used in modern platforms
 */
public class GameObject implements Serializable
{
    private String name;
    private String desc;
    private String view;

    public GameObject(String name, String desc, String view)
    {
        this.name = name;
        this.desc = desc;
        this.view = view;
    }

    public GameObject(){name="";desc="";view="";}

    public GameObject(GameObject other)
    {
        name = other.getName();
        desc = other.getDesc();
        view = other.getView();
    }

    public GameObject(String name)
    {
        this.name = name;
        desc="";
        view="";
    }

    public String getName(){System.out.println(name);return name;}
    public String getDesc(){System.out.println(desc);return desc;}
    public String getView(){System.out.println(view);return view;}

    public String toString()
    {
        return name+"\n"+view;
    }
}

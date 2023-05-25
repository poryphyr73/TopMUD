package Environment;

public class GameObject 
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

    public GameObject(){}

    public GameObject(GameObject other)
    {
        name = other.getName();
        desc = other.getDesc();
        view = other.getView();
    }

    public String getName(){System.out.println(name);return name;}
    public String getDesc(){System.out.println(desc);return desc;}
    public String getView(){System.out.println(view);return view;}

    public String toString()
    {
        return "A "+name+"; "+view;
    }
}

package Commands;

import Environment.*;

public interface Command 
{
    public void execute(GameObject executor, GameObject target, String arg);
}

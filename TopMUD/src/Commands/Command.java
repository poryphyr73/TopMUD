package Commands;

import Environment.*;

public interface Command 
{
    public void execute(GameObject executor, String[] args);
}

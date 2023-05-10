package Commands;

import Environment.*;

public interface Command 
{
    public void execute(Thing other, String input);

    public String getHelp();
}

package Server;

public enum ConnectionStates
{
    //Default on connect
    AWAITNG_NAME, //Enter username until valid or "new"
    AWAITING_PASSWORD, //Enter pass until valid

    //Default on "new" from name
    AWAITNG_NEW_NAME, //Enter new name until valid
    AWAITING_NEW_PASSWORD, //Enter new pass until valid
    CONFIRM_PASSWORD, //Enter pass. If invalid, return to new pass

    PLAYING //Default state
}

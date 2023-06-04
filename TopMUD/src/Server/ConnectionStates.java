package Server;

public enum ConnectionStates
{
    //Default on connect
    AWAITING_NAME, //Enter username until valid or "new"
    AWAITING_PASSWORD, //Enter pass until valid

    //Default on "new" from name
    AWAITING_NEW_NAME, //Enter new name until valid
    AWAITING_NEW_PASSWORD, //Enter new pass until valid
    AWAITING_NEW_CHARACTER,

    PLAYING //Default state
}

package com.commander.drone.ali.dronecommander.data;

/**
 * Created by ali on 12/17/2016.
 */

public class Command {
    //Different types of commands
    public enum TYPE{
        EXPLORE,
        READ
    }
    public enum State{
        NEW,
        RETRY,
        FINISHED
    }

    private final Room mRoom;
    private final TYPE mType;
    private Integer mCommandID;
    private static int mIDGenerator = 0;// This ID generator will simply increment every time a new command is made
    Command.State mCurrentState;
    //Commands are given to Explore or Read different Rooms
    public Command(TYPE type, Room room){
        mType = type;
        mRoom = room;
        mCommandID = mIDGenerator++;
        mCurrentState = State.NEW;
    }

    public Integer getCommandID() {
        return mCommandID;
    }

    public Room getRoom() {
        return mRoom;
    }

    public TYPE getCommandType(){
        return mType;
    }

    public State getCurrentState(){
        return State.NEW;
    }
    public void failed(){
        mCurrentState =  State.RETRY;
    }

    public void success(){
        mCurrentState = State.FINISHED;
    }


}


package com.commander.drone.ali.dronecommander.data;

import java.util.Set;

/**
 * Created by ali on 12/11/2016.
 */

public class Room {
    //Room States are either new (unexplored), explored (Meaning the connections were found and the writing was read in the room)
    public enum State{
        NEW,
        FULLY_EXPLORED
    }
    private Integer mRoomID;//assuming all room IDs are unique , thats not too much to ask for, is it?
    //Used to show the state of the rooms in the UI
    private State mCurrentState;
    private boolean mHasExploredConnections = false;
    private boolean mHasCheckedWriting = false;
    private static Set<Integer> mUsedIDsSet;
    private Room(int roomID){
        mRoomID = roomID;
        mCurrentState = State.NEW;
    }
    //MakeRoom class prevents rooms being make with the same ID
    public static Room makeRoom(Integer roomID){
        if(mUsedIDsSet.contains(roomID)){
            return null;
        }
        return new Room(roomID);
    }
    public Integer getRoomID() {
        return mRoomID;
    }

    public State getState(){
        return mCurrentState;
    }

    public void checkedConnections() {
        mHasExploredConnections = true;
        if(mHasCheckedWriting && mHasExploredConnections){
            mCurrentState = State.FULLY_EXPLORED;
        }
    }
    public void readWriting() {
        mHasCheckedWriting = true;
        if(mHasCheckedWriting && mHasExploredConnections){
            mCurrentState = State.FULLY_EXPLORED;
        }
    }

}

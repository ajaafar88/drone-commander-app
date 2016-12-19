package com.commander.drone.ali.dronecommander.data;

import android.util.Log;

import java.util.HashSet;
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
    private String mRoomID;//assuming all room IDs are unique , thats not too much to ask for, is it?
    //Used to show the state of the rooms in the UI
    private State mCurrentState;
    private boolean mHasExploredConnections = false;
    private boolean mHasCheckedWriting = false;
    private static Set<String> mUsedIDsSet = new HashSet<String>();
    private Room(String roomID){
        mRoomID = roomID;
        mCurrentState = State.NEW;
    }
    //MakeRoom class prevents rooms being make with the same ID
    //This also marks the Room IDs as being visited
    public static Room makeRoom(String roomID){
        if(mUsedIDsSet.contains(roomID)){
            return null;
        }
        mUsedIDsSet.add(roomID);
        return new Room(roomID);
    }
    public String getRoomID() {
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

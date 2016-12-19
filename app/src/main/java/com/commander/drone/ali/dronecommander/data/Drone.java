package com.commander.drone.ali.dronecommander.data;

import android.util.SparseArray;

/**
 * Created by ali on 12/11/2016.
 */

public class Drone {
    private String mDroneID;

    private SparseArray<Command> pendingCommands;

    public SparseArray<Command> getCommands() {
        return pendingCommands;
    }

    public void addCommands(SparseArray<Command> commands) {
        this.pendingCommands = commands;
    }

    public Drone(String droneID){
        pendingCommands = new SparseArray<Command>();
        mDroneID = droneID;
    }

    public String getDroneID() {
        return mDroneID;
    }


}

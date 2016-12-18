package com.commander.drone.ali.dronecommander.data;

import android.util.SparseArray;

/**
 * Created by ali on 12/11/2016.
 */

public class Drone {
    private Integer mDroneID;

    private SparseArray<Command> pendingCommands;

    public SparseArray<Command> getCommands() {
        return pendingCommands;
    }

    public void addCommands(SparseArray<Command> commands) {
        this.pendingCommands = commands;
    }

    public Drone(int droneID){
        pendingCommands = new SparseArray<Command>();
        mDroneID = droneID;
    }

    public Integer getDroneID() {
        return mDroneID;
    }


}

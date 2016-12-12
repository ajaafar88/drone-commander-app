package com.commander.drone.ali.dronecommander.data;

/**
 * Created by ali on 12/11/2016.
 * exploring rooms will happen using an iterative approach of DFS treating rooms like Nodes
 * http://www.java2blog.com/2015/12/depth-first-search-in-java.html
 */

public class Room {
    //Room States are either new (unexplored), explored (Meaning has connected rooms to this room that are new)or a deadend(Meaning all connected rooms to this room that have no more new rooms to explore)
    enum State{
        NEW,
        EXPLORED,
        DEAD_END
    }
    private int RoomID;//assuming all room IDs are unique , thats not too much to ask for, is it?
    private State mCurrentState;
    public Room(){

    }
}

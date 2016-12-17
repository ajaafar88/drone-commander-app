package com.commander.drone.ali.dronecommander;

import android.os.AsyncTask;
import android.util.SparseArray;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.commander.drone.ali.dronecommander.data.Command;
import com.commander.drone.ali.dronecommander.data.Constants;
import com.commander.drone.ali.dronecommander.data.Drone;
import com.commander.drone.ali.dronecommander.data.JSONParser;
import com.commander.drone.ali.dronecommander.data.Room;
import com.commander.drone.ali.dronecommander.network.DroneCommandRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by ali on 12/11/2016.
 * Singleton to manage the drones movement through the labyrinth
 * Manages Volley request Queue
 * http://www.javaworld.com/article/2073352/core-java/simply-singleton.html
 * exploring rooms will happen using an iterative approach of DFS treating rooms like Nodes
 * http://www.java2blog.com/2015/12/depth-first-search-in-java.html
 */

public class DroneCommandManager {
    private static DroneCommandManager instance = null;
    private RequestQueue mRequestQueue;
    private Response.ErrorListener mErrorListener;
    private DroneCommanderListener mDroneCommanderListener;
    private Queue<Drone> mDroneQueue;//queue of drones that will take commands in FIFO order
    private Stack<Room> mRoomStack; //stack used for searching through the rooms using DFS
    private Queue<Command> mCommandQueue; //queue used for passing out the commands to the drones
    private Queue<Command> mRetryQueue; // queue that is populated with failed commands and drones are resent with these commands to
    private SparseArray<String> mResponseStringArray; //a Position -> String to store the resulting String based on using the position as a key

    private DroneCommandManager() {
        mDroneQueue = new PriorityQueue<Drone>();
        mCommandQueue = new PriorityQueue<Command>();
        mRetryQueue = new PriorityQueue<Command>();
        mRoomStack = new Stack<Room>();
        mResponseStringArray = new SparseArray<String>();
    }
    public static DroneCommandManager getInstance() {
        if(instance == null) {
            instance = new DroneCommandManager();
        }
        return instance;
    }

    public void startExploration(){

        Response.Listener<String> startMissionResponseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(mDroneCommanderListener != null){
                    mDroneCommanderListener.onMissionStarted();
                }
                JSONParser.parseStartResponse(response ,mDroneQueue ,mRoomStack );
            }
        };
        mRequestQueue.add(new DroneCommandRequest(Request.Method.GET,
                Constants.BASE_URL+Constants.START_MISSION,
                startMissionResponseListener,
                mErrorListener
                ));
    }

    public void setDroneCommanderListener(DroneCommanderListener droneCommanderListener) {
        mDroneCommanderListener = droneCommanderListener;
    }

    class SendCommandsTask extends AsyncTask<Void,String,String>{
        @Override
        protected String doInBackground(Void... voids) {
            return "result";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

    }
}

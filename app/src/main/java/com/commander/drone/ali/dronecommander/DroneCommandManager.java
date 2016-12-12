package com.commander.drone.ali.dronecommander;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.commander.drone.ali.dronecommander.data.Constants;
import com.commander.drone.ali.dronecommander.network.DroneCommandRequest;

/**
 * Created by ali on 12/11/2016.
 * Singleton to manage the drones movement through the labyrinth
 * Manages Volley request Queue
 * http://www.javaworld.com/article/2073352/core-java/simply-singleton.html
 */

public class DroneCommandManager {
    private static DroneCommandManager instance = null;
    private RequestQueue mRequestQueue;
    private Response.ErrorListener mErrorListener;
    private DroneCommanderListener mDroneCommanderListener;

    private DroneCommandManager() {

        // Exists only to defeat instantiation.
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
}

package com.commander.drone.ali.dronecommander;

import android.app.DownloadManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
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
 * volley requestqueue doc
 * https://developer.android.com/training/volley/requestqueue.html
 */

public class DroneCommandManager {
    private static DroneCommandManager instance = null;
    private RequestQueue mRequestQueue;
    private Response.ErrorListener mErrorListener;
    private DroneCommanderListener mDroneCommanderListener;
    private Queue<Drone> mDroneQueue;//queue of drones that will take commands in FIFO order
    private Stack<Room> mRoomStack; //stack used for searching through the rooms using DFS
    private boolean mDidStartedExploration = false;


    private DroneCommandManager(Context context) {

        mDroneQueue = new PriorityQueue<Drone>();
        mRoomStack = new Stack<Room>();
        Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();
    }
    public static DroneCommandManager getInstance(Context context) {
        if(instance == null) {
            instance = new DroneCommandManager(context);
            // Instantiate the cache

        }
        return instance;
    }

    public void startExploration(){
        if(!mDidStartedExploration) {

            Response.Listener<String> startMissionResponseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (mDroneCommanderListener != null) {
                        mDroneCommanderListener.onMissionStarted();
                    }

                    mDidStartedExploration = true;
                    JSONParser.parseStartResponse(response, mDroneQueue, mRoomStack);

                    //Exploring Process = Pop Room stack to fill CommandQueue with commands the drones will need to do
                    //Check the retry Queue if any commands need ot be retried because of a previous failure
                    //If the retry queue is empty take a command from the command queue
                    //if the command queue is empty pop the room stack to get more commands into the command queue
                    //if Retry queue , command queue and room stack is empty the exploration is done
                    new SendCommandsTask(mRequestQueue,mDroneQueue,mRoomStack).execute();
            }
            };
            mRequestQueue.add(new DroneCommandRequest(Request.Method.GET,
                    Constants.BASE_URL + Constants.START_MISSION,
                    startMissionResponseListener,
                    mErrorListener
            ));
        }
    }






    public void setDroneCommanderListener(DroneCommanderListener droneCommanderListener) {
        mDroneCommanderListener = droneCommanderListener;
    }

    class SendCommandsTask extends AsyncTask<Void,String,String>{
        private RequestQueue mRequestQueue;
        private Stack<Room> mRoomStack; //stack used for searching through the rooms using DFS
        private Response.ErrorListener mErrorListener;
        private Queue<Command> mCommandQueue; //queue used for passing out the commands to the drones
        private Queue<Command> mRetryQueue; // queue that is populated with failed commands and drones are resent with these commands to
        private SparseArray<String> mResponseStringArray; //a Position -> String to store the resulting String based on using the position as a key
        private int mMaxNumOfDrones;
        public SendCommandsTask(RequestQueue requestQueue, Queue<Drone> droneQueue, Stack<Room> roomStack) {
            mRequestQueue = requestQueue;
            mCommandQueue = new PriorityQueue<Command>();
            mRetryQueue = new PriorityQueue<Command>();
            mResponseStringArray = new SparseArray<String>();
            mRoomStack = roomStack;
            mDroneQueue = droneQueue;
            mMaxNumOfDrones = droneQueue.size();
        }

        @Override
        protected String doInBackground(Void... voids) {

            //Exploring Process
            //Check if all Drones are present
            //Fill command queue with commands from Room stack
            //Give them commands from the retry Queue if any commands need to be retried because of a previous failure
            //If the retry queue is empty take a command from the command queue
            //if the command queue is empty pop the room stack to get more commands into the command queue
            //if Retry queue , command queue, and room stack is empty and all the drones have returned the exploration is done
            //Once there are no commands left , wait for the drones to return
            while(mDroneQueue.size() == mMaxNumOfDrones && (mRetryQueue.isEmpty() && mRoomStack.isEmpty() && mCommandQueue.isEmpty())){//if Retry queue , command queue and room stack is empty the exploration is done
                try {
                    Thread.sleep(600);//Round trip time takes 500 ms lets wait for 600 on the safe side.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(mDroneQueue.size() == mMaxNumOfDrones){//Check if all Drones are present

                    while(!mRoomStack.isEmpty()){//Fill command queue with commands from Room stack
                        Room room = mRoomStack.pop();
                        mCommandQueue.add(new Command(Command.TYPE.READ, room));
                        mCommandQueue.add(new Command(Command.TYPE.EXPLORE, room));
                    }


                    for(int i = 0; i< mMaxNumOfDrones; i++) {
                        //If the command Queues are empty then the Drones need to wait for the
                        //sent commands to process . So they have new rooms to explore and read
                        if(!mRetryQueue.isEmpty() || !mCommandQueue.isEmpty()) {
                            Drone currentDrone = mDroneQueue.remove();
                            SparseArray<Command> commandSparseArray = new SparseArray<>();
                            int currentNumberOfCommands = 0;
                            boolean isDroneMaxedOut = false;
                            //Give them commands from the retry Queue if any commands need to be retried because of a previous failure
                            while (!mRetryQueue.isEmpty() && !isDroneMaxedOut) {// Drone is Maxed if it is maxed out on commands(5)
                                Command commandToAdd = mRetryQueue.remove();
                                commandSparseArray.append(commandToAdd.getCommandID(), commandToAdd);
                                currentNumberOfCommands++;
                                if (currentNumberOfCommands >= Constants.MAX_NUMBER_OF_COMMANDS_PER_DRONE) {
                                    isDroneMaxedOut = true;
                                }

                            }
                            //If the retry queue is empty take a command from the command queue
                            while (!mCommandQueue.isEmpty() && !isDroneMaxedOut) {
                                Command commandToAdd = mCommandQueue.remove();
                                commandSparseArray.append(commandToAdd.getCommandID(), commandToAdd);
                                currentNumberOfCommands++;
                                if (currentNumberOfCommands >= Constants.MAX_NUMBER_OF_COMMANDS_PER_DRONE) {
                                    isDroneMaxedOut = true;
                                }

                            }
                            //Drones is ready if there are no more commands and it has a command
                            if (commandSparseArray.size() > 0) {
                                currentDrone.addCommands(commandSparseArray);
                                final Drone droneBeingSent = currentDrone;
                                JSONObject jsonRequest = new JSONObject();
                                SparseArray<Command> sparseCommandArray = droneBeingSent.getCommands();
                                for (int j = 0; j<sparseCommandArray.size() ; j++ ) {
                                    try {
                                    Command command = sparseCommandArray.get(sparseCommandArray.keyAt(j));
                                    JSONObject commandDetails = new JSONObject();
                                        commandDetails.put(command.getCommandTypeString(), command.getRoom().getRoomID());
                                    jsonRequest.put(command.getCommandID().toString(),commandDetails);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                Response.Listener<String> droneReturnedListener = new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        JSONParser.parseCommandResponse(droneBeingSent,response, mDroneQueue, mRoomStack,mRetryQueue,mResponseStringArray);
                                    }
                                };

                                mRequestQueue.add(new DroneCommandRequest(Request.Method.POST ,Constants.BASE_URL+ "/drone/:"+droneBeingSent.getDroneID()+"/commands",jsonRequest,
                                        droneReturnedListener,mErrorListener));
                            }
                        }
                    }
                }
            }

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

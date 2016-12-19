package com.commander.drone.ali.dronecommander;

import android.content.Context;
import android.os.AsyncTask;
import android.speech.tts.UtteranceProgressListener;
import android.util.SparseArray;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

import java.util.LinkedList;
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
    private LinkedList<Drone> mDroneQueue;//queue of drones that will take commands in FIFO order
    private Stack<Room> mRoomStack; //stack used for searching through the rooms using DFS
    private boolean mDidStartedExploration = false;


    private DroneCommandManager(Context context) {

        mDroneQueue = new LinkedList<Drone>();
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

                    new SendCommandsTask(mRequestQueue,mDroneQueue,mRoomStack,mDroneCommanderListener).execute();
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

    private class SendCommandsTask extends AsyncTask<Void,String,String>{
        private RequestQueue mRequestQueue;
        private String mCurrentWaveDetailsString = "";
        private DroneCommanderListener mDroneCommanderListener;
        private Stack<Room> mRoomStack; //stack used for searching through the rooms, similar to using DFS but i am filling up the command queue instead because of possibility of failures
        private Response.ErrorListener mErrorListener;
        private LinkedList<Command> mCommandQueue; //queue used for passing out the commands to the drones
        private LinkedList<Command> mRetryQueue; // queue that is populated with failed commands and drones are resent with these commands to
        private SparseArray<String> mResponseStringArray; //a Position -> String to store the resulting String based on using the position as a key
        private int mMaxNumOfDrones;
        public SendCommandsTask(RequestQueue requestQueue, LinkedList<Drone> droneQueue, Stack<Room> roomStack, DroneCommanderListener droneCommanderListener) {
            mRequestQueue = requestQueue;
            mCommandQueue = new LinkedList<Command>();
            mRetryQueue = new LinkedList<Command>();
            mResponseStringArray = new SparseArray<String>();
            mRoomStack = roomStack;
            mDroneQueue = droneQueue;
            mMaxNumOfDrones = droneQueue.size();
            mDroneCommanderListener = droneCommanderListener;
            mErrorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    error.printStackTrace();
                }
            };
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

            int waveCount = 1;
            while(mDroneQueue.size() != mMaxNumOfDrones || (!mRetryQueue.isEmpty() || !mRoomStack.isEmpty() || !mCommandQueue.isEmpty())){
                // if all the drones returned and if Retry queue , command queue and room stack is empty the exploration is done
                try {
                    Thread.sleep(100);//Round trip time takes 500 ms lets wait for 600 on the safe side.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                if(mDroneQueue.size() == mMaxNumOfDrones){//Check if all Drones are present

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(Constants.PREPEND_WAVE_DETAILS_STRING);
                    stringBuilder.append(waveCount++);
                    stringBuilder.append("<br>");
                    while(!mRoomStack.isEmpty()){//Fill command queue with commands from Room stack
                        Room room = mRoomStack.pop();
                        mCommandQueue.add(new Command(Command.TYPE.READ, room));
                        mCommandQueue.add(new Command(Command.TYPE.EXPLORE, room));
                    }

                    for(int i = 0; i< mMaxNumOfDrones; i++) {
                        //If the command Queues are empty then the Drones need to wait for the
                        //sent commands to process . So they have new rooms to explore and read
                        stringBuilder.append("D");
                        stringBuilder.append(i);
                        stringBuilder.append("<br>");
                        if(!mRetryQueue.isEmpty() || !mCommandQueue.isEmpty()) {
                            Drone currentDrone = mDroneQueue.removeFirst();
                            SparseArray<Command> commandSparseArray = new SparseArray<>();
                            int currentNumberOfCommands = 0;
                            boolean isDroneMaxedOut = false;
                            //Give them commands from the retry Queue if any commands need to be retried because of a previous failure
                            while (!mRetryQueue.isEmpty() && !isDroneMaxedOut) {// Drone is Maxed if it is maxed out on commands(5)
                                Command commandToAdd = mRetryQueue.removeFirst();
                                commandSparseArray.append(commandToAdd.getCommandID(), commandToAdd);
                                Util.appendCommandString(commandToAdd, stringBuilder);
                                stringBuilder.append(" ");
                                currentNumberOfCommands++;
                                if (currentNumberOfCommands >= Constants.MAX_NUMBER_OF_COMMANDS_PER_DRONE) {
                                    isDroneMaxedOut = true;
                                }

                            }
                            //If the retry queue is empty take a command from the command queue
                            while (!mCommandQueue.isEmpty() && !isDroneMaxedOut) {
                                Command commandToAdd = mCommandQueue.removeFirst();
                                commandSparseArray.append(commandToAdd.getCommandID(), commandToAdd);
                                Util.appendCommandString(commandToAdd, stringBuilder);
                                stringBuilder.append(" ");

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

                                mRequestQueue.add(new DroneCommandRequest(Request.Method.POST ,Constants.BASE_URL+ "drone/"+droneBeingSent.getDroneID()+"/commands",jsonRequest,
                                        droneReturnedListener,mErrorListener));
                            }
                        } else {
                            stringBuilder.append(" Nothing!");
                        }
                        stringBuilder.append("<br>");
                    }

                    //Update progress UI with commands sent using the DroneCommanderListener
                    publishProgress(Constants.PREPEND_REPORT_STRING + Util.createReportString(mResponseStringArray),
                            Constants.PREPEND_COMMAND_QUEUE_STRING + Util.createCommandQueueString(mCommandQueue),
                            Constants.PREPEND_RETRY_QUEUE_STRING + Util.createCommandQueueString(mRetryQueue),
                            stringBuilder.toString());
                }
            }

            return Util.createReportString(mResponseStringArray);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            mDroneCommanderListener.onMissionUpdate(values[DroneCommanderListener.CURRENT_REPORT_INDEX],
                    values[DroneCommanderListener.COMMAND_QUEUE_INDEX],
                    values[DroneCommanderListener.RETRY_QUEUE_INDEX],
                    values[DroneCommanderListener.DRONE_WAVE_INDEX]);
        }

        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);

            Response.Listener<String> missionCompleteListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    mDroneCommanderListener.onMissionCompleted(result);
                }
            };
            JSONObject jsonReport = new JSONObject();
            try {
                jsonReport.put("message",result);
                mRequestQueue.add(new DroneCommandRequest(Request.Method.POST ,Constants.BASE_URL+ "report",jsonReport,
                        missionCompleteListener,mErrorListener));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}

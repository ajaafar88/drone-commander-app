package com.commander.drone.ali.dronecommander.data;

import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

/**
 * Created by ali on 12/17/2016.
 * This class will contain Static functions parse JSON received and populate data structures with the proper data
 */

public class JSONParser {

    private JSONParser(){};
    //returns false if it fails to parse json
    public static boolean parseStartResponse(String jsonString, LinkedList<Drone> droneQueue, Stack<Room> roomStack){
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("drones");
            //Populate drone queue with found drones
            for(int i=0; i < jsonArray.length() ; i++){
                droneQueue.add(new Drone(jsonArray.getString(i)));
            }
            //Populates Room Stack with found room
            roomStack.add(Room.makeRoom(jsonObject.getString("roomId")));
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean parseCommandResponse(Drone returningDrone, String jsonString, LinkedList<Drone> droneQueue, Stack<Room> roomStack,
                                               LinkedList<Command> retryCommandQueue, SparseArray<String> reportStringSparseArray){
        SparseArray<Command> returningDroneCommands = returningDrone.getCommands();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            Iterator<String> commandIDStringIterator = jsonObject.keys();
            //if Drone has an error Then all the commands they were sent with have failed
            if(jsonObject.opt("error") != null){
                for(int i = 0 ; i<returningDroneCommands.size() ; i++){
                    int key = returningDroneCommands.keyAt(i);
                    Command returningDroneCommand = returningDroneCommands.get(key);
                    returningDroneCommand.failed();
                    retryCommandQueue.add(returningDroneCommand);
                    returningDroneCommands.remove(key);

                }
            } else {

                while(commandIDStringIterator.hasNext()) {
                    String commandIDStringValue = commandIDStringIterator.next();
                    JSONObject resultJsonObject = jsonObject.getJSONObject(commandIDStringValue);
                    Integer currentCommandIDIntegerValue = Integer.valueOf(commandIDStringValue);

                    Command command = returningDroneCommands.get(currentCommandIDIntegerValue);
                    //Command ID returned with an error so the command failed and the command will
                    //we be added to the retry queue and removed from the drones list of pending commands
                    if (resultJsonObject.opt("error") != null) {
                        command.failed();
                        retryCommandQueue.add(command);
                    } else {
                        if (Command.TYPE.EXPLORE == command.getCommandType()) {
                            JSONArray connectionsJSONArray = resultJsonObject.getJSONArray("connections");
                            //if Command was to explore the room will populate the room stack with its connections
                            for (int i = 0; i < connectionsJSONArray.length(); i++) {
                                Room room = Room.makeRoom(connectionsJSONArray.getString(i));//Will contain Null if the roomid was already used
                                if(room != null){
                                    roomStack.add(room);
                                }
                            }
                            command.getRoom().checkedConnections();
                        } else if (Command.TYPE.READ == command.getCommandType()) {
                            //if Command was writing  , the report array will be populated by the string if it is valid
                            int position = Integer.parseInt(resultJsonObject.getString("order"));
                            if (position != -1) {
                                String writing = resultJsonObject.getString("writing");
                                if (writing != null) {
                                    reportStringSparseArray.append(position, writing);
                                }
                            }
                            command.getRoom().readWriting();
                        }
                        command.success();
                    }
                    //Command reference should be lost here
                    returningDroneCommands.remove(command.getCommandID());
                }
                droneQueue.add(returningDrone);

            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

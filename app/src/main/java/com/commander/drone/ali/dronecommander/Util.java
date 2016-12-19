package com.commander.drone.ali.dronecommander;

import android.util.SparseArray;

import com.commander.drone.ali.dronecommander.data.Command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;

/** example of UI text displayed
 Current Report: Here is a report
 CommandQueue : E45 R45
 Retry Queue : E35 R35

 Wave : 1
 D1 E1 R1 E2 R2 E3
 D2 R3 E4 R4 E5 R5...
 * Created by ali on 12/18/2016.
 */

public class Util {

    private Util(){
    }

    public static String createReportString(SparseArray<String> collectionOfWords){
        int [] keyArray = new int [collectionOfWords.size()];
        for(int i =0; i < keyArray.length; i++){
            keyArray[i] = collectionOfWords.keyAt(i);
        }
        Arrays.sort(keyArray);
        StringBuilder reportBuilder = new StringBuilder();
        for(int i =0; i < keyArray.length; i++) {
            reportBuilder.append(collectionOfWords.get(keyArray[i]));
        }
        return reportBuilder.toString();
    }

    //Same function is used for Retry Queue
    public static String createCommandQueueString( Queue<Command> commandQueue){
        Object[] commands = commandQueue.toArray();
        StringBuilder commandQueueBuilder = new StringBuilder();
        Command tempCommand;
        for(int i=0; i<commands.length; i++){
            tempCommand = (Command) commands[i];
            appendCommandString(tempCommand,commandQueueBuilder);
            commandQueueBuilder.append(" ");

        }
        return commandQueueBuilder.toString();
    }

    public static void appendCommandString(Command command, StringBuilder stringBuilder){
        if(command.getCommandType() == Command.TYPE.EXPLORE) {
            stringBuilder.append("E");
        }else{
            stringBuilder.append("R");
        }
        stringBuilder.append(command.getRoom().getRoomID());
    }

}

package com.commander.drone.ali.dronecommander;

/**
 * Created by ali on 12/11/2016.
 * This interface will allow our activity to be updated with the DroneCommanderManagers updates
 * For now onMissionStarted and onMissionCompleted
 */

public interface DroneCommanderListener {
    int CURRENT_REPORT_INDEX = 0;
    int COMMAND_QUEUE_INDEX = 1;
    int RETRY_QUEUE_INDEX = 2;
    int DRONE_WAVE_INDEX = 3;
    void onMissionStarted();
    void onMissionUpdate(String currentReport, String currentCommandQueue, String currentRetryQueue, String waveDetails);
    void onMissionCompleted(String result);
}

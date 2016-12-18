package com.commander.drone.ali.dronecommander;

/**
 * Created by ali on 12/11/2016.
 * This interface will allow our activity to be updated with the DroneCommanderManagers updates
 * For now onMissionStarted and onMissionCompleted
 */

public interface DroneCommanderListener {
    void onMissionStarted();
    void onMissionUpdate();
    void onMissionCompleted(String result);
}

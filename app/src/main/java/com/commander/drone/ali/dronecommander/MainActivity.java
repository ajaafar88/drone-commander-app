package com.commander.drone.ali.dronecommander;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.commander.drone.ali.dronecommander.network.DroneCommandRequest;

public class MainActivity extends AppCompatActivity implements DroneCommanderListener {
    DroneCommandManager mDroneCommandManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mDroneCommandManager = DroneCommandManager.getInstance();

        mDroneCommandManager.setDroneCommanderListener(this);



    }

    @Override
    public void onMissionStarted() {

    }

    @Override
    public void onMissionCompleted() {

    }
}

package com.commander.drone.ali.dronecommander;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.commander.drone.ali.dronecommander.network.DroneCommandRequest;

public class MainActivity extends AppCompatActivity implements DroneCommanderListener {
    DroneCommandManager mDroneCommandManager;
    private TextView mCommandQueueTextView;
    private TextView mRetryQueueTextView;
    private TextView mReportTextView;
    private ListView mWaveDetailsListView;
    private DroneWaveAdapter mDroneWaveAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCommandQueueTextView = (TextView)findViewById(R.id.current_command_queue_textview);
        mRetryQueueTextView = (TextView)findViewById(R.id.current_retry_command_queue_textview);
        mReportTextView = (TextView)findViewById(R.id.current_report_textview);
        mWaveDetailsListView = (ListView)findViewById(R.id.drone_details_listview);
        mDroneWaveAdapter = new DroneWaveAdapter();
        mWaveDetailsListView.setAdapter(mDroneWaveAdapter);
        mDroneCommandManager = DroneCommandManager.getInstance(this);
        mDroneCommandManager.setDroneCommanderListener(this);
        mDroneCommandManager.startExploration();


    }

    @Override
    public void onMissionStarted() {
        mReportTextView.setText("Mission Started!");
    }

    @Override
    public void onMissionUpdate(String currentReport, String currentCommandQueue, String currentRetryQueue, String waveDetails) {
        mCommandQueueTextView.setText(currentCommandQueue);
        mRetryQueueTextView.setText(currentRetryQueue);
        mReportTextView.setText(currentReport);
        mDroneWaveAdapter.add(waveDetails);

    }

    @Override
    public void onMissionCompleted(String result) {
        mReportTextView.setText(result);
        mCommandQueueTextView.setText("Mission Completed!");
        mRetryQueueTextView.setText("");
    }

}

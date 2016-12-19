package com.commander.drone.ali.dronecommander;

import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ali on 12/18/2016.
 */

public class DroneWaveAdapter extends BaseAdapter {
    ArrayList<String> mWaveList;

    public DroneWaveAdapter(){
        mWaveList = new ArrayList<String>();
    }
    @Override
    public int getCount() {
        return mWaveList.size();
    }

    public void add(String newWave){
        mWaveList.add(newWave);
        this.notifyDataSetChanged();
    }

    @Override
    public Object getItem(int i) {
        return mWaveList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = View.inflate(viewGroup.getContext(), R.layout.drone_result_list_item, null);
            holder = new ViewHolder();
            holder.mWaveDetailsTextView = (TextView) view.findViewById(R.id.drone_details_text_view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }
        holder.mWaveDetailsTextView.setText(Html.fromHtml(mWaveList.get(i)));
        return view;
    }

    private class ViewHolder{
        public TextView mWaveDetailsTextView;
    }
}

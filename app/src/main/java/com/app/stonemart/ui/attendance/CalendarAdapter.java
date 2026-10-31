package com.app.stonemart.ui.attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.stonemart.R;
import com.app.stonemart.data.model.Attendance;

import java.util.List;

/**
 * CalendarAdapter - Grid adapter for rendering monthly calendar date items and status indicators.
 */
public class CalendarAdapter extends BaseAdapter {

    private final Context context;
    private final List<Attendance> attendanceList;
    private final LayoutInflater inflater;

    public CalendarAdapter(Context context, List<Attendance> attendanceList) {
        this.context = context;
        this.attendanceList = attendanceList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return attendanceList != null ? attendanceList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return attendanceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            holder = new ViewHolder();
            holder.text1 = convertView.findViewById(android.R.id.text1);
            holder.text2 = convertView.findViewById(android.R.id.text2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Attendance item = attendanceList.get(position);
        if (item != null) {
            holder.text1.setText(item.getDate() + " • " + (item.isPresent() ? "Present" : "Absent"));
            holder.text2.setText("In: " + item.getInTime() + " | Out: " + item.getOutTime());
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView text1;
        TextView text2;
    }
}

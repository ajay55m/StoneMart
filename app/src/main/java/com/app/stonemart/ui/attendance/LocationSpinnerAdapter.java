package com.app.stonemart.ui.attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.stonemart.R;
import com.app.stonemart.data.model.LocationMaster;

import java.util.List;
import java.util.Locale;

/**
 * LocationSpinnerAdapter - Custom adapter for displaying available work locations
 * in the Check-In/Out dropdown with name, ID, and coordinates.
 */
public class LocationSpinnerAdapter extends ArrayAdapter<LocationMaster> {

    private final LayoutInflater inflater;
    private int selectedPosition = 0;

    public LocationSpinnerAdapter(@NonNull Context context, @NonNull List<LocationMaster> locations) {
        super(context, 0, locations);
        this.inflater = LayoutInflater.from(context);
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_location_spinner_selected, parent, false);
        }

        LocationMaster loc = getItem(position);
        TextView tvSelected = convertView.findViewById(R.id.tvSpinnerSelectedText);
        if (loc != null && tvSelected != null) {
            tvSelected.setText(loc.locationName);
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_location_spinner_dropdown, parent, false);
        }

        LocationMaster loc = getItem(position);
        if (loc != null) {
            TextView tvName = convertView.findViewById(R.id.tvDropdownLocationName);
            TextView tvCoords = convertView.findViewById(R.id.tvDropdownLocationCoords);
            View ivCheck = convertView.findViewById(R.id.ivDropdownCheck);

            if (tvName != null) {
                tvName.setText(loc.locationName);
            }
            if (tvCoords != null) {
                tvCoords.setText(String.format(Locale.US, "ID: %d • Lat: %.4f, Lng: %.4f", loc.id, loc.latitude, loc.longitude));
            }
            if (ivCheck != null) {
                ivCheck.setVisibility(position == selectedPosition ? View.VISIBLE : View.GONE);
            }
        }

        return convertView;
    }
}


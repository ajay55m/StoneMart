package com.app.stonemart;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AttendanceHistoryAdapter extends RecyclerView.Adapter<AttendanceHistoryAdapter.ViewHolder> {
    private final List<AttendanceRecordModel> list;

    public AttendanceHistoryAdapter(List<AttendanceRecordModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecordModel item = list.get(position);
        holder.tvDate.setText(item.getDate());
        holder.tvStatus.setText(item.getStatus());
        holder.tvPunchIn.setText(item.getCheckInTime());
        holder.tvPunchOut.setText(item.getCheckOutTime());
        holder.tvNetDuration.setText(item.getNetDuration());
        holder.tvLocationName.setText(item.getLocationName());
        holder.tvVerifiedTag.setText(item.getVerificationTag());

        // Apply StoneMart HRMS Dynamic Color Coding for Status Badges
        String status = item.getStatus() != null ? item.getStatus().toUpperCase() : "";
        if (status.contains("PRESENT") || status.contains("ACTIVE") || status.contains("DUTY")) {
            holder.tvStatus.setTextColor(Color.parseColor("#15803D")); // Dark Green
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DCFCE7"))); // Light Green Tint
        } else if (status.contains("HOLIDAY")) {
            holder.tvStatus.setTextColor(Color.parseColor("#7E22CE")); // Purple
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F3E8FF"))); // Light Purple Tint
        } else if (status.contains("SUNDAY")) {
            holder.tvStatus.setTextColor(Color.parseColor("#BE123C")); // Rose Red
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFE4E6"))); // Light Rose Tint
        } else if (status.contains("MISSING")) {
            holder.tvStatus.setTextColor(Color.parseColor("#B45309")); // Amber
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF3C7"))); // Light Amber Tint
        } else if (status.contains("ABSENT")) {
            holder.tvStatus.setTextColor(Color.parseColor("#B91C1C")); // Crimson Red
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEE2E2"))); // Light Red Tint
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#1D4ED8")); // Azure Blue
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EFF6FF")));
        }

        if (item.isPayrollSynced()) {
            holder.tvSyncStatus.setText("PAYROLL SYNCED");
            holder.tvSyncStatus.setTextColor(Color.parseColor("#059669")); // Emerald
        } else {
            holder.tvSyncStatus.setText("Beacon: GT-884");
            holder.tvSyncStatus.setTextColor(Color.parseColor("#1E40AF")); // Blue
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvStatus, tvPunchIn, tvPunchOut, tvNetDuration, tvLocationName, tvVerifiedTag, tvSyncStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPunchIn = itemView.findViewById(R.id.tvPunchIn);
            tvPunchOut = itemView.findViewById(R.id.tvPunchOut);
            tvNetDuration = itemView.findViewById(R.id.tvNetDuration);
            tvLocationName = itemView.findViewById(R.id.tvLocationName);
            tvVerifiedTag = itemView.findViewById(R.id.tvVerifiedTag);
            tvSyncStatus = itemView.findViewById(R.id.tvSyncStatus);
        }
    }
}
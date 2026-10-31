package com.app.stonemart;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import java.util.List;

public class LeaveTypeAdapter extends ArrayAdapter<LeaveTypeItem> {

    private final LayoutInflater inflater;

    public LeaveTypeAdapter(@NonNull Context context, @NonNull List<LeaveTypeItem> objects) {
        super(context, 0, objects);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_spinner_leave_type_selected, parent, false);
        }

        LeaveTypeItem item = getItem(position);
        if (item != null) {
            TextView tvCode = convertView.findViewById(R.id.tvSelectedCode);
            TextView tvName = convertView.findViewById(R.id.tvSelectedName);
            TextView tvBadge = convertView.findViewById(R.id.tvSelectedBadge);

            tvCode.setText(item.getCode());
            tvName.setText(item.getName());

            applyCategoryCodeTheme(tvCode, item.getCode());

            if (item.isPaid()) {
                tvBadge.setText("PAID");
                tvBadge.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_badge_paid));
                tvBadge.setTextColor(0xFF16A34A);
            } else {
                tvBadge.setText("UNPAID");
                tvBadge.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_badge_unpaid));
                tvBadge.setTextColor(0xFFDC2626);
            }
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_spinner_leave_type, parent, false);
        }

        convertView.setBackgroundColor(Color.WHITE);

        LeaveTypeItem item = getItem(position);
        if (item != null) {
            TextView tvCode = convertView.findViewById(R.id.tvSpinnerCode);
            TextView tvName = convertView.findViewById(R.id.tvSpinnerName);
            TextView tvSub = convertView.findViewById(R.id.tvSpinnerSub);
            TextView tvBadge = convertView.findViewById(R.id.tvSpinnerBadge);

            tvCode.setText(item.getCode());
            tvName.setText(item.getName());
            tvSub.setText(item.getAllowanceDesc());

            applyCategoryCodeTheme(tvCode, item.getCode());

            if (item.isPaid()) {
                tvBadge.setText("PAID");
                tvBadge.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_badge_paid));
                tvBadge.setTextColor(0xFF16A34A);
            } else {
                tvBadge.setText("UNPAID");
                tvBadge.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_badge_unpaid));
                tvBadge.setTextColor(0xFFDC2626);
            }
        }

        return convertView;
    }

    public static void applyCategoryCodeTheme(TextView tvCode, String code) {
        if (code == null) return;
        Context ctx = tvCode.getContext();
        switch (code.toUpperCase()) {
            case "CL":
                tvCode.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_pill_code_blue));
                tvCode.setTextColor(0xFF1D4ED8);
                break;
            case "SL":
                tvCode.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_pill_code_rose));
                tvCode.setTextColor(0xFFE11D48);
                break;
            case "EL":
                tvCode.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_pill_code_emerald));
                tvCode.setTextColor(0xFF059669);
                break;
            case "LOP":
                tvCode.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_pill_code_amber));
                tvCode.setTextColor(0xFFD97706);
                break;
            case "PERSONAL":
                tvCode.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_pill_code_indigo));
                tvCode.setTextColor(0xFF4F46E5);
                break;
            case "LL":
                tvCode.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_pill_code_purple));
                tvCode.setTextColor(0xFF7E22CE);
                break;
            default:
                tvCode.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_pill_code_blue));
                tvCode.setTextColor(0xFF1D4ED8);
                break;
        }
    }
}

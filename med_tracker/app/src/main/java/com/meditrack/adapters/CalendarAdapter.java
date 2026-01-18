package com.meditrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.meditrack.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private List<Date> dates;
    private int selectedPosition = 0;
    private OnDateClickListener listener;

    public interface OnDateClickListener {
        void onDateClick(Date date);
    }

    public CalendarAdapter(List<Date> dates, OnDateClickListener listener) {
        this.dates = dates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_date, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        Date date = dates.get(position);
        holder.bind(date, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName;
        TextView tvDayNumber;
        View container;

        CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.layout_date_item);
            tvDayName = itemView.findViewById(R.id.tv_day_name);
            tvDayNumber = itemView.findViewById(R.id.tv_date_number);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    selectedPosition = position;
                    notifyDataSetChanged();
                    listener.onDateClick(dates.get(position));
                }
            });
        }

        void bind(Date date, boolean isSelected) {
            SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            SimpleDateFormat dayNumberFormat = new SimpleDateFormat("d", Locale.getDefault());

            tvDayName.setText(dayNameFormat.format(date));
            tvDayNumber.setText(dayNumberFormat.format(date));

            if (isSelected) {
                container.setBackgroundResource(R.drawable.bg_calendar_date_selected);
                int primary = ContextCompat.getColor(itemView.getContext(), R.color.primary);
                tvDayNumber.setTextColor(primary);
                tvDayName.setTextColor(primary);
            } else {
                container.setBackgroundResource(R.drawable.bg_calendar_date_unselected);
                int textPrimary = ContextCompat.getColor(itemView.getContext(), R.color.text_primary);
                tvDayNumber.setTextColor(textPrimary);
                tvDayName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
            }
        }
    }
}

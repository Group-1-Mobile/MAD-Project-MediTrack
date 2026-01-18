package com.meditrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.meditrack.R;
import com.meditrack.models.DoseStatus;
import com.meditrack.models.MedicineHistory;
import com.meditrack.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<MedicineHistory> historyList;

    public HistoryAdapter() {
        this.historyList = new ArrayList<>();
    }

    public void setHistory(List<MedicineHistory> historyList) {
        this.historyList = historyList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        MedicineHistory history = historyList.get(position);
        holder.bind(history);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private View viewStatusIndicator;
        private TextView tvDateTime;
        private TextView tvStatus;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            viewStatusIndicator = itemView.findViewById(R.id.viewStatusIndicator);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(MedicineHistory history) {
            tvDateTime.setText(DateTimeUtils.formatDateTime(history.getScheduledTime()));
            tvStatus.setText(history.getStatus().getDisplayName());

            int color;
            if (history.getStatus() == DoseStatus.TAKEN) {
                color = ContextCompat.getColor(itemView.getContext(), R.color.status_taken);
            } else if (history.getStatus() == DoseStatus.MISSED) {
                color = ContextCompat.getColor(itemView.getContext(), R.color.status_missed);
            } else {
                color = ContextCompat.getColor(itemView.getContext(), R.color.status_skipped);
            }

            viewStatusIndicator.setBackgroundColor(color);
            tvStatus.setTextColor(color);
        }
    }
}

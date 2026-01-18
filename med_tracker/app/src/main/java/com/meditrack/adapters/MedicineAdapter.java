package com.meditrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meditrack.R;
import com.meditrack.models.Medicine;
import com.meditrack.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {
    private List<Medicine> medicines;
    private OnMedicineClickListener listener;

    public interface OnMedicineClickListener {
        void onMedicineClick(Medicine medicine);
    }

    public MedicineAdapter(OnMedicineClickListener listener) {
        this.medicines = new ArrayList<>();
        this.listener = listener;
    }

    public void setMedicines(List<Medicine> medicines) {
        this.medicines = medicines;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine medicine = medicines.get(position);
        holder.bind(medicine);
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    class MedicineViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMedicineName;
        private TextView tvDosage;
        private TextView tvNextDose;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tv_medicine_name);
            tvDosage = itemView.findViewById(R.id.tv_medicine_dose);
            tvNextDose = itemView.findViewById(R.id.tv_medicine_time);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMedicineClick(medicines.get(position));
                }
            });
        }

        public void bind(Medicine medicine) {
            tvMedicineName.setText(medicine.getName());
            
            String details = medicine.getAmount() + " " + medicine.getType();
            if (medicine.getDosage() != null && !medicine.getDosage().isEmpty()) {
                details += " • " + medicine.getDosage();
            }
            tvDosage.setText(details);
            
            // Get next reminder time from the list
            java.util.List<String> times = medicine.getReminderTimesList();
            String nextTime24 = DateTimeUtils.getNextReminderTime(times);
            String time12 = DateTimeUtils.formatTime12(nextTime24);
            tvNextDose.setText(time12);
        }
    }
}

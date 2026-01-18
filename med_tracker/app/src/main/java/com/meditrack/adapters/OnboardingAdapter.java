package com.meditrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meditrack.R;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {
    
    private static class OnboardingItem {
        int imageRes;
        String title;
        String description;

        OnboardingItem(int imageRes, String title, String description) {
            this.imageRes = imageRes;
            this.title = title;
            this.description = description;
        }
    }

    private final OnboardingItem[] items;

    public OnboardingAdapter(android.content.Context context) {
        items = new OnboardingItem[]{
                new OnboardingItem(
                        R.drawable.ic_medicine,
                        context.getString(R.string.onboarding_title_1),
                        context.getString(R.string.onboarding_desc_1)
                ),
                new OnboardingItem(
                        R.drawable.ic_notification,
                        context.getString(R.string.onboarding_title_2),
                        context.getString(R.string.onboarding_desc_2)
                ),
                new OnboardingItem(
                        R.drawable.ic_check,
                        context.getString(R.string.onboarding_title_3),
                        context.getString(R.string.onboarding_desc_3)
                )
        };
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.onboarding_slide, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingItem item = items[position];
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivOnboarding;
        private TextView tvTitle;
        private TextView tvDescription;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivOnboarding = itemView.findViewById(R.id.ivOnboarding);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }

        public void bind(OnboardingItem item) {
            ivOnboarding.setImageResource(item.imageRes);
            tvTitle.setText(item.title);
            tvDescription.setText(item.description);
        }
    }
}

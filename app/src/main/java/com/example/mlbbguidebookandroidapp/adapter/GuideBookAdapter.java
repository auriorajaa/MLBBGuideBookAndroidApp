package com.example.mlbbguidebookandroidapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mlbbguidebookandroidapp.R;
import com.example.mlbbguidebookandroidapp.model.GuideBook;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class GuideBookAdapter extends RecyclerView.Adapter<GuideBookAdapter.GuideBookViewHolder> {

    private List<GuideBook> guideBookList;
    private List<GuideBook> filteredList;
    private OnGuideBookClickListener onGuideBookClickListener;

    public GuideBookAdapter(List<GuideBook> guideBookList) {
        this.guideBookList = guideBookList;
        this.filteredList = new ArrayList<>(guideBookList); // Make a copy for filtering
    }

    public interface OnGuideBookClickListener {
        void onGuideBookClick(GuideBook guideBook);
    }

    public void setOnGuideBookClickListener(OnGuideBookClickListener listener) {
        this.onGuideBookClickListener = listener;
    }

    @NonNull
    @Override
    public GuideBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_article, parent, false);
        return new GuideBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuideBookViewHolder holder, int position) {
        GuideBook guideBook = filteredList.get(position);
        holder.titleTextView.setText(guideBook.getArticleTitle());
        holder.categoryTextView.setText(guideBook.getArticleCategory());
        holder.contentTextView.setText(guideBook.getArticleContent());

        // Format createdAt
        String formattedDate = formatTimestamp(guideBook.getCreatedAt());
        holder.dateTextView.setText(formattedDate);

        // Set UID
        holder.uidTextView.setText("UID: " + guideBook.getUID());

        // Use Glide to load the image from URL
        Glide.with(holder.itemView.getContext())
                .load(guideBook.getArticleImage())
                .placeholder(R.drawable.ic_launcher_foreground) // Placeholder image if not loaded yet
                .into(holder.imageView);

        // Set click listener on the whole card
        holder.itemView.setOnClickListener(v -> {
            if (onGuideBookClickListener != null) {
                onGuideBookClickListener.onGuideBookClick(guideBook);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class GuideBookViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, categoryTextView, contentTextView, dateTextView, uidTextView;
        ImageView imageView;

        public GuideBookViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.articleTitle);
            categoryTextView = itemView.findViewById(R.id.articleCategory);
            contentTextView = itemView.findViewById(R.id.articleContent);
            dateTextView = itemView.findViewById(R.id.articleDate);
            uidTextView = itemView.findViewById(R.id.articleUID);
            imageView = itemView.findViewById(R.id.articleImage);
        }
    }

    public void updateArticles(List<GuideBook> newGuideBooks) {
        filteredList.clear();
        filteredList.addAll(newGuideBooks);
        notifyDataSetChanged();
    }

    public String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = isoFormat.parse(timestamp);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            return dateFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            return timestamp; // Return original timestamp if parsing fails
        }
    }
}

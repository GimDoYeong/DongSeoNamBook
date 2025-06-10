package com.example.dongseonambook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder> {

    List<String> sentenceList;
    OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String sentence);

    }


    public RVAdapter(List<String> sentenceList, OnItemClickListener listener){
        this.sentenceList=sentenceList;
        this.listener=listener;
    }





    @NonNull
    @Override
    public RVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull RVAdapter.ViewHolder holder, int position) {
        holder.bind(sentenceList.get(position), listener);

    }

    @Override
    public int getItemCount() {
        return sentenceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public CardView cardView;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.itemId);
            cardView = (CardView) view;
        }

        public void bind(final String sentence, final OnItemClickListener listener) {
            textView.setText(sentence);
            cardView.setOnClickListener(v -> listener.onItemClick(sentence));
        }
    }
}

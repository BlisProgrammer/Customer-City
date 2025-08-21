package com.blis.customercity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blis.customercity.data.OnlineRecord;

import java.util.ArrayList;

public class TwoLineAdapter extends RecyclerView.Adapter<TwoLineAdapter.RecordViewHolder> {
    private final ArrayList<OnlineRecord> itemList;
    private final Context context;
    public TwoLineAdapter(Context context, ArrayList<OnlineRecord> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        OnlineRecord item = itemList.get(position);
        holder.titleTextView.setText(item.getLine1Text());
        holder.descriptionTextView.setText(item.getLine2Text());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    private OnItemClickListener mListener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.record_item, parent, false);
        return new RecordViewHolder(view, mListener);
    }

    public static class RecordViewHolder extends RecyclerView.ViewHolder {
        public final TextView titleTextView;
        public final TextView descriptionTextView;

        public RecordViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.itemTitle);
            descriptionTextView = itemView.findViewById(R.id.itemDescription);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }
}
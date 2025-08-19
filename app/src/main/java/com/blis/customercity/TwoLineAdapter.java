package com.blis.customercity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blis.customercity.Data.OnlineRecord;

import java.util.ArrayList;

//public class TwoLineAdapter extends ArrayAdapter<OnlineRecord> {
//    public TwoLineAdapter(Context context, ArrayList<OnlineRecord> items) {
//        super(context, android.R.layout.simple_list_item_2, android.R.id.text1, items);
//    }
//
//
//    @NonNull
//    @Override
//    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
//        View view = super.getView(position, convertView, parent);
//
//        TextView text1 = view.findViewById(android.R.id.text1);
//        TextView text2 = view.findViewById(android.R.id.text2);
//
//        OnlineRecord currentItem = getItem(position);
//        if (currentItem != null) {
//            text1.setText(currentItem.getLine1Text());
//            text1.setTypeface(text1.getTypeface(), Typeface.BOLD);
//            text1.setTextSize(20);
//            text2.setText(currentItem.getLine2Text());
//        }
//        return view;
//    }
//
//}

public class TwoLineAdapter extends RecyclerView.Adapter<TwoLineAdapter.RecordViewHolder> {
    private ArrayList<OnlineRecord> itemList;
    private Context context;
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
        public TextView titleTextView;
        public TextView descriptionTextView;

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
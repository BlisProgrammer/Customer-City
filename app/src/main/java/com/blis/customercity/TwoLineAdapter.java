package com.blis.customercity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.blis.customercity.data.Record;

import java.util.ArrayList;
import java.util.Objects;

public class TwoLineAdapter extends RecyclerView.Adapter<TwoLineAdapter.RecordViewHolder> {
    private final ArrayList<Record> itemList;
    private final Context context;

    public TwoLineAdapter(Context context, ArrayList<Record> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        Record item = itemList.get(position);
        holder.titleTextView.setText(item.getLine1Text());
        holder.descriptionTextView.setText(item.getLine2Text());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);
    }
    private OnItemClickListener itemClickListener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }
    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.record_item, parent, false);
        return new RecordViewHolder(view, itemClickListener);
    }

    public void updateList(ArrayList<Record> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return TwoLineAdapter.this.itemList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(TwoLineAdapter.this.itemList.get(oldItemPosition).getId(), newList.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return TwoLineAdapter.this.itemList.get(oldItemPosition).equals(newList.get(newItemPosition));
            }
        });

        // Clear and replace the old list with the new one
        this.itemList.clear();
        this.itemList.addAll(newList);

        // Dispatch the specific updates
        diffResult.dispatchUpdatesTo(this);
    }

    public static class RecordViewHolder extends RecyclerView.ViewHolder {
        public final TextView titleTextView;
        public final TextView descriptionTextView;
        public final ImageButton deleteButton;

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
            deleteButton = itemView.findViewById(R.id.delete_button);
            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });
        }
    }
}
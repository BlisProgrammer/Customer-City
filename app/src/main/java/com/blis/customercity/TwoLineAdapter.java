package com.blis.customercity;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class TwoLineAdapter extends ArrayAdapter<Record> {
    public TwoLineAdapter(Context context, ArrayList<Record> items) {
        super(context, android.R.layout.simple_list_item_2, android.R.id.text1, items);
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        TextView text1 = view.findViewById(android.R.id.text1);
        TextView text2 = view.findViewById(android.R.id.text2);

        Record currentItem = getItem(position);
        if (currentItem != null) {
            text1.setText(currentItem.getLine1Text());
            text1.setTypeface(text1.getTypeface(), Typeface.BOLD);
            text1.setTextSize(20);
            text2.setText(currentItem.getLine2Text());
        }
        return view;
    }

}

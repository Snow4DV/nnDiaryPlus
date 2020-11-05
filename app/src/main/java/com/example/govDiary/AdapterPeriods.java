package com.example.govDiary;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.LayoutInflater;

import androidx.cardview.widget.CardView;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// The adapter class which
// extends RecyclerView Adapter
public class AdapterPeriods
        extends RecyclerView.Adapter<AdapterPeriods.MyView> {
    private final OnClickListener mOnClickListener;
    // List with String type
    private List<Period> list;
    // View Holder class which
    // extends RecyclerView.ViewHolder
    public class MyView extends RecyclerView.ViewHolder implements  View.OnClickListener {

        // Text View
        TextView textView;
        CardView cardView;
        OnClickListener onClickListener;
        // parameterised constructor for View Holder class
        // which takes the view as a parameter
        public MyView(View view, OnClickListener onClickListener)
        {
            super(view);

            // initialise TextView with id
            textView = view
                    .findViewById(R.id.textview);
            cardView = view.findViewById(R.id.cardview);
            this.onClickListener = onClickListener;
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickListener.onClick(getAdapterPosition());
        }
    }



    // Constructor for adapter class
    // which takes a list of String type
    public AdapterPeriods(List<Period> horizontalList, OnClickListener onClickListener)
    {
        this.mOnClickListener = onClickListener;
        this.list = horizontalList;
    }

    // Override onCreateViewHolder which deals
    // with the inflation of the card layout
    // as an item for the RecyclerView.
    @Override
    public MyView onCreateViewHolder(ViewGroup parent,
                                     int viewType)
    {

        // Inflate item.xml using LayoutInflator
        View itemView
                = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_period,
                        parent,
                        false);

        // return itemView
        return new MyView(itemView, mOnClickListener);
    }

    // Override onBindViewHolder which deals
    // with the setting of different data
    // and methods related to clicks on
    // particular items of the RecyclerView.
    @Override
    public void onBindViewHolder(final MyView holder,
                                 final int position)
    {
        holder.textView.setText(list.get(position).name);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(holder.textView, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
    }

    // Override getItemCount which Returns
    // the length of the RecyclerView.
    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public interface  OnClickListener{
        void onClick(int position);
    }
}

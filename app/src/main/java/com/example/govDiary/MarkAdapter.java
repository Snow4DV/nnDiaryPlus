package com.example.govDiary;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import java.util.ArrayList;

public class MarkAdapter  extends BaseAdapter {
    Context context;
    private ArrayList<Mark> marks;
    LayoutInflater layoutInflater;
    public MarkAdapter(ArrayList<Mark> marks, Context context)  {
        this.marks = marks;
        this.context = context;
    }

    @Override
    public int getCount() {
        return marks.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Holder holder = new Holder();
        View rowView;
        rowView = layoutInflater.inflate(R.layout.mark_item, null);
        //find views
        holder.mark = rowView.findViewById(R.id.mark);
        holder.cardView = rowView.findViewById(R.id.cardView);
        //set 'em to values
        double weight = marks.get(position).weight;
        holder.mark.setText(marks.get(position).mark.toUpperCase());
        if(weight >= 1 && weight < 1.25){
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.BLUE));
        }

        else if(weight >= 1.25 && weight < 1.75){
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.REDYELLOW));
        }
        else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.GREEN));
        }

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(context, "You Clicked "+ position, Toast.LENGTH_LONG).show();

            }
        });

        return rowView;
    }

    public class Holder
    {
        TextView mark;
        CardView cardView;
    }
}

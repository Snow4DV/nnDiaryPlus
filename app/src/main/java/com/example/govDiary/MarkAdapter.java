package com.example.govDiary;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.github.florent37.viewtooltip.ViewTooltip;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;


public class MarkAdapter  extends BaseAdapter {
    Context context;
    View mTooltipView;
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
        int markColor;
        holder.mark.setText(marks.get(position).mark.toUpperCase());
        if(weight >= 1 && weight < 1.25){
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.BLUE));
            markColor = R.color.BLUE;
        }

        else if(weight >= 1.25 && weight < 1.75){
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.REDYELLOW));
            markColor = R.color.REDYELLOW;
        }
        else if(weight >= 1.75){
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.GREEN));
            markColor = R.color.GREEN;
        }
        else{
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.RED));
            markColor = R.color.RED;
        }

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Toast.makeText(context, "You Clicked "+ position, Toast.LENGTH_LONG).show();
                String lessonComment = marks.get(position).lessonComment;
                String comment = marks.get(position).comment;
                String markType = marks.get(position).markType;
                String date = "Ошибка";
                DateFormat df = new SimpleDateFormat("dd.MM");
                try {
                    date = df.format((new SimpleDateFormat("yyyy-MM-dd").parse(marks.get(position).date)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String popupText = "Оценка от " + date;
                if(weight != 0.0) popupText +=  ", " + "значимость: " + weight;
                if(!(comment.equals("") || comment.equals("null"))) popupText += ", " +  "комментарий: " + comment;
                if(!(lessonComment.equals("") || lessonComment.equals("null"))) popupText += ", " +  "комментарий (урок): " + lessonComment;
                if(!(markType.equals("") || markType.equals("null"))) popupText += ", " +  "тип оценки: " + markType;
                final ViewTooltip tooltipViewNew = ViewTooltip
                        .on(view)
                        .color(context.getResources().getColor(markColor))
                        .corner(30)
                        .autoHide(false, 3000)
                        .clickToHide(true)
                        .text(popupText)
                        .position(ViewTooltip.Position.TOP);
                tooltipViewNew.onHide(new ViewTooltip.ListenerHide() {
                    @Override
                    public void onHide(View view) {
                        mTooltipView = null;
                        ((JournalActivity)context).setMTooltipView(mTooltipView);
                    }
                });
                mTooltipView =  tooltipViewNew.show();
                ((JournalActivity)context).setMTooltipView(mTooltipView);
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

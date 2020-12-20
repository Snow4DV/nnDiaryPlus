package com.example.govDiary;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class AdapterFinalMarks extends RecyclerView.Adapter<AdapterFinalMarks.ViewHolder> { //Adapter for final marks list
    private LayoutInflater layoutInflater;
    ArrayList<MarkedLesson> markedLessons;
    private Context cont;
    AdapterFinalMarks(Context context, ArrayList<MarkedLesson> markedLessons){
        this.markedLessons = markedLessons;
        this.layoutInflater = LayoutInflater.from(context);
        cont = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_finalmarks, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MarkAdapter adapterMark = new MarkAdapter(markedLessons.get(position).marks, cont); //Creating custom MarkAdapter for marks lists in colored squares
        holder.gridView.setAdapter(adapterMark);

        //old code
        holder.textName.setText(markedLessons.get(position).name);
        if(!markedLessons.get(position).ifMarkIsFinal)
            if(markedLessons.get(position).averageMark != 0) {
                holder.textFinalMark.setText(String.valueOf(String.format("%.02f", markedLessons.get(position).averageMark)));
                holder.textFinalMark.setVisibility(View.VISIBLE);
            }
            else holder.textFinalMark.setVisibility(View.INVISIBLE);
        else{
            holder.textFinalMark.setText(Html.fromHtml("<b>" + markedLessons.get(position).finalMark + "</b>"));
            holder.textFinalMark.setTextSize(25);
        }
        holder.itemView.post(new Runnable() {
            @Override
            public void run() {
                     int width = holder.gridView.getWidth();
                Log.d(TAG, "ViewHolder: setting numColumns to: " +  holder.gridView.getWidth()/32 + ", width is " + holder.gridView.getWidth());
            }
        });
        switch((int) Math.round(markedLessons.get(position).averageMark)){
            case 3:
                holder.colorLine.setColorFilter(ContextCompat.getColor(cont,
                        R.color.REDYELLOW));
                break;
            case 4:
                holder.colorLine.setColorFilter(ContextCompat.getColor(cont,
                        R.color.YELLOWGREEN));
                break;
            case 5:
                holder.colorLine.setColorFilter(ContextCompat.getColor(cont,
                        R.color.GREEN));
                break;
            case 2:
                holder.colorLine.setColorFilter(ContextCompat.getColor(cont,
                        R.color.RED));
                break;
            default:
                holder.colorLine.setColorFilter(ContextCompat.getColor(cont,
                        R.color.WHITE));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return markedLessons.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView textName, textFinalMark;
        ImageView colorLine;
        FixedGridView gridView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            gridView = itemView.findViewById(R.id.gridView);
            textName = itemView.findViewById(R.id.lessonName);
            textFinalMark = itemView.findViewById(R.id.lessonMark);
            colorLine = itemView.findViewById(R.id.colorLine);
        }
    }


}

package com.example.govDiary;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.TreeMultimap;

import java.util.Objects;
import java.util.TreeMap;

public class AdapterTimetable extends RecyclerView.Adapter<AdapterTimetable.ViewHolder> {
    private LayoutInflater layoutInflater;
    private Context cont;
    private TreeMap<Integer, TreeMultimap<Integer,String>> lessonsAdapterMap = new TreeMap<>();  // <Номер недели, <Номер урока\текст для вывода>
    AdapterTimetable(Context context,TreeMap<Integer, TreeMultimap<Integer,String>> lessonsAdapterMap){
        this.lessonsAdapterMap = lessonsAdapterMap;
        this.layoutInflater = LayoutInflater.from(context);
        cont = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_timetable, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //holder.textName.setText(lessons.get(position).name);
        String text = "";
        int weekday = position + 1;
        String weekdayString;
        switch(weekday){
            case 1:
                weekdayString = "Понедельник";
                break;
            case 2:
                weekdayString = "Вторник";
                break;
            case 3:
                weekdayString = "Среда";
                break;
            case 4:
                weekdayString = "Четверг";
                break;
            case 5:
                weekdayString = "Пятница";
                break;
            case 6:
                weekdayString = "Суббота";
                break;
            default:
                weekdayString = "Ошибка";
                break;
        }
        holder.weekdayName.setText(weekdayString);
        TreeMultimap<Integer, String> tempMap = lessonsAdapterMap.get(position + 1);
        for (int i = 0; i < tempMap.size(); i++) {
            for (String s:
                    tempMap.get(i)) {
                text += s;
            }
        }
        text = text.substring(0, text.length() - 1);
        holder.weekdayText.setText(Html.fromHtml(text));
    }

    @Override
    public int getItemCount() {
        return Objects.requireNonNull(lessonsAdapterMap.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView weekdayName, weekdayText;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            weekdayName = itemView.findViewById(R.id.weekdayName);
            weekdayText = itemView.findViewById(R.id.weekdayText);
        }
    }
}

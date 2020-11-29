package com.example.govDiary;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterTimetable extends RecyclerView.Adapter<AdapterTimetable.ViewHolder> {
    private static final String TAG =  "AdapterTimetable";
    private LayoutInflater layoutInflater;
    private Context cont;
    private ArrayList<TimetableDays> timetableDays;
    AdapterTimetable(Context context, ArrayList<TimetableDays> timetableDays){
        this.timetableDays = timetableDays;
        this.layoutInflater = LayoutInflater.from(context);
        cont = context;
        Log.d(TAG, "AdapterTimetableConstructor: timetableDaysSize is " + timetableDays.size());
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_timetable, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.weekdayName.setText(timetableDays.get(position).name);
        ArrayList<TimetableLesson> curDay = timetableDays.get(position).lessons;
        holder.lessonsLayout.removeAllViews();
        for(int i = 0; i < curDay.size(); i++){
            View lessonView = layoutInflater.inflate(R.layout.timetable_item_list, null);
            TextView lessonString = lessonView.findViewById(R.id.lessonString);
            TextView lessonNum = lessonView.findViewById(R.id.lessonNum);
            TextView teacherText = lessonView.findViewById(R.id.teacherNameAndRoom);
            String lessonNameData = curDay.get(i).lessonName;
            String lessonGroupData = curDay.get(i).group;
            String roomNumData = curDay.get(i).room;
            String teacherNameData = curDay.get(i).teacher;
            if(!teacherNameData.equals("null")) teacherText.setText(teacherNameData);
            else teacherText.setVisibility(View.INVISIBLE);
            if(!lessonNum.equals(""))
                lessonNum.setText(curDay.get(i).lessonNumber);
            if(curDay.get(i).repeatedNum) lessonNum.setAlpha(0.0f);
            if(!lessonGroupData.equals("")){
                lessonString.setText(Html.fromHtml("<b>" + lessonNameData + "</b> (" + lessonGroupData + ") "));
            }
            else{
                lessonString.setText(Html.fromHtml("<b>" + lessonNameData + "</b> "));
            }
            holder.lessonsLayout.addView(lessonView);

        }


    }

    @Override
    public int getItemCount() {
        return timetableDays.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView weekdayName;
        LinearLayout lessonsLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            weekdayName = itemView.findViewById(R.id.weekdayName);
            lessonsLayout = itemView.findViewById(R.id.lessonsLayout);
        }
    }
}

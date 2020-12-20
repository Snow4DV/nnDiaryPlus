package com.example.govDiary;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.ContentValues.TAG;

public class AdapterDiary extends RecyclerView.Adapter<AdapterDiary.ViewHolder> {
    private LayoutInflater layoutInflater;
    private List<Lesson> lessons;
    private Context cont;
    private String[] colors = {"#f44336", "#e91e63", "#9c27b0", "#673ab7", "#3f51b5", "#1565c0", "#03a9f4", "#00bcd4", "#009688", "#4caf50", "#8bc34a", "#cddc39", "#ffeb3b", "#ffc107", "#ff9800", "#ff5722", "#f44336", "#e91e63", "#9c27b0", "#673ab7", "#4caf50"}; //16 colors
    AdapterDiary(Context context, List<Lesson> lessons){
        this.layoutInflater = LayoutInflater.from(context);
        this.lessons = lessons;
        cont = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = layoutInflater.inflate(R.layout.item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.linearLayout.removeAllViews();
        //on click listener to every carcview
        holder.textName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(lessons.get(position).homework.equals("") || lessons.get(position).homework.equals("null") || lessons.get(position).homework == null)) {
                    Toast.makeText(cont, "Домашнее задание скопировано!", Toast.LENGTH_SHORT).show();
                    ClipboardManager clipboard = (ClipboardManager) ((JournalActivity) cont).getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("homework", lessons.get(position).homework);
                    clipboard.setPrimaryClip(clip);
                }

            }
        });
        String mark = lessons.get(position).mark;
        String name = "<font color=" + getColorByString(lessons.get(position).name) +">" + lessons.get(position).number + ". " + "<b>" + lessons.get(position).name + "</b" + "</font>";
        String homework = lessons.get(position).homework;
        holder.textName.setText(Html.fromHtml(name));
        holder.textHW.setText(homework);
        //есть ли оценка
        if(!(mark.equals("null"))){
            Log.d(TAG, "onBindViewHolder: pos" + position + "mark" + lessons.get(position).mark);
            holder.textMark.setText(Html.fromHtml(mark));
            holder.space.getLayoutParams().height = 0;
            holder.textMark.setVisibility(View.VISIBLE);

        }
        else{
            holder.textMark.setVisibility(View.GONE);
        }
        //есть ли тема
        if(!(lessons.get(position).topic == null || lessons.get(position).topic == "null")){
            holder.textTopic.setVisibility(View.VISIBLE);
        }
        else{
            holder.textTopic.setVisibility(View.GONE);
        }
        //есть ли домашнее задание
        if(!(lessons.get(position).homework == null || homework == "null")){
            holder.textHW.setVisibility(View.VISIBLE);
        }
        else{
            holder.textHW.setVisibility(View.GONE);
        }
        holder.textTopic.setText(lessons.get(position).topic);
        holder.options.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType("vnd.android.cursor.item/event");

                Calendar cal = Calendar.getInstance();
                long startTime = cal.getTimeInMillis();
                long endTime = cal.getTimeInMillis() + 60 * 60 * 1000;

                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime);
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);

                intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);

                //This is Information about Calender Event.
                String namePlainStr = Html.fromHtml(name).toString();
                intent.putExtra(CalendarContract.Events.TITLE, "Д/З " + namePlainStr.substring(3));
                intent.putExtra(CalendarContract.Events.DESCRIPTION, Html.fromHtml(homework).toString());

                cont.startActivity(intent);
            }
        });
        if(lessons.get(position).files != null){
            List<String> keys = new ArrayList<>(lessons.get(position).files.keySet());
            List<String> values = new ArrayList<>(lessons.get(position).files.values());
            for (int i = 0; i < keys.size(); i++) {
                View fileView = layoutInflater.inflate(R.layout.file_item_with_padding, null);
                TextView fileName = fileView.findViewById(R.id.fileName);
                fileName.setText(keys.get(i));
                final int ind = i;
                fileName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(values.get(ind)));
                        cont.startActivity(i);
                    }
                });
                holder.linearLayout.addView(fileView);
            }
        }
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView textMark, textHW, textTopic, textName;
        Space space;
        ImageButton options;
        CardView cardView;
        LinearLayout linearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.lessonName);
            space = itemView.findViewById(R.id.spaceItem);
            textHW = itemView.findViewById(R.id.lessonHomeWork);
            textMark = itemView.findViewById(R.id.lessonMark);
            textTopic = itemView.findViewById(R.id.lessonTopic);
            options = itemView.findViewById(R.id.optionsButton);
            cardView = itemView.findViewById(R.id.cardViewDiary);
            linearLayout = itemView.findViewById(R.id.linearFiles);
        }


    }


    private String getColorByString(String s){
        char firstChar = Character.toLowerCase(s.charAt(0));
        String alphabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
        int index = alphabet.indexOf(firstChar)/2;
        return colors[index];
    }





}

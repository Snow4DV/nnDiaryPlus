package com.example.govDiary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;
import java.util.List;

public class MessagesListAdapter extends ArrayAdapter<Message> {
    ArrayList<Message> messages;
    Context context;
    String authToken = "";

    public MessagesListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Message> objects, Context cont, String authT) {
        super(context, resource, objects);
        this.context = cont;
        this.messages = objects;
        this.authToken = authT;
    }


    @Override
    public int getCount() {
        return messages.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        Message curMessage = getItem(position);
        if(v == null){
            v = LayoutInflater.from(getContext()).inflate(R.layout.message_item, parent, false);
        }
        CardView cardView = v.findViewById(R.id.message_card);
        TextView senderText = v.findViewById(R.id.sender);
        TextView shortText = v.findViewById(R.id.brief);
        TextView subject = v.findViewById(R.id.subject);
        TextView date = v.findViewById(R.id.date);
        ImageView avatarView = v.findViewById(R.id.avatar);
        ImageView attachedFileIconView = v.findViewById(R.id.fileAttached);
        StringBuilder usersString = new StringBuilder();
        for(String user: curMessage.users.values()){
            usersString.append(user);
            usersString.append(", ");
        }
        if(curMessage.unread){
            subject.setTypeface(Typeface.DEFAULT_BOLD);
            senderText.setTypeface(Typeface.DEFAULT_BOLD);
        }
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ViewMessageActivity.class);
                intent.putExtra("authToken", authToken);
                intent.putExtra("userFromTo", usersString.toString());
                intent.putExtra("messageId", curMessage.id);
                getContext().startActivity(intent);
            }
        });
        date.setText(curMessage.msgDate);
        senderText.setText(usersString.substring(0, usersString.length() - 2));
        shortText.setText(Html.fromHtml(curMessage.shortText));
        subject.setText(curMessage.subject);
        if(curMessage.withFiles) attachedFileIconView.setVisibility(View.VISIBLE);
        else attachedFileIconView.setVisibility(View.INVISIBLE);
        ColorGenerator generator = ColorGenerator.MATERIAL; //generating avatar
        TextDrawable drawable = TextDrawable.builder()
                .buildRect(usersString.substring(0,1), generator.getColor(usersString.toString().split(" ")[0]));
        avatarView.setImageDrawable(drawable);
        return v;
    }
}

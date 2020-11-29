package com.example.govDiary;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MessagesViewPagerAdapter extends FragmentStateAdapter {


    public MessagesViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }
    MessagesFragment mesFragIn, mesFragOut;
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0:
                mesFragIn = new MessagesFragment();
                mesFragIn.setUsersObject("user_from");
                return mesFragIn;
            case 1:
                mesFragOut = new MessagesFragment();
                mesFragOut.setUsersObject("users_to");
                return mesFragOut;
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public void refreshMessages(){
        if(mesFragIn != null)
            mesFragIn.refreshMessages();
        if(mesFragOut!= null)
            mesFragOut.refreshMessages();
    }
}

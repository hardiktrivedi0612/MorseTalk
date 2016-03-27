package com.utdallas.hpt150030.morsetalktry;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roshan on 27/03/16.
 */
public class MessageAdapter extends BaseAdapter
{
    private List<Message> messages = new ArrayList<Message>();

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            view = layoutInflater.inflate(R.layout.message_item_layout, parent,false);
        }

        Message msg = messages.get(position);

        TextView tv = (TextView)view.findViewById(R.id.date_time);
        tv.setText(msg.getTime());

        if(msg.getSentBy().equals("Y"))
        {
            view.setBackgroundColor(Color.GREEN);
        }
        else
        {
            view.setBackgroundColor(Color.YELLOW);
        }
        return view;
    }

    public void addMessages(List<Message> msg)
    {
        messages = msg;
    }
}

package com.utdallas.hpt150030.morsetalktry;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Hardik on 3/27/2016.
 */
public class ChatsScreenAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private List<String> list;

    public ChatsScreenAdapter(Context context, int textViewResourceId, List<String> objects) {
        super(context, textViewResourceId, objects);
        this.mContext = context;
        this.list = objects;
    }

    class ViewHolder {
        TextView email;
        ImageView icon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chats_screen_listitem, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.email = (TextView) convertView.findViewById(R.id.nameTextView);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String bean = list.get(position);
        if (bean != null) {

            int value = (Math.abs(bean.hashCode()) % 5) + 1;
            switch (value) {
                case 1:
                    viewHolder.icon.setImageResource(R.mipmap.contact1);
                    break;
                case 2:
                    viewHolder.icon.setImageResource(R.mipmap.contact2);
                    break;
                case 3:
                    viewHolder.icon.setImageResource(R.mipmap.contact3);
                    break;
                case 4:
                    viewHolder.icon.setImageResource(R.mipmap.contact4);
                    break;
                case 5:
                    viewHolder.icon.setImageResource(R.mipmap.contact5);
                    break;

            }

            viewHolder.email.setText(bean);
        }
        return convertView;
    }
}
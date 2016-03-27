package com.utdallas.hpt150030.morsetalktry;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roshan on 27/03/16.
 */
public class MessagesActivity extends Activity
{
    private MessageAdapter msgAdapter;
    private Vibrator vibrator;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_layout);

        ListView lv = (ListView)findViewById(R.id.messages_list);
        msgAdapter = new MessageAdapter();

//        Bundle extras = getIntent().getExtras();
//        String user = extras.getString("USER");
        String user = getIntent().getStringExtra(Constants.EMAIL);

        msgAdapter.addMessages(dbOperation(user));

        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        lv.setAdapter(msgAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position,long id)
            {
                Message msg = (Message) msgAdapter.getItem(position);

                long[] vibratePattern = new long[(msg.getMessage().length() * 2) + 1];
                int count = 0;
                vibratePattern[count++] = 0;
                //Toast.makeText(MorseActivity.this, strBuilder.toString(), Toast.LENGTH_SHORT).show();
                for (char c : msg.getMessage().toCharArray()) {
                    switch (c) {
                        case 'd':
                            vibratePattern[count++] = 150;
                            vibratePattern[count++] = 150;
                            break;
                        case 'u':
                            vibratePattern[count++] = 450;
                            vibratePattern[count++] = 150;
                            break;
                        case 'c':
                            vibratePattern[count++] = 0;
                            vibratePattern[count++] = 450;
                            break;
                        case 'w':
                            vibratePattern[count++] = 0;
                            vibratePattern[count++] = 1050;
                            break;
                    }

                }

                vibrator.vibrate(vibratePattern, -1);
            }
        });
    }

    public List<Message> dbOperation(String user)
    {
        DBOperations db = new DBOperations(MessagesActivity.this);
        return db.getAllMessages(user);
    }
}

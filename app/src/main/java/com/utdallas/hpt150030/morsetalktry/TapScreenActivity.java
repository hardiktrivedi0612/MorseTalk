package com.utdallas.hpt150030.morsetalktry;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * Created by Hardik on 3/26/2016.
 */
public class TapScreenActivity extends AppCompatActivity {

    private static final String TAG = "TapScreenActivity";

    private SurfaceView morseTapView;
    private GestureDetector gestureDetector = null;
    private long downTime;
    private long upTime;
    private StringBuilder strBuilder = new StringBuilder();
    private Button sendButton;
    private ProgressDialog progressDialog;
    private String toEmail;
    private String fromEmail;
    private DBOperations dbOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap_screen);

        dbOperations = new DBOperations(TapScreenActivity.this);

        String email = getIntent().getStringExtra(Constants.EMAIL);
        if(email == null || email.equals("")) {
            Toast.makeText(this, "Sender not properly selected. Please try again",Toast.LENGTH_SHORT).show();
            finish();
        }

        toEmail = email;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TapScreenActivity.this);
        fromEmail = sharedPreferences.getString(Constants.EMAIL, "");


        morseTapView = (SurfaceView)findViewById(R.id.morseTapView);
        sendButton = (Button)findViewById(R.id.sendButton);

        morseTapView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getActionMasked();
                switch (action)
                {
                    case MotionEvent.ACTION_DOWN:
                        downTime = System.currentTimeMillis();
                        if((downTime - upTime) > 900 && (downTime - upTime) < 2100)
                        {
//                            strBuilder.append('/');
                            strBuilder.append('c');

                        }
                        else if((downTime - upTime) > 2100 && upTime > 0)
                        {
//                            strBuilder.append('*');
                            strBuilder.append('w');
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        upTime = System.currentTimeMillis();
                        if((upTime - downTime) < 300)
                        {
//                            strBuilder.append('.');
                            strBuilder.append('d');
                        }
                        else
                        {
                            strBuilder.append('u');
//                            strBuilder.append('_');
                        }
                        break;
                }
                return true;
            }
        });


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SendMorseCode(TapScreenActivity.this,strBuilder.toString()).execute();

                strBuilder = new StringBuilder();
            }
        });
    }

    private class SendMorseCode extends AsyncTask<String, Void, Void> {

        private Context context;
        private String message;

        public SendMorseCode(Context c, String message) {
            this.context = c;
            this.message = message;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(this.context);
            progressDialog.setMessage("Sending Morse Code. Please wait");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                URL url = new URL("http://MorseAppServerEnv.mtpu2zva7q.us-west-2.elasticbeanstalk.com/MorsePushServlet?fromUserId="+fromEmail+"&toUserId="+toEmail+"&message="+message);
                Log.d(TAG, url.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                Log.d(TAG,connection.toString());
                connection.setRequestMethod("GET");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                final int responseCode = connection.getResponseCode();
                Log.d(TAG,""+responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                while ((line = br.readLine()) != null) {
//                    Toast.makeText(context.getApplicationContext(),line,Toast.LENGTH_SHORT).show();
                    Log.d(TAG, line);
                }
                br.close();
                TapScreenActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if(responseCode == 200) {
                            //success
                            Message msg = new Message();
                            msg.setUser(toEmail);
                            msg.setMessage(message);
                            msg.setSentBy("Y");
                            msg.setTime(new java.sql.Timestamp(new java.util.Date().getTime()).toString());
                            dbOperations.insertMessage(msg);
                            Toast.makeText(TapScreenActivity.this, "Message successfully sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TapScreenActivity.this, "Message could not be sent", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
            finally {
                progressDialog.dismiss();
            }

            return null;
        }
    }
}

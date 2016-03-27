package com.utdallas.hpt150030.morsetalktry;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Button submitbutton;
    private EditText emailEditText;

    private static final String TAG = "MainActivity";
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressDialog progressDialog;
    private boolean isReceiverRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String email = sharedPreferences.getString(Constants.EMAIL, "");
        if (!email.equals("")) {
            //user is already logged in
            Intent chatScreenActivity = new Intent(MainActivity.this, ChatsScreenActivity.class);
            startActivity(chatScreenActivity);
            finish();
        }


        setContentView(R.layout.activity_main);

        submitbutton = (Button) findViewById(R.id.submitButton);
        emailEditText = (EditText) findViewById(R.id.emailId);

        submitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditText.getText().toString();
                //TODO: Input validation of the email text


                //Making HTTP request

                new GetRequest(MainActivity.this, email).execute();
            }
        });


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                disMissDialog();

                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean("sentTokenToServer", false);
//                if (sentToken) {
//                    Toast.makeText(MainActivity.this, "Token retrieved and sent to server! You can now use gcmsender to\n" +
//                            "        send downstream messages to this app.", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(MainActivity.this, "An error occurred while either fetching the InstanceID token,\n" +
//                            "        sending the fetched token to the server or subscribing to the PubSub topic. Please try\n" +
//                            "        running the sample again.", Toast.LENGTH_SHORT).show();
//                }
            }
        };

        showDialog();
        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(MainActivity.this, RegistrationIntentService.class);
            startService(intent);
        }

    }

    private void registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter("registrationComplete"));
            isReceiverRegistered = true;
        }
    }

    protected void showDialog() {
        try {
            if (progressDialog == null) {
                progressDialog = ProgressDialog.show(this, "", "Registering GCM. Please wait");
            } else {
                progressDialog.show();
            }
        } catch (Exception e) {

        }
    }

    protected void disMissDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        } catch (Exception e) {
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 9000)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    private class GetRequest extends AsyncTask<String, Void, Void> {

        private Context context;
        private String email;

        public GetRequest(Context c, String email) {
            this.context = c;
            this.email = email;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(this.context);
            progressDialog.setMessage("Please wait");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String token = sharedPreferences.getString("gcmToken", "");
            if (token.equals("")) {
                registerReceiver();
                if (checkPlayServices()) {
                    // Start IntentService to register this application with GCM.
                    Intent intent = new Intent(MainActivity.this, RegistrationIntentService.class);
                    startService(intent);
                }
                return null;
            }
            try {
                URL url = new URL("http://MorseAppServerEnv.mtpu2zva7q.us-west-2.elasticbeanstalk.com/RegistrationServlet?userId=" + email + "&registrationToken=" + token);
                Log.d(TAG, url.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                Log.d(TAG,connection.toString());
                connection.setRequestMethod("GET");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                final int responseCode = connection.getResponseCode();
                Log.d(TAG, "" + responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                while ((line = br.readLine()) != null) {
//                    Toast.makeText(context.getApplicationContext(),line,Toast.LENGTH_SHORT).show();
                    Log.d(TAG, line);
                }
                br.close();
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (responseCode == 200) {
                            //success
                            sharedPreferences.edit().putString("email", email).apply();
                            Toast.makeText(MainActivity.this, "Email stored", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Could not complete the service. Please try again", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                        Intent chatScreenActivity = new Intent(MainActivity.this, ChatsScreenActivity.class);
                        startActivity(chatScreenActivity);
                        finish();
                    }
                });
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            } finally {
                progressDialog.dismiss();
            }

            return null;
        }
    }
}

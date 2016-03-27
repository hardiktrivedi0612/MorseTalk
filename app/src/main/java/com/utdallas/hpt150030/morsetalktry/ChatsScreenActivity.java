package com.utdallas.hpt150030.morsetalktry;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ChatsScreenActivity extends AppCompatActivity {

    private final String TAG = "ChatScreenActivity";
    private ProgressDialog progressDialog;
    private List<String> chats;
    private DBOperations dbOperations;
    private ListView listView;
    private ChatsScreenAdapter chatsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//                Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts/people"));
                pickContactIntent.setType(ContactsContract.CommonDataKinds.Email.CONTENT_TYPE);
                ChatsScreenActivity.this.startActivityForResult(pickContactIntent, Constants.PICK_CONTACT_REQUEST);
            }
        });

        dbOperations = new DBOperations(ChatsScreenActivity.this);
        chats = dbOperations.getAllChats();

        listView = (ListView) findViewById(R.id.listView);
        chatsAdapter = new ChatsScreenAdapter(this, R.id.listView, chats);
        listView.setAdapter(chatsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent tapScreen = new Intent(ChatsScreenActivity.this, TapScreenActivity.class);
                tapScreen.putExtra(Constants.EMAIL, chats.get(position));
                startActivity(tapScreen);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(ChatsScreenActivity.this, "Long item click",Toast.LENGTH_SHORT).show();
                Intent messagesScreen = new Intent(ChatsScreenActivity.this, MessagesActivity.class);
                messagesScreen.putExtra(Constants.EMAIL, chats.get(position));
                startActivity(messagesScreen);
                return true;
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        chatsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (requestCode == Constants.PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
//                // Get the URI that points to the selected contact
//                Uri contactUri = data.getData();
//                // We only need the NUMBER column, because there will be only one row in the result
//                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
//
//                // Perform the query on the contact to get the NUMBER column
//                // We don't need a selection or sort order (there's only one result for the given URI)
//                // CAUTION: The query() method should be called from a separate thread to avoid blocking
//                // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
//                // Consider using CursorLoader to perform the query.
//                Cursor cursor = getContentResolver()
//                        .query(contactUri, projection, null, null, null);
//                cursor.moveToFirst();
//
//                // Retrieve the phone number from the NUMBER column
//                int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
//                String number = cursor.getString(column);
//
//                // Do something with the phone number...
                Cursor cursor = null;
                String email = "", name = "";
                try {
                    Uri result = data.getData();
                    Log.v(TAG, "Got a contact result: " + result.toString());

                    cursor = getContentResolver().query(result, null, null, null, null);
                    cursor.moveToFirst();

                    // let's just get the first email
                    if (cursor.moveToFirst()) {
                        int nameId = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                        name = cursor.getString(nameId);
                        int emailIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                        email = cursor.getString(emailIdx);

                        Log.v(TAG, "Got email: " + email);

                        //Service Call To check if the contact exists in MorseTalk
//                        Toast.makeText(this, name + " : " + email, Toast.LENGTH_SHORT).show();

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ChatsScreenActivity.this);
                        String myEmail = sharedPreferences.getString(Constants.EMAIL, "");

                        new VerifyUser(ChatsScreenActivity.this, email, myEmail).execute();

                    } else {
                        Log.w(TAG, "No results");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to get email data", e);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (email.length() == 0 && name.length() == 0) {
                        Toast.makeText(this, email + " " + name + " " + "No Email for Selected Contact", Toast.LENGTH_LONG).show();
                    }

                }
            }
        }
    }

    private class VerifyUser extends AsyncTask<String, Void, Void> {

        private Context context;
        private String toEmail, fromEmail;

        public VerifyUser(Context c, String toEmail, String fromEmail) {
            this.context = c;
            this.toEmail = toEmail;
            this.fromEmail = fromEmail;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(this.context);
            progressDialog.setMessage("Verifying user. Please wait");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                URL url = new URL("http://MorseAppServerEnv.mtpu2zva7q.us-west-2.elasticbeanstalk.com/MorsePushServlet?fromUserId=" + fromEmail + "&toUserId=" + toEmail);
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

                ChatsScreenActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (responseCode == 200) {
                            //success
                            //if success then go to TapScreen
                            Intent tapScreen = new Intent(ChatsScreenActivity.this, TapScreenActivity.class);
                            tapScreen.putExtra(Constants.EMAIL, toEmail);
                            startActivity(tapScreen);
                        } else {
                            Toast.makeText(ChatsScreenActivity.this, "Sorry! This user does not exist. You invite them to MorseTalk!", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                ChatsScreenActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChatsScreenActivity.this, "Sorry! This user does not exist. You invite them to MorseTalk!", Toast.LENGTH_SHORT).show();
                    }
                });

            } finally {
                progressDialog.dismiss();
            }
            return null;
        }
    }
}

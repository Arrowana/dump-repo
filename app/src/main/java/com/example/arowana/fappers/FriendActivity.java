package com.example.arowana.fappers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FriendActivity extends Activity implements AsyncResponse {

    private ListView friendList;
    private FriendAdapter friends;
    private EditText searchET;
    private Button findButton;
    private TextView usernameTV;
    private Button addButton;
    private ProgressDialog progress;
    private String idFriend;
    private String newUser;
    private String[] friendStrings = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        String friendsJson = getIntent().getStringExtra("friendsJson");

        //Read data in shared Preferences
        final SharedPreferences prefs = this.getSharedPreferences("fappers", this.MODE_PRIVATE);
        final String username=prefs.getString("username", "Error");
        final String id=prefs.getString("id", "Error");

        searchET = (EditText) findViewById(R.id.searchET);
        findButton = (Button) findViewById(R.id.findButton);

        usernameTV = (TextView) findViewById(R.id.usernameTV);
        addButton = (Button) findViewById(R.id.addButton);

        friendList = (ListView) findViewById(R.id.friendList);

        findButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                boolean contains = false;
                for(String user : friendStrings){
                    if(user.equals(searchET.getText().toString())){
                        contains=true;
                    }
                }
                //Checks if already friends
                if(!contains) {
                    usernameTV.setVisibility(View.GONE);
                    addButton.setVisibility(View.GONE);
                    progress = ProgressDialog.show(FriendActivity.this, "Chargement", "Veuillez patienter");

                    // Add data to nameValuePairs
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("action", "search"));
                    nameValuePairs.add(new BasicNameValuePair("username", searchET.getText().toString()));

                    HttpAsyncTask myTask = new HttpAsyncTask("http://10.0.2.2/fp/", FriendActivity.this);
                    myTask.execute(nameValuePairs);
                }
                else{
                    Toast.makeText(FriendActivity.this, "You are already friend with " + searchET.getText().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                progress = ProgressDialog.show(FriendActivity.this, "Chargement", "Veuillez patienter");

                // Add data to nameValuePairs
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("action", "addFriend"));
                nameValuePairs.add(new BasicNameValuePair("id", id));
                nameValuePairs.add(new BasicNameValuePair("idFriend", idFriend));

                newUser = searchET.getText().toString();

                HttpAsyncTask myTask = new HttpAsyncTask("http://10.0.2.2/fp/", FriendActivity.this);
                myTask.execute(nameValuePairs);
            }
        });

        //Reading friend list from JSON and populating ListView
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(friendsJson);

            JSONArray jsonArray = jsonObject.getJSONArray("friends");

            friendStrings = new String[jsonArray.length()];
            String[] idStrings = new String[jsonArray.length()];

            for (int i = 0; i < jsonArray.length(); i++) {
                friendStrings[i] = jsonArray.getJSONObject(i).getString("username");
                idStrings[i] = jsonArray.getJSONObject(i).getString("id");
            }

            //Put Strings inside ArrayList so we can use add on ArrayAdapter
            ArrayList<String> list=new ArrayList<String>();
            list.addAll(Arrays.asList(friendStrings));
            friends = new FriendAdapter(this, list);
            friendList.setAdapter(friends);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processFinish(String result) {
        Log.v("FP", "Message reçu : " + result);
        progress.dismiss();
        usernameTV.setText("Username doesn't exist");

        //Reading friend list from JSON
        JSONObject jsonObject = null;
        String action = null;
        String success = null;
        String username = null;
        idFriend = null;
        try {
            jsonObject = new JSONObject(result);
            action = jsonObject.getString("action");
            success = jsonObject.getString("success");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Follow response to action
        if(action.equals("search")){
            usernameTV.setVisibility(View.VISIBLE);

            if(success.equals("1")){
                try {
                    idFriend = jsonObject.getString("id");
                    username = jsonObject.getString("username");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                usernameTV.setText(username);
                addButton.setVisibility(View.VISIBLE);
            }
            else if(success.equals("0")){
                Toast.makeText(this, "Can't find this user", Toast.LENGTH_LONG).show();
            }
        }
        else if(action.equals("addFriend")){
            if(success.equals("1")){
                usernameTV.setVisibility(View.GONE);
                addButton.setVisibility(View.GONE);

                //Adding user to friendList ListView
                friends.add(newUser);
                friends.notifyDataSetChanged();

                Toast.makeText(this,"Friend added", Toast.LENGTH_LONG).show();
            }
            else if(success.equals("0")){
                Toast.makeText(this, "Add failed", Toast.LENGTH_LONG).show();
            }
        }
    }
}

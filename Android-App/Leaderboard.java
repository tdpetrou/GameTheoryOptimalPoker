package com.tdp.coolp;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.widget.LoginButton;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.AccessToken;
import com.facebook.login.widget.ProfilePictureView;

import com.facebook.*;
import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;


public class Leaderboard extends FragmentActivity {

    private ProfilePictureView profilePictureView;
    private String fbName;
    private String fbID;
    AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        Log.d("leaderboard2", "just began");

        try {
            accessToken = AccessToken.getCurrentAccessToken();
        } catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
            Log.e("access token error", "exception", e);
        } finally {
            addProfilePic();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_leaderboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addProfilePic(){
        Log.d("leaderboard2", "in profile pic");
        profilePictureView = (ProfilePictureView) findViewById(R.id.selfProfilePic);

        GraphRequest request =  GraphRequest.newMeRequest(accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject user, GraphResponse response) {
                        if (user != null) {
                            fbName = user.optString("name");
                            fbID = user.optString("id");
                            profilePictureView.setProfileId(fbID);
                            ((TextView) findViewById(R.id.selfName)).setText(fbName);
                        }
                    }
                });

        GraphRequest friendRequest = GraphRequest.newMyFriendsRequest(
                accessToken,
                new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(
                            JSONArray friendList,
                            GraphResponse response) {
                        if (friendList != null){
                         Log.d("leaderboard2", friendList.toString());
                        }
                    }
                });



        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,birthday,gender");
        request.setParameters(parameters);
        request.executeAsync();

        friendRequest.executeAsync();
    }
}

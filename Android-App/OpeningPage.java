

package com.tdp.coolp;

import java.util.Arrays;
import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.Configuration;



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

import org.json.JSONObject;


public class OpeningPage extends FragmentActivity {

    private CallbackManager callbackManager;
    private ProfilePictureView profilePictureView;
    LoginButton loginButton;
    private String fbName;
    private String fbID;
    AccessToken accessToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        Log.d("On create", "made it to oncreate");
        setContentView(R.layout.activity_opening_page);

        try {
            accessToken = AccessToken.getCurrentAccessToken();
        } catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
            Log.e("access token error", "exception", e);
        } finally {
            getPlayerInfo();
        }


        callbackManager = CallbackManager.Factory.create();
        Log.d("fb login", "just about to start it");
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Toast.makeText(getApplicationContext(), "already logged in", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        loginButton = (LoginButton) findViewById(R.id.login_button);
        List<String> permissionNeeds = Arrays.asList("user_photos", "email", "user_birthday",
                "public_profile", "user_friends");

        loginButton.setReadPermissions(permissionNeeds);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                getPlayerInfo();

            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_opening_page);
    }

    public void getPlayerInfo(){
        Log.d("fb login", "success");
//        profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
        Log.d("fb login", "got picture");
        GraphRequest request = GraphRequest.newMeRequest(accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject user, GraphResponse response) {
                        if (user != null) {
                            fbName = user.optString("name");
                            fbID = user.optString("id");
//                            profilePictureView.setVisibility(View.VISIBLE);
                            Log.d("fb login", fbID);
//                            profilePictureView.setProfileId(fbID);

                            Toast.makeText(getApplicationContext(), user.optString("birthday") +
                                    user.optString("name") + user.optString("gender"), Toast.LENGTH_LONG).show();
                        }
                    }
                });

        Log.d("fb login" ,"bundling");
        Bundle parameters = new Bundle();
        Log.d("fb login", "adding parameters");
        parameters.putString("fields", "id,name,link,birthday,gender");
        Log.d("fb login", "setting parameters");
        request.setParameters(parameters);
        Log.d("fb login", "about to execute");
        request.executeAsync();
    }

    public void BeginPlay(View v) {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        intent.putExtra("fbName", fbName);
        intent.putExtra("fbID", fbID);
        this.startActivity(intent);
    }

    public void leaderboard(View v){
        Intent intent = new Intent();
        intent.setClass(this, Leaderboard.class);
        this.startActivity(intent);
    }

    public void settings(View v){
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.settings_dialog);
        dialog.setTitle("Title...");
        dialog.getWindow().getAttributes().windowAnimations = R.style.Animations_SmileWindow;

        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.text);
        text.setText("Android custom dialog example!");
        ImageView image = (ImageView) dialog.findViewById(R.id.image);
        image.setImageResource(R.drawable.ic_launcher);

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void achievements(View v){
        Intent intent = new Intent();
        intent.setClass(this, Achievements.class);
        this.startActivity(intent);
    }
}

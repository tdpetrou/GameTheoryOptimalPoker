

package com.tdp.coolp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.content.res.Configuration;


import com.facebook.login.widget.LoginButton;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.AccessToken;

import com.facebook.*;
import com.facebook.login.LoginManager;


public class OpeningPage extends FragmentActivity {
    
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        //
        //        try {
        //            PackageInfo info = getPackageManager().getPackageInfo(
        //                    "com.tdp.coolp",
        //                    PackageManager.GET_SIGNATURES);
        //            for (Signature signature : info.signatures) {
        //                MessageDigest md = MessageDigest.getInstance("SHA");
        //                md.update(signature.toByteArray());
        //                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
        //            }
        //        } catch (NameNotFoundException e) {
        //
        //        } catch (NoSuchAlgorithmException e) {
        //
        //        }
        
        
        
        
        callbackManager = CallbackManager.Factory.create();
        //        LoginButton loginButton = (LoginButton) view.findViewById(R.id.usersettings_fragment_login_button);
        
        
        LoginManager.getInstance().registerCallback(callbackManager,
                                                    new FacebookCallback<LoginResult>() {
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
        setContentView(R.layout.activity_opening_page);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_opening_page);
    }
    
    private void getPlayerInfo() {
        String name = "default";
        String id = "1";
        if (AccessToken.getCurrentAccessToken() != null) {
            Profile profile = Profile.getCurrentProfile();
            id = profile.getId();
            name = profile.getName();
        }
        Log.d("fucker", name + ' ' + id);
    }
    
    public void BeginPlay(View v) {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        //        intent.putExtra("fbName", mainFragment.fbData[0]);
        //        intent.putExtra("fbID", mainFragment.fbData[1]);
        this.startActivity(intent);
    }
    
    
    
    //    @Override
    //    protected void onResume() {
    //        super.onResume();
    //
    //        // Call the 'activateApp' method to log an app event for use in analytics and advertising
    //        // reporting.  Do so in the onResume methods of the primary Activities that an app may be
    //        // launched into.
    //        AppEventsLogger.activateApp(this);
    //
    //
    //    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    
    
    //    @Override
    //    public void onPause() {
    //        super.onPause();
    //
    //        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
    //        // reporting.  Do so in the onPause methods of the primary Activities that an app may be
    //        // launched into.
    //        AppEventsLogger.deactivateApp(this);
    //    }
    
    //    @Override
    //    protected void onDestroy() {
    //        super.onDestroy();
    //        profileTracker.stopTracking();
    //    }
}



//public class OpeningPage extends FragmentActivity {
//    private MainFragment mainFragment;
//
////    CallbackManager callbackManager;
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        callbackManager = CallbackManager.Factory.create();
//        LoginButton loginButton = (LoginButton) view.findViewById(R.id.usersettings_fragment_login_button);
//        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() { ... });
//    }
//
//    private boolean mResolvingConnectionFailure = false;
//    private boolean mAutoStartSignInFlow = true;
//    private boolean mSignInClicked = false;
//    boolean mExplicitSignOut = false;
//    boolean mInSignInFlow = false; // set to true when you're in the middle of the
//    // sign in flow, to know you should not attempt
//    // to connect in onStart()
//
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////
////        if (savedInstanceState == null) {
////            // Add the fragment on initial activity setup
////            mainFragment = new MainFragment();
////            getSupportFragmentManager()
////                    .beginTransaction()
////                    .add(android.R.id.content, mainFragment)
////                    .commit();
////        } else {
////            // Or set the fragment from restored state info
////            mainFragment = (MainFragment) getSupportFragmentManager()
////                    .findFragmentById(android.R.id.content);
////        }
////
////        try {
////            PackageInfo info = getPackageManager().getPackageInfo(
////                    "com.tdp.coolp",
////                    PackageManager.GET_SIGNATURES);
////            for (Signature signature : info.signatures) {
////                MessageDigest md = MessageDigest.getInstance("SHA");
////                md.update(signature.toByteArray());
////                String hash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
////                Toast.makeText(getApplicationContext(),hash, Toast.LENGTH_LONG);
////                Log.d("KeyHash:", hash);
////            }
////        } catch (NameNotFoundException e) {
////            Log.d("Name not found", e.toString());
////        } catch (NoSuchAlgorithmException e) {
////            Log.d("No such algo", e.toString());
////        }
////
////    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.opening_page, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        // Logs 'install' and 'app activate' App Events.
//        AppEventsLogger.activateApp(this);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        // Logs 'app deactivate' App Event.
//        AppEventsLogger.deactivateApp(this);
//    }
//
//
//    public void BeginPlay(View v) {
//        Intent intent = new Intent();
//        intent.setClass(this, MainActivity.class);
//        intent.putExtra("fbName", mainFragment.fbData[0]);
//        intent.putExtra("fbID", mainFragment.fbData[1]);
//        this.startActivity(intent);
//    }
//
//    public void achievements(View v){
////        startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), REQUEST_ACHIEVEMENTS);
//
//    }
//
//
//    public static class MainFragment extends Fragment {
//        private static final String TAG = "MainFragment";
//        CallbackManager callbackManager;
//        String[] fbData = new String[2];
//
////        private Session.StatusCallback callback = new Session.StatusCallback() {
////            @Override
////            public void call(Session session, SessionState state, Exception exception) {
////                onSessionStateChange(session, state, exception);
////            }
////        };
//
//        public MainFragment() {
//        }
//
//        @Override
//        public View onCreateView(
//                LayoutInflater inflater,
//                ViewGroup container,
//                Bundle savedInstanceState) {
//            View view = inflater.inflate(R.layout.activity_opening_page, container, false);
//
//            LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
//            loginButton.setReadPermissions("user_friends");
//            // If using in a fragment
//            loginButton.setFragment(this);
//            // Other app specific specialization
//
//            // Callback registration
//            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//                @Override
//                public void onSuccess(LoginResult loginResult) {
//                    // App code
//                }
//
//                @Override
//                public void onCancel() {
//                    // App code
//                }
//
//                @Override
//                public void onError(FacebookException exception) {
//                    // App code
//                }
//            });
//
//            return view;
//        }
//
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//
//            FacebookSdk.sdkInitialize(getApplicationContext());
//            callbackManager = CallbackManager.Factory.create();
//            LoginButton loginButton = (LoginButton) view.findViewById(R.id.usersettings_fragment_login_button);
//            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() { ... });
//        }
//
////        private void onSessionStateChange(Session session, SessionState state, Exception exception) {
////            if (state.isOpened()) {
////                Log.i(TAG, "Logged in...");
////                Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
////
////                    @Override
////                    public void onCompleted(GraphUser user, Response response) {
////                        if (user != null) {
////                            // Display the parsed user info
////                            fbData = buildUserInfoDisplay(user);
//////                            Log.d("User info", fbData);
////                        }
////                    }
////                });
////            } else if (state.isClosed()) {
////                Log.i(TAG, "Logged out...");
////            }
////            Log.d("session state changed", "state : " + state.toString());
////
////        }
//
////        @Override
////        public void onResume() {
////            super.onResume();
////
////            // For scenarios where the main activity is launched and user
////            // session is not null, the session state change notification
////            // may not be triggered. Trigger it if it's open/closed.
////            Session session = Session.getActiveSession();
////            if (session != null &&
////                    (session.isOpened() || session.isClosed()) ) {
////                onSessionStateChange(session, session.getState(), null);
////            }
////            uiHelper.onResume();
////        }
////
////        @Override
////        public void onActivityResult(int requestCode, int resultCode, Intent data) {
////            super.onActivityResult(requestCode, resultCode, data);
////            uiHelper.onActivityResult(requestCode, resultCode, data);
////        }
////
////        @Override
////        public void onPause() {
////            super.onPause();
////            uiHelper.onPause();
////        }
////
////        @Override
////        public void onDestroy() {
////            super.onDestroy();
////            uiHelper.onDestroy();
////        }
////
////        @Override
////        public void onSaveInstanceState(Bundle outState) {
////            super.onSaveInstanceState(outState);
////            uiHelper.onSaveInstanceState(outState);
////        }
//
//        private String[] buildUserInfoDisplay(GraphUser user) {
//            String[] data = new String[2];
//
//            // Example: typed access (name)
//            // - no special permissions required
//            data[0] = user.getName();
//
//            // Example: typed access (name)
//            // - no special permissions required
//            data[1] = user.getId();
//            return data;
//        }
//    }
//}

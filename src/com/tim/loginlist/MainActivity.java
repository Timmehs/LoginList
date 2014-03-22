package com.tim.loginlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;
import com.facebook.*;


/**
 * Created by Tim Sandberg on 3/20/14.
 */


public class MainActivity extends FragmentActivity {
    private static final int LOGIN = 0;
    private static final int FRIENDLIST = 1;
    private static final int FRAGMENT_COUNT = FRIENDLIST + 1;
    private boolean isResumed = false;
    private static final String TAG = "MainActivity";
    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    public static Session session;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        uiHelper = new UiLifecycleHelper(this, callback);
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);


        session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(this, null, callback, savedInstanceState);
            } else {
                session = new Session(this);
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this).setCallback(callback));
            } else {
                Log.d(TAG, "OnCreate(): No token loaded");
            }
        }


        FragmentManager fm = getSupportFragmentManager();
        fragments[LOGIN] = fm.findFragmentById(R.id.splashFragment);
        fragments[FRIENDLIST] = fm.findFragmentById(R.id.friendlistFragment);

        FragmentTransaction transaction = fm.beginTransaction();
        for(Fragment f : fragments) {
            transaction.hide(f);
        }
        transaction.commit();
    }

    private UiLifecycleHelper uiHelper;

    private Session.StatusCallback callback =
            new Session.StatusCallback() {
                @Override
                public void call(Session session,
                                 SessionState state, Exception exception) {
                    onSessionStateChange(session, state, exception);
                    Log.d(TAG,"Status callback");
                }
            };

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {

        // Only make changes if the activity is visible
        if (isResumed) {
            FragmentManager manager = getSupportFragmentManager();
            int backStackSize = manager.getBackStackEntryCount();
            // Clear the back stack
            for (int i = 0; i < backStackSize; i++) {
                manager.popBackStack();
            }
            if (state.isOpened()) {
                // If the session state is open:
                // Show the authenticated fragment
                showFragment(FRIENDLIST, false);
            } else if (state.isClosed()) {
                // If the session state is closed:
                // Show the login fragment
                showFragment(LOGIN, true);
            }
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Session session = Session.getActiveSession();

        if (session != null && session.isOpened()) {
            // if the session is already open,
            // try to show the fragment_friendlist fragment
            showFragment(FRIENDLIST, false);
        } else {
            Toast.makeText(this, "Session aint open",Toast.LENGTH_SHORT).show();
            // otherwise present the splash screen
            // and ask the person to login.
            showFragment(LOGIN, false);
        }
    }


    private void showFragment(int fragmentIndex, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == fragmentIndex) {
                transaction.show(fragments[i]);
            } else {
                transaction.hide(fragments[i]);
            }
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        isResumed = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
        isResumed = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, "Activity Result", Toast.LENGTH_SHORT).show();
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }



}

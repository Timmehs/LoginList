package com.tim.loginlist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.facebook.UiLifecycleHelper;

/**
 * Created by Tim Sandberg on 3/20/14.
 */

public class LoginFragment extends Fragment {
    private TextView userInfoTextView;
    private static final String TAG = "LoginFragment";
    private UiLifecycleHelper uiHelper;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container,savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_login,
                container, false);
        setRetainInstance(true);

        return view;
    }



}

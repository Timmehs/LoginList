package com.tim.loginlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.facebook.*;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tim Sandberg on 3/21/14.
 */
public class SelectionFragment extends Fragment {
    private static final String TAG = "SelectionFragment";
    private TextView welcome;
    private ProfilePictureView profilePictureView;
    private TextView userNameView;
    private static final int REAUTH_ACTIVITY_CODE = 100;
    private static List<Friend> friends;
    private static ActionListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView()");
        View v = inflater.inflate(R.layout.selection,
                container, false);
        ListView friendsList = (ListView) v.findViewById(R.id.friendsList);
        friends = new ArrayList<Friend>();
        adapter = new ActionListAdapter(getActivity(), R.id.friendsList, friends);
        friendsList.setAdapter(adapter);
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            makeMeRequest(session);
        }

        setRetainInstance(true);

        return v;
    }

    private void makeMeRequest(final Session session) {
        Request request = Request.newMeRequest(session,
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (session == Session.getActiveSession()) {
                            Log.d(TAG, "Response successful");
                            String request_string = user.getId() + "/friends";
                            Log.d(TAG, "Request string: " + request_string);
                            if (user != null) {
                                new Request(
                                        session,
                                        request_string,
                                        null,
                                        HttpMethod.GET,
                                        new Request.Callback() {
                                            public void onCompleted(Response response) {
                                                Log.d(TAG, response.toString());
                                                parseFriends(response);
                                            }

                                        }).executeAsync();
                            }
                            if (response.getError() != null) {
                                Log.e(TAG, "Response error " + response.getError());
                        }

                    }
                }
        });
        request.executeAsync();
    }

   private void parseFriends(Response response) {
       GraphObject graphObject = response.getGraphObject();
       if (graphObject != null) {
           JSONObject jsonObject = graphObject.getInnerJSONObject();
           try {
               JSONArray array = jsonObject.getJSONArray("data");
               for (int i = 0; i < array.length(); i++) {
                   JSONObject object = (JSONObject) array.get(i);
                   Friend friend = new Friend(
                           object.getString("name"),
                           object.getString("id"));
                   friends.add(friend);
                   Log.d(TAG, "Added " + friend.getName());

               }
               adapter.notifyDataSetChanged();
           } catch (JSONException e) {
               e.printStackTrace();
           }
       }

   }


    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (session != null && session.isOpened()) {
            // Get the user's data.
            makeMeRequest(session);
        }
    }

    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REAUTH_ACTIVITY_CODE) {
            uiHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        uiHelper.onSaveInstanceState(bundle);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    private class ActionListAdapter extends ArrayAdapter<Friend> {

        private List<Friend> listElements;

        public ActionListAdapter(Context context, int resourceId,
                                 List<Friend> listElements) {
            super(context, resourceId, listElements);
            this.listElements = listElements;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater =
                        (LayoutInflater) getActivity()
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.friends_list_item, null);
            }

            Friend listElement = listElements.get(position);

            if (listElement != null) {
                ProfilePictureView ppv = (ProfilePictureView) view.findViewById(R.id.friend_pic);
                TextView friendName = (TextView) view.findViewById(R.id.friend_name);
                TextView friendLocation = (TextView) view.findViewById(R.id.friend_location);

                if (ppv != null) {
                    ppv.setProfileId(listElement.getId());
                }
                if (friendName != null) {
                    friendName.setText(listElement.getName());
                }
                if (friendLocation != null) {

                }
            }
            return view;
        }
    }

}


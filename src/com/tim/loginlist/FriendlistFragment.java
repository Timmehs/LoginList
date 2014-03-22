package com.tim.loginlist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
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
public class FriendlistFragment extends Fragment {
    private static final String TAG = "FriendlistFragment";
    private TextView welcome;
    private ProfilePictureView profilePictureView;
    private TextView userNameView;
    private static final int REAUTH_ACTIVITY_CODE = 100;
    private static List<Friend> friends;
    private static FriendlistAdapter adapter;


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
        View v = inflater.inflate(R.layout.fragment_friendlist,
                container, false);
        ListView friendsList = (ListView) v.findViewById(R.id.friendsList);

        friends = new ArrayList<Friend>();
        adapter = new FriendlistAdapter(getActivity(), R.id.friendsList, friends);
        friendsList.setAdapter(adapter);
        friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Friend friend = (Friend) parent.getItemAtPosition(position);
                String facebookScheme = "fb://profile/" + friend.getId();
                Intent facebookIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookScheme));
                startActivity(facebookIntent);
            }
        });
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            makeMeRequest(session);
        }
        setRetainInstance(true);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logout_menu_item:
                LogoutConfirm lc = new LogoutConfirm();
                lc.show(getActivity().getSupportFragmentManager(), "Log out?");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /*
    * Creates Me Request and pulls friendslist as Graph Object if successful
    *
    */
    private void makeMeRequest(final Session session) {
        Request request = Request.newMeRequest(session,
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (session == Session.getActiveSession()) {
                            Log.d(TAG, "Response successful");
                            String request_string = user.getId() + "/friends";
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

    /* Parses data from facebook request response into friend objects, stores to
    *  List<Friends> friends.
    */
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

    private class FriendlistAdapter extends ArrayAdapter<Friend> {

        private List<Friend> listElements;

        public FriendlistAdapter(Context context, int resourceId,
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
                if (ppv != null) {
                    ppv.setProfileId(listElement.getId());
                }
                if (friendName != null) {
                    friendName.setText(listElement.getName());
                }

            }
            return view;
        }
    }


    //Dialog fragment for logout confirmation
    private class LogoutConfirm extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.logout_prompt)
                    .setPositiveButton(R.string.logout_confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Session sesh = Session.getActiveSession();
                            sesh.closeAndClearTokenInformation();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}


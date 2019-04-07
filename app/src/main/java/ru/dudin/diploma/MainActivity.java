package ru.dudin.diploma;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.simplefacebookclient.core.AuthModule;
import com.example.simplefacebookclient.core.db.DatabaseModule;
import com.example.simplefacebookclient.core.db.PostOrm;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.dudin.diploma.PostDialogFragments.CreatePostDialogFragment;
import ru.dudin.diploma.PostDialogFragments.DeletePostDialogFragment;
import ru.dudin.diploma.PostDialogFragments.UpdatePostDialogFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
                                                               PostsFragment.PostFragmentListener,
                                                               CreatePostDialogFragment.Listener,
                                                               DeletePostDialogFragment.Listener,
                                                               UpdatePostDialogFragment.Listener {

    private final AuthModule mAuthModule = AuthModule.getInstance();
    private final LoginManager mLoginManager = LoginManager.getInstance();
    private final CallbackManager mCallbackManager = CallbackManager.Factory.create();

    private static final String HAS_JUST_LOGGED_IN_KEY = "HAS_JUST_LOGGED_IN";
    private static final String IS_TOAST_POST_ABSENCE_SHOWN_KEY = "IS_TOAST_POST_ABSENCE_SHOWN";

    public static final String BROADCAST_LOAD_POSTS = "MainActivity.BROADCAST_LOAD_POSTS";
    public static final String LOAD_POSTS_DATA_KEY = "MainActivity.DATA";

    private boolean mIsAuthModuleInAuthenticatedState;
    private DatabaseModule mDatabaseModule;

    private int mChosenPostID;

    private boolean mHasJustLoggedIn;
    private boolean mIsToastPostsAbsenceShown;

    private final FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {

        @Override
        public void onSuccess(final LoginResult loginResult) {
            showCreatePostDialog();
        }

        @Override
        public void onCancel() {
            // do nothing
        }

        @Override
        public void onError(final FacebookException error) {
            // do nothing
        }
    };

    private final AuthModule.Listener mAuthListener = new AuthModule.Listener() {

        @SuppressLint("RestrictedApi")
        @Override
        public void onStateChanged(final AuthModule.State state) {
            switch (state) {
                case NOT_INITIALIZED:
                    mUsernameTextView.setVisibility(View.GONE);
                    mNotAuthenticatedTextView.setVisibility(View.VISIBLE);
                    mLogoutMenuItem.setVisible(false);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mFloatingActionButton.setVisibility(View.GONE);
                    mHeaderView.setBackgroundResource(R.drawable.side_nav_bar);
                    break;

                case NOT_AUTHENTICATED:
                    mUsernameTextView.setVisibility(View.GONE);
                    mNotAuthenticatedTextView.setVisibility(View.VISIBLE);
                    mLogoutMenuItem.setVisible(false);
                    mProgressBar.setVisibility(View.GONE);
                    mFloatingActionButton.setVisibility(View.GONE);
                    mHeaderView.setBackgroundResource(R.drawable.side_nav_bar);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.view_content_container, new LoginFragment())
                            .commit();
                    break;

                case AUTHENTICATED:
                    mUsernameTextView.setVisibility(View.VISIBLE);
                    mNotAuthenticatedTextView.setVisibility(View.GONE);
                    mLogoutMenuItem.setVisible(true);
                    mProgressBar.setVisibility(View.GONE);
                    mFloatingActionButton.setVisibility(View.VISIBLE);
                    mHeaderView.setBackgroundResource(R.drawable.side_nav_bar_auth);

                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.view_content_container, new PostsFragment())
                            .commit();
                    break;
            }
        }
    };

    BroadcastReceiver receiverDeletePost = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mChosenPostID = intent.getExtras().getInt(PostsFragment.CHOSEN_ID_POST_DATA_KEY);
            showDeletePostDialog();
        }
    };

    BroadcastReceiver receiverUpdatePost = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mChosenPostID = intent.getExtras().getInt(PostsFragment.CHOSEN_ID_POST_DATA_KEY);
            showUpdatePostDialog();
        }
    };

    private DrawerLayout mDrawerLayout;
    private TextView mUsernameTextView;
    private TextView mNotAuthenticatedTextView;
    private MenuItem mLogoutMenuItem;
    private ProgressBar mProgressBar;
    private ProfileTracker mProfileTracker;
    private FloatingActionButton mFloatingActionButton;
    private View mHeaderView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mHasJustLoggedIn = savedInstanceState.getBoolean(HAS_JUST_LOGGED_IN_KEY);
            mIsToastPostsAbsenceShown = savedInstanceState.getBoolean(IS_TOAST_POST_ABSENCE_SHOWN_KEY);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.view_drawer_layout);
        mProgressBar = (ProgressBar) findViewById(R.id.view_progress_bar);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.view_toolbar);
        setSupportActionBar(toolbar);

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.view_fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                getInstanceDatabaseModule();
                showCreatePostDialog();

//				if (!AccessToken.getCurrentAccessToken().getPermissions().contains("publish_actions")) {
//					final Set<String> mPermissions = new HashSet<>();
//					mPermissions.add("publish_actions");
//					mLoginManager.logInWithPublishPermissions(MainActivity.this, mPermissions);
//				} else {
//					showCreatePostDialog();
//				}
            }
        });

        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.view_navigation);
        navigationView.setNavigationItemSelectedListener(this);

        mHeaderView = navigationView.getHeaderView(0);
        mUsernameTextView = (TextView) mHeaderView.findViewById(R.id.view_username);
        mNotAuthenticatedTextView = (TextView) mHeaderView.findViewById(R.id.view_not_authenticated);
        mLogoutMenuItem = navigationView.getMenu().findItem(R.id.nav_logout);

        mLoginManager.registerCallback(mCallbackManager, mFacebookCallback);

        IntentFilter filterDeleteChosenPost = new IntentFilter(PostsFragment.BROADCAST_DELETE_POST);
        registerReceiver(receiverDeletePost, filterDeleteChosenPost);

        IntentFilter filterUpdateChosenPost = new IntentFilter(PostsFragment.BROADCAST_UPDATE_POST);
        registerReceiver(receiverUpdatePost, filterUpdateChosenPost);
    }


    @Override
    public boolean hasJustLoggedIn() {
        return mHasJustLoggedIn;
    }

    @Override
    public void loadPosts() {
        if(!mHasJustLoggedIn)
            mHasJustLoggedIn = true;

        getInstanceDatabaseModule();

        if(!mIsToastPostsAbsenceShown) {
            checkPostsAbsence();
            mIsToastPostsAbsenceShown = true;
        }

        new LoadPostsTask().execute();
    }

    private void checkPostsAbsence(){
        try {
            if(mDatabaseModule.getPosts().isEmpty()){
                Toast toast = Toast.makeText(this, getString(R.string.posts_are_absent), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getInstanceDatabaseModule(){
        if(mAuthModule.getState() == AuthModule.State.AUTHENTICATED) {
            mIsAuthModuleInAuthenticatedState = true;
            mDatabaseModule = DatabaseModule.getInstance();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        mAuthModule.addListener(mAuthListener);

        mProfileTracker = new ProfileTracker() {

            @Override
            protected void onCurrentProfileChanged(final Profile oldProfile, final Profile currentProfile) {
                onProfileUpdate();
            }
        };
        onProfileUpdate();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mAuthModule.removeListener(mAuthListener);
        mProfileTracker.stopTracking();

        if(mIsAuthModuleInAuthenticatedState){
            mDatabaseModule = null;
            mIsAuthModuleInAuthenticatedState = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (receiverDeletePost != null) {
            try {
                unregisterReceiver(receiverDeletePost);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (receiverUpdatePost != null) {
            try {
                unregisterReceiver(receiverUpdatePost);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        new PostsFragment().setDiscardPrefModPostsLoadedFirstTimeUponStopApp();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.nav_logout) {

            mHasJustLoggedIn = false;
            new PostsFragment().setDiscardPrefModPostsLoadedFirstTimeUponLogOut();

            mAuthModule.logout();
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @SuppressLint("SetTextI18n")
    private void onProfileUpdate() {
        final Profile profile = Profile.getCurrentProfile();
        if (profile != null) {
            mUsernameTextView.setText(profile.getFirstName() + " " + profile.getLastName());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.putBoolean(HAS_JUST_LOGGED_IN_KEY, mHasJustLoggedIn);
            savedInstanceState.putBoolean(IS_TOAST_POST_ABSENCE_SHOWN_KEY, mIsToastPostsAbsenceShown);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    //=======================================================
    private void showCreatePostDialog() {
        new CreatePostDialogFragment().show(getSupportFragmentManager(), "CreatePostDialogFragment");
    }

    @Override
    public void onCreatePost(final CreatePostDialogFragment fragment, final String text) {
            new CreatePostTask().execute(text, Calendar.getInstance().getTime());
    }

    private void showDeletePostDialog() {
        new DeletePostDialogFragment().show(getSupportFragmentManager(), "DeletePostDialogFragment");
    }

    @Override
    public void onDeletePost(DeletePostDialogFragment fragment) {
        new DeletePostTask().execute(mChosenPostID);
    }

    @Override
    public void onDeleteAllPosts(DeletePostDialogFragment fragment) {
        new DeleteAllPostsTask().execute();
    }

    private void showUpdatePostDialog() {
        new UpdatePostDialogFragment().show(getSupportFragmentManager(), "UpdatePostDialogFragment");
    }

    @Override
    public void onUpdatePost(UpdatePostDialogFragment fragment, String text) {
        new UpdatePostTask().execute(mChosenPostID, text, Calendar.getInstance().getTime());
    }

    //==========================================================================
    private class CreatePostTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Object... values) {
            try {
                final PostOrm post = new PostOrm();
                post.setMessage((String) values[0]);
                post.setCreatedTime((Date) values[1]);
                mDatabaseModule.createPost(post);

                return null;
            } catch (final SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
//           super.onPostExecute(aVoid);
            new LoadPostsTask().execute();
        }
    }


    private class UpdatePostTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(final Object... values) {

            try {
                final PostOrm post = mDatabaseModule.loadPost((Integer) values[0]);
                post.setMessage((String) values[1]);
                post.setCreatedTime((Date) values[2]);
                mDatabaseModule.updatePost(post);

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void aVoid) {
//            super.onPostExecute(aVoid);
            new LoadPostsTask().execute();
        }
    }

    private class DeletePostTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(final Integer... position) {

            try {
                mDatabaseModule.deletePost(position[0]);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void aVoid) {
//            super.onPostExecute(aVoid);
            new LoadPostsTask().execute();
        }
    }

    private class DeleteAllPostsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                mDatabaseModule.deleteAllPosts();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void aVoid) {
//            super.onPostExecute(aVoid);
            new LoadPostsTask().execute();
        }
    }

    //=================================================================================
    //TODO - наверно LoadPostTask
    private class LoadPostsTask extends AsyncTask<Void, Void, List<PostOrm>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<PostOrm> doInBackground(final Void... voids) {
            try {
                return mDatabaseModule.getPosts();
            } catch (final SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(final List<PostOrm> posts) {
            // super.onPostExecute(users);

            Bundle bundle = new Bundle();
            bundle.putSerializable(LOAD_POSTS_DATA_KEY, (Serializable) posts);
            Intent intent = new Intent(BROADCAST_LOAD_POSTS);
            intent.putExtras(bundle);
            sendBroadcast(intent);
        }
    }
}
package ru.dudin.diploma;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.simplefacebookclient.core.db.PostOrm;

import java.util.ArrayList;
import java.util.List;

public class PostsFragment extends Fragment {

    private final PreferencesModule mPreferencesModule = PreferencesModule.getInstance();

    private PostFragmentListener mPostFragmentListener;

    private float oldY = 0f;
    private final int MIN_FINGER_SCREEN_POINTS_MOVEMENT = 30;

    public static final String BROADCAST_DELETE_POST = "PostsFragment.BROADCAST_DELETE_POST";
    public static final String BROADCAST_UPDATE_POST = "PostsFragment.BROADCAST_UPDATE_POST";
    public static final String CHOSEN_ID_POST_DATA_KEY = "PostsFragment.CHOSEN_ID_POST";

    private PostListAdapter mPostListAdapter;
    private List<PostOrm> posts;

    BroadcastReceiver receiverLoadPosts = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            posts = (ArrayList<PostOrm>) bundle.getSerializable(MainActivity.LOAD_POSTS_DATA_KEY);
            mPostListAdapter.setData(posts);
        }
    };

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.post_fragment_recycler_view, container, false);
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        IntentFilter filterLoadPosts = new IntentFilter(MainActivity.BROADCAST_LOAD_POSTS);
        getActivity().registerReceiver(receiverLoadPosts, filterLoadPosts);

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.view_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mPostFragmentListener = (MainActivity) getActivity();

        mPostListAdapter = new PostListAdapter(getActivity());


        if(!mPostFragmentListener.hasJustLoggedIn() && mPreferencesModule.isPostsLoadedFirstTime()){
            Log.d("!@#", "Залогированы от предыдущего запуска и посты " +
                    "ранее загружались (false && true). Для загрузки постов нужно по экрану телефона " +
                    "провести пальцем сверху вниз.");

            showToastMoveFinger();
            recyclerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    float newY;
                    float deltaY;

                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            oldY = motionEvent.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            newY = motionEvent.getRawY();
                            deltaY = newY - oldY;

                            if(deltaY > MIN_FINGER_SCREEN_POINTS_MOVEMENT) {
                                loadDataPosts();
                                mPreferencesModule.setPostsLoadedFirstTime(false);
                                recyclerView.setOnTouchListener(null);
                            }
                            break;
                    }
                    return false;
                }
            });
        } else {
            Log.d("!@#", "Либо только что залогировались, либо это вращение, поэтому " +
                                   "посты загружаем сразу.");
            loadDataPosts();
        }

        //=============================================================
        mPostListAdapter.registerListener(new PostListAdapter.AdapterListener() {
            @Override
            public void invokeReceiverDeletePost(int mPostId) {
                getActivity().sendBroadcast(new Intent(BROADCAST_DELETE_POST)
                        .putExtra(CHOSEN_ID_POST_DATA_KEY, mPostId));
            }

            @Override
            public void invokeReceiverUpdatePost(int mPostId) {
                Intent intent = new Intent(BROADCAST_UPDATE_POST);
                intent.putExtra(CHOSEN_ID_POST_DATA_KEY, mPostId);
                getActivity().sendBroadcast(intent);
            }
        });

        recyclerView.setAdapter(mPostListAdapter);
    }

    private void showToastMoveFinger() {
        Toast toast = Toast.makeText(getContext(), getString(R.string.to_load_posts_move_finger), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void loadDataPosts() {
        mPostFragmentListener.loadPosts();
    }

    // Подготавливаем Preferences для загрузки постов после проведения по экрану пальцем сверху вниз.
    // Это ситуация, когда логирование уже было на предыдущей сессии.
    public void setDiscardPrefModPostsLoadedFirstTimeUponStopApp() {

        if(!mPreferencesModule.isPostsLoadedFirstTime())
            mPreferencesModule.setPostsLoadedFirstTime(true);

    }

    //  Подготавливаем Preferences для автоматической загрузки постов после логирования.
    public void setDiscardPrefModPostsLoadedFirstTimeUponLogOut() {
        if(mPreferencesModule.isPostsLoadedFirstTime())
            mPreferencesModule.setPostsLoadedFirstTime(false);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (receiverLoadPosts != null) {
            try {
                getActivity().unregisterReceiver(receiverLoadPosts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface PostFragmentListener {
        boolean hasJustLoggedIn();
        void loadPosts();
    }
}
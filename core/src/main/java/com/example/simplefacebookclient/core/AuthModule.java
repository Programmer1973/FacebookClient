package com.example.simplefacebookclient.core;

import android.util.Log;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.login.LoginManager;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.util.Set;

public class AuthModule {
	private static final String TAG = "AuthModule";

	private static AuthModule sInstance;

	private final Set<Listener> mListeners = Sets.newHashSet();

	private State mState = State.NOT_INITIALIZED;

	public static void createInstance() {
		sInstance = new AuthModule();
	}

	public static AuthModule getInstance() {
		return sInstance;
	}

	private void changeState(final State newState) {
		Preconditions.checkState(mState != newState, "New state is equal to old state: " + newState);

		Log.d(TAG, String.format("changeState: %s -> %s", mState.toString(), newState.toString()));
		mState = newState;
		for (final Listener listener : mListeners) {
			listener.onStateChanged(mState);
		}
	}

	private AuthModule() {
		// do nothing
	}

	public void init() {
		new AccessTokenTracker() {

			@Override
			protected void onCurrentAccessTokenChanged(final AccessToken oldAccessToken,
													   final AccessToken currentAccessToken) {
				final String msg = String.format(
						"oldAccessToken: %s; currentAccessToken: %s",
						oldAccessToken,
						currentAccessToken
				);
				if (currentAccessToken == null && mState == State.AUTHENTICATED) {
					changeState(State.NOT_AUTHENTICATED);
				} else if (currentAccessToken != null && mState == State.NOT_AUTHENTICATED) {
					changeState(State.AUTHENTICATED);
				}
			}
		};

		if (AccessToken.getCurrentAccessToken() != null) {
			changeState(State.AUTHENTICATED);
		} else {
			changeState(State.NOT_AUTHENTICATED);
		}
	}

	public void addListener(final Listener listener) {
		Preconditions.checkState(!mListeners.contains(listener));

		mListeners.add(listener);
		listener.onStateChanged(mState);
	}

	public void removeListener(final Listener listener) {
		Preconditions.checkState(mListeners.contains(listener));

		mListeners.remove(listener);
	}

	public void logout() {
		LoginManager.getInstance().logOut();
	}

	public State getState() {
		return mState;
	}

	public enum State {
		NOT_INITIALIZED, AUTHENTICATED, NOT_AUTHENTICATED
	}

	public interface Listener {

		void onStateChanged(State state);
	}
}

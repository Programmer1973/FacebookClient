package ru.dudin.diploma;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

@SuppressWarnings("ConstantConditions")
public class LoginFragment extends Fragment {

	private final CallbackManager mCallbackManager = CallbackManager.Factory.create();
	private final FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {

		@Override
		public void onSuccess(final LoginResult loginResult) {
			Log.d("!@#", "onSuccess");

		}

		@Override
		public void onCancel() {
			Log.d("!@#", "onCancel");

		}

		@Override
		public void onError(final FacebookException error) {
			Toast.makeText(getActivity(), R.string.login_error, Toast.LENGTH_SHORT).show();
			Log.e("!@#", "onError", error);
		}
	};

	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
							 @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_auth, container, false);
	}

	@Override
	public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final LoginButton loginButton = (LoginButton) getView().findViewById(R.id.view_facebook_login_button);
		loginButton.setFragment(this);
		loginButton.setReadPermissions("public_profile", "email", "user_friends", "user_posts");
		loginButton.registerCallback(mCallbackManager, mFacebookCallback);
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		mCallbackManager.onActivityResult(requestCode, resultCode, data);
	}
}
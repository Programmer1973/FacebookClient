package ru.dudin.diploma;

import android.app.Application;
import android.util.Log;

import com.example.simplefacebookclient.core.AuthModule;
import com.example.simplefacebookclient.core.db.DatabaseModule;
import com.example.simplefacebookclient.core.posts.PostsModule;
import com.facebook.FacebookSdk;

public class ClientApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		PreferencesModule.createInstance(this);
		AuthModule.createInstance();
		FacebookSdk.sdkInitialize(getApplicationContext(), new FacebookSdk.InitializeCallback() {

			@Override
			public void onInitialized() {
				AuthModule.getInstance().init();
			}
		});
		DatabaseModule.createInstance(this);
		PostsModule.createInstance();
	}
}

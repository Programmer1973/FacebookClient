package ru.dudin.diploma;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PreferencesModule {
	private static final String PREFERENCES_FILENAME = "preferences.dat";
	private static final String IS_POSTS_LOADED_FIRST_TIME_KEY = "IS_POSTS_LOADED_FIRST_TIME";

	private static PreferencesModule sInstance;

	private final SharedPreferences mSettings;


	public static void createInstance(final Context context) {
		sInstance = new PreferencesModule(context);
	}

	public static PreferencesModule getInstance() {
		return sInstance;
	}

	private PreferencesModule(final Context context) {
		mSettings = context.getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);
	}

	public boolean isPostsLoadedFirstTime() {
		return mSettings.getBoolean(IS_POSTS_LOADED_FIRST_TIME_KEY, false);
	}

	public void setPostsLoadedFirstTime(final boolean postsLoadedFirstTime) {
		mSettings.edit().putBoolean(IS_POSTS_LOADED_FIRST_TIME_KEY, postsLoadedFirstTime).apply();
	}
}
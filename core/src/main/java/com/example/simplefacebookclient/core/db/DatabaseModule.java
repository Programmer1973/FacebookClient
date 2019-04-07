package com.example.simplefacebookclient.core.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

public class DatabaseModule {
	private static DatabaseModule sInstance;

	private final DatabaseHelper mDatabaseHelper;


	public static void createInstance(final Context context) {
		sInstance = new DatabaseModule(context);
	}

	public static DatabaseModule getInstance() {
		return sInstance;
	}

	private DatabaseModule(final Context context) {
		mDatabaseHelper = new DatabaseHelper(context);
	}

	public List<PostOrm> getPosts() throws SQLException {
		return mDatabaseHelper.getDao(PostOrm.class).queryForAll();
	}

	public void deletePost(final Integer /*String*/ id) throws SQLException { // Изначально было String
		final Dao<PostOrm, Integer /*String*/> dao = mDatabaseHelper.getDao(PostOrm.class); // Изначально было String
		dao.deleteById(id);
	}

	public void createPost(final PostOrm post) throws SQLException {
		mDatabaseHelper.getDao(PostOrm.class).create(post);
	}

	public void updatePost(final PostOrm post) throws SQLException {
		mDatabaseHelper.getDao(PostOrm.class).update(post);
	}

	public PostOrm loadPost(final Integer /*String*/ id) throws SQLException { // Изначально было String
		final Dao<PostOrm, Integer /*String*/> dao = mDatabaseHelper.getDao(PostOrm.class); // Изначально было String
		return dao.queryForId(id);
	}

	public void deleteAllPosts() throws SQLException {
		mDatabaseHelper.getDao(PostOrm.class).deleteBuilder().delete();
	}


 	private static class DatabaseHelper extends OrmLiteSqliteOpenHelper {

		private static final String DATABASE_NAME = "app.db";
		private static final int DATABASE_VERSION = 2;

		public DatabaseHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(final SQLiteDatabase database, final ConnectionSource connectionSource) {
			createTables(connectionSource);
		}

		@Override
		public void onUpgrade(final SQLiteDatabase database, final ConnectionSource connectionSource,
				final int oldVersion, final int newVersion) {
			dropTables(connectionSource);
			createTables(connectionSource);
		}

		private void createTables(final ConnectionSource connectionSource) {
			try {
				TableUtils.createTable(connectionSource, PostOrm.class);
			} catch (final SQLException e) {
				throw new RuntimeException(e);
			}
		}

		private void dropTables(final ConnectionSource connectionSource) {
			try {
				TableUtils.dropTable(connectionSource, PostOrm.class, true);
			} catch (final SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
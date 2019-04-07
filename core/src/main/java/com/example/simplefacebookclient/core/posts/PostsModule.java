package com.example.simplefacebookclient.core.posts;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.simplefacebookclient.core.db.DatabaseModule;
import com.example.simplefacebookclient.core.db.PostOrm;
//import com.facebook.AccessToken;
//import com.facebook.GraphRequest;
//import com.facebook.GraphResponse;
//import com.facebook.HttpMethod;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class PostsModule {
	private static final String TAG = "PostsModule";
	private static final int BACKEND_WAIT_TIME = 3000;

	private static PostsModule sInstance;

	private final DatabaseModule mDatabaseModule = DatabaseModule.getInstance();

	private final Set<Listener> mListeners = Sets.newHashSet();
	private final Set<Post> mPosts = Sets.newTreeSet(new PostsComparator());

	private State mState = State.IDLE;

	public static void createInstance() {
		sInstance = new PostsModule();
	}

	public static PostsModule getInstance() {
		return sInstance;
	}

	private PostsModule() {
		changeState(State.LOADING);
		new InitialPostsLoader().execute();
	}

	private void changeState(final State newState) {
		Preconditions.checkState(mState != newState);

		Log.d(TAG, String.format("changeState: %s -> %s", mState.toString(), newState.toString()));
		mState = newState;
		for (final Listener listener : mListeners) {
			listener.onStateChanged(mState);
		}
	}

	public void create(final Post post) {
		Preconditions.checkState(mState == State.IDLE);

		changeState(State.CREATING);

		final Bundle params = new Bundle();
		params.putString("message", post.getMessage());
		new GraphRequest(
				AccessToken.getCurrentAccessToken(),
				"/me/feed",
				params,
				HttpMethod.POST,
				new GraphRequest.Callback() {
					public void onCompleted(final GraphResponse response) {
						new CreatePostResponseProcessor().execute(response, post);
					}
				}
		).executeAsync();
	}

	public void load() {
		load(false);
	}

	public void delete(final Post post) {
		Preconditions.checkState(mState == State.IDLE);

		changeState(State.DELETING);

		new GraphRequest(
				AccessToken.getCurrentAccessToken(),
				"/" + post.getId(),
				null,
				HttpMethod.DELETE,
				new GraphRequest.Callback() {
					public void onCompleted(final GraphResponse response) {
						new DeletePostResponseProcessor().execute(response, post);
					}
				}
		).executeAsync();
	}

	public void update(final Post post) {
		Preconditions.checkState(mState == State.IDLE);

		changeState(State.UPDATING);

		final Bundle params = new Bundle();
		params.putString("message", post.getMessage());
		new GraphRequest(
				AccessToken.getCurrentAccessToken(),
				"/" + post.getId(),
				params,
				HttpMethod.POST,
				new GraphRequest.Callback() {
					public void onCompleted(final GraphResponse response) {
						new UpdatePostResponseProcessor().execute(response, post);
					}
				}
		).executeAsync();
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

	public Set<Post> getPosts() {
		return ImmutableSet.copyOf(mPosts);
	}

	public Post findPostById(final String id) {
		for (final Post post : mPosts) {
			if (post.getId().equals(id)) {
				return post;
			}
		}

		return null;
	}

	private void load(final boolean shouldIgnoreCurrentState) {
		if (!shouldIgnoreCurrentState) {
			Preconditions.checkState(mState == State.IDLE);
		}

		changeState(State.LOADING);

		new GraphRequest(
				AccessToken.getCurrentAccessToken(),
				"/me/feed",
				null,
				HttpMethod.GET,
				new GraphRequest.Callback() {

					public void onCompleted(final GraphResponse response) {
						new LoadPostsResponseProcessor().execute(response);
					}
				}
		).executeAsync();
	}

	private class InitialPostsLoader extends AsyncTask<Void, Void, Set<Post>> {

		private volatile Throwable mError;

		@Override
		protected Set<Post> doInBackground(final Void... params) {
			/**
			 * Здесь мы получаем посты из ORMLite, и помещаем их в Set<Post> posts (промежуточная
			 * коллекция создаваемая в бэкграунде).
			 */
			try {
				final Set<Post> posts = Sets.newHashSet();
				for (final PostOrm postOrm : mDatabaseModule.getPosts()) {
					posts.add(createPost(postOrm));
				}
				return posts;
			} catch (final SQLException e) {
				mError = e;
				return null;
			}
		}

		@Override
		protected void onPostExecute(final Set<Post> posts) {
			/**
			 * Здесь мы эти полученные из ORMLite посты (из Set<Post> posts) копируем
			 * в Set<Post> mPosts.
			 */
			if (mError == null) {
				mPosts.addAll(posts);
				for (final Listener listener : mListeners) {
					listener.onPostsLoadedSuccessfully(mPosts);
				}
			} else {
				for (final Listener listener : mListeners) {
					listener.onPostsLoadFailed(mError);
				}
			}

			changeState(State.IDLE);
		}

		private Post createPost(final PostOrm postOrm) {
			final Post post = new Post();
//			post.setId(postOrm.getId());
			post.setMessage(postOrm.getMessage());
			post.setCreatedTime(postOrm.getCreatedTime());
			return post;
		}
	}

	private PostOrm createPostOrm(final Post post) {
		final PostOrm postOrm = new PostOrm();
//		postOrm.setId(post.getId());
		postOrm.setMessage(post.getMessage());
		postOrm.setCreatedTime(post.getCreatedTime());
		return postOrm;
	}

	private class UpdatePostResponseProcessor extends AsyncTask<Object, Void, Post> {

		private volatile Throwable mError;

		@Override
		protected Post doInBackground(final Object... params) {
			final GraphResponse response = (GraphResponse) params[0];
			final Post post = (Post) params[1];

			try {
				/**
				 * Если нет ошибки из Facebook-а.
				 * Здесь мы создаём "ORMLite пост" и добавляем(update-им) его в ORMLite.
				 * Разве id поста тут не иcпользуется?
				 */
				if (response.getError() == null) {
//					mDatabaseModule.updatePost(createPostOrm(post));
					return post;
				} else {
					/**
					 * Иначе, по id поста загружаем "старый" ORMLite пост.
					 * Из него вынимаем сообщение и устанавливаем его в пост.
					 */
//					final PostOrm oldPostOrm = mDatabaseModule.loadPost(post.getId());
//					post.setMessage(oldPostOrm.getMessage());
					mError = new Exception(response.getError().getErrorMessage());
					return post;
				}
			} catch (final Throwable t) {
				mError = t;
				return post;
			}
		}

		@Override
		protected void onPostExecute(final Post post) {
			if (mError == null) {
				for (final Listener listener : mListeners) {
					listener.onPostUpdatedSuccessfully(post);
				}
			} else {
				for (final Listener listener : mListeners) {
					listener.onPostUpdatingFailed(post, mError);
				}
			}

			changeState(State.IDLE);
		}
	}

	private class DeletePostResponseProcessor extends AsyncTask<Object, Void, Post> {

		private volatile Throwable mError;

		@Override
		protected Post doInBackground(final Object... params) {

			final GraphResponse response = (GraphResponse) params[0];
			final Post post = (Post) params[1];
			/**
			 * Здесь мы из поста получаем id и удаляем пост с этим id из ORMLite.
			 */
			try {
				if (response.getError() == null) {
//					mDatabaseModule.deletePost(post.getId());
					return post;
				} else {
					mError = new Exception(response.getError().getErrorMessage());
					return post;
				}
			} catch (final Throwable t) {
				mError = t;
				return post;
			}
		}

		@Override
		protected void onPostExecute(final Post post) {
			/**
			 * Здесь удаляем пост из Set<Post> mPosts.
			 */
			if (mError == null) {
				mPosts.remove(post);
				for (final Listener listener : mListeners) {
					listener.onPostDeletedSuccessfully(post);
				}
			} else {
				for (final Listener listener : mListeners) {
					listener.onPostDeletingFailed(post, mError);
				}
			}

			changeState(State.IDLE);
		}
	}

	private class CreatePostResponseProcessor extends AsyncTask<Object, Void, Post> {


		private volatile Throwable mError;

		@Override
		protected Post doInBackground(final Object... params) {
			final GraphResponse response = (GraphResponse) params[0];
			final Post post = (Post) params[1];

			try {
				/**
				 * Здесь мы получаем id поста из платфомы Facebook-а, присваиваем этот id нашему посту и
				 * этот пост вносим в ORMLite.
				 */
				final String id;
				if (response.getError() == null) {
					id = response.getJSONObject().getString("id");
				} else {
					mError = new Exception(response.getError().getErrorMessage());
					return null;
				}

				post.setId(id);
//				mDatabaseModule.createPost(createPostOrm(post));

				return post;
			} catch (final Throwable t) {
				mError = t;
				return post;
			}
		}


		@Override
		protected void onPostExecute(final Post post) {
			/**
			 * Здесь добавляем этот пост в Set<Post> mPosts.
			 */
			if (mError == null) {
				mPosts.add(post);
				for (final Listener listener : mListeners) {
					listener.onPostCreatedSuccessfully(post);
				}
				new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
					@Override
					public void run() {
						load(true);
					}
				}, BACKEND_WAIT_TIME);
			} else {
				for (final Listener listener : mListeners) {
					listener.onPostCreationFailed(post, mError);
				}
				changeState(State.IDLE);
			}
		}
	}

	private class LoadPostsResponseProcessor extends AsyncTask<GraphResponse, Void, Set<Post>> {

		private volatile Throwable mError;

		@Override
		protected Set<Post> doInBackground(final GraphResponse... params) {
			final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

			try {
				/**
				 * Здесь мы из Facebook-а получаем JSON-массив постов по "data-е".
				 */
				final GraphResponse response = params[0];
				final JSONArray postsJsonArray;
				if (response.getError() == null) {
					postsJsonArray = response.getJSONObject().getJSONArray("data");
				} else {
					mError = new Exception(response.getError().getErrorMessage());
					return null;
				}

				/**
				 * Тут мы удаляем все посты из ORMLite, т.е. очищаем его. В Json-массиве мы узнаём
				 * количество постов. Создаём посты из этого Json-массива. И помещаем их в ORMLite
				 * (предварительно очищенный).
				 */
				final Set<Post> posts = Sets.newHashSet();
				mDatabaseModule.deleteAllPosts();
				for (int i = 0; i < postsJsonArray.length(); i++) {
					final JSONObject postJson = postsJsonArray.getJSONObject(i);

					final Post post = new Post();
					post.setId(postJson.getString("id"));
					post.setMessage(postJson.getString("message"));
					post.setCreatedTime(dateFormat.parse(postJson.getString("created_time")));
					posts.add(post);

//					mDatabaseModule.createPost(createPostOrm(post));
				}

				return posts;
			} catch (final Throwable t) {
				mError = t;
				return null;
			}
		}

		@Override
		protected void onPostExecute(final Set<Post> posts) {
			/**
			 * Тут мы очищаем Set<Post> mPosts и помещаем в него посты posts.
			 */
			if (mError == null) {
				mPosts.clear();
				mPosts.addAll(posts);
				for (final Listener listener : mListeners) {
					listener.onPostsLoadedSuccessfully(mPosts);
				}
			} else {
				for (final Listener listener : mListeners) {
					listener.onPostsLoadFailed(mError);
				}
			}

			changeState(State.IDLE);
		}
	}

	private static class PostsComparator implements Comparator<Post> {

		@Override
		public int compare(final Post lhs, final Post rhs) {
			final Date rhsCreatedTime = rhs.getCreatedTime();
			final Date lhsCreatedTime = lhs.getCreatedTime();

			if (rhsCreatedTime == null && lhsCreatedTime == null) {
				return 0;
			}

			if (rhsCreatedTime == null) {
				return 1;
			}

			if (lhsCreatedTime == null) {
				return -1;
			}

			return (int) Math.signum(rhsCreatedTime.getTime() - lhsCreatedTime.getTime());
		}
	}

	public enum State {
		IDLE, LOADING, CREATING, DELETING, UPDATING
	}

	public interface Listener {

		void onStateChanged(State state);

		void onPostCreatedSuccessfully(Post post);

		void onPostCreationFailed(Post post, Throwable t);

		void onPostsLoadedSuccessfully(Set<Post> posts);

		void onPostsLoadFailed(Throwable t);

		void onPostDeletedSuccessfully(Post post);

		void onPostDeletingFailed(Post post, Throwable t);

		void onPostUpdatedSuccessfully(Post post);

		void onPostUpdatingFailed(Post post, Throwable t);
	}
}

package ru.dudin.diploma;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.simplefacebookclient.core.db.PostOrm;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {

    private final LayoutInflater mInflater;
    private List<PostOrm> mPosts;
    private AdapterListener mAdapterListener;
    private int mPostId;

    public void registerListener(AdapterListener mAdapterListener){
        this.mAdapterListener = mAdapterListener;
    }

    public PostListAdapter(final Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = mInflater.inflate(R.layout.view_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.textViewPostMessage.setText(mPosts.get(position).getMessage());
        holder.textViewPostCreatedTime.setText(new SimpleDateFormat("dd:MM:yyyy HH:mm", Locale.US).format(mPosts.get(position).getCreatedTime()));
    }

    @Override
    public int getItemCount() {
        return mPosts == null ? 0 : mPosts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewPostMessage;
        public TextView textViewPostCreatedTime;
        public TextView textViewXDeletePost;

        public ViewHolder(final View itemView) {
            super(itemView);

            textViewPostMessage = (TextView) itemView.findViewById(R.id.text_view_post_message);
            textViewPostCreatedTime = (TextView) itemView.findViewById(R.id.text_view_post_created_time);
            textViewXDeletePost = (TextView) itemView.findViewById(R.id.text_view_x_delete_post);

                textViewXDeletePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        invocationReceiverDeletePost();
                    }
                });

                textViewPostMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      invocationReceiverUpdatePost();
                    }
                });

                textViewPostCreatedTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      invocationReceiverUpdatePost();
                    }
                });
        }

        private void invocationReceiverDeletePost() {
            adapterPosition();
            mAdapterListener.invokeReceiverDeletePost(mPostId);
        }

        private void invocationReceiverUpdatePost() {
            adapterPosition();
            mAdapterListener.invokeReceiverUpdatePost(mPostId);
        }

        private void adapterPosition() {
            mPostId = mPosts.get(getAdapterPosition()).getId();
        }
    }

    public void setData (final List<PostOrm> posts) {
        mPosts = posts;
        notifyDataSetChanged();
    }

    interface AdapterListener {
        void invokeReceiverDeletePost(int mPostId);
        void invokeReceiverUpdatePost(int mPostId);
    }
}
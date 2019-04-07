package ru.dudin.diploma.PostDialogFragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import ru.dudin.diploma.R;

public class DeletePostDialogFragment extends DialogFragment {

    private Listener mListener;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListener = (Listener) getActivity();
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder
                .setTitle(R.string.delete_post)
                .setIcon(R.mipmap.ic_launcher_round)
                .setPositiveButton(getString(R.string.delete_all_posts), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onDeleteAllPosts(DeletePostDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {

                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        mListener.onDeletePost(DeletePostDialogFragment.this);
                    }
                })
                .setNeutralButton(R.string.cancel, null);

        return builder.create();
    }

    public interface Listener {
        void onDeletePost(DeletePostDialogFragment fragment);
        void onDeleteAllPosts(DeletePostDialogFragment fragment);
    }
}
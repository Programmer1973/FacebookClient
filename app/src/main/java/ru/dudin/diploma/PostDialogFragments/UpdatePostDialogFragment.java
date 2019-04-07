package ru.dudin.diploma.PostDialogFragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;

import ru.dudin.diploma.R;

public class UpdatePostDialogFragment extends DialogFragment {

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
                .setTitle(R.string.update_post /*Html.fromHtml("<font color='#FF7F27'>R.string.update_post</font>"*/)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {

                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        TextInputEditText postTextInputEditText = (TextInputEditText) getDialog().findViewById(R.id.view_post);
                        final String text = postTextInputEditText.getText().toString();
                        if (TextUtils.isEmpty(text)) {
                            postTextInputEditText.setError(getString(R.string.error_no_text));
                        } else {
                            mListener.onUpdatePost(UpdatePostDialogFragment.this, text);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setView(R.layout.fragment_post_dialog);
        } else {
            builder.setView(LayoutInflater.from(getActivity()).inflate(R.layout.fragment_post_dialog, null, false));
        }

        return builder.create();
    }

    public interface Listener {
        void onUpdatePost(UpdatePostDialogFragment fragment, String text);
    }
}
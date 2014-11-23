package com.capstone.potlatch.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;


/**
 * Activities that contain this fragment must implement the
 * {@link com.capstone.potlatch.dialogs.DialogConfirm.OnDialogConfirmListener} interface
 * to handle interaction events.
 */
public class DialogConfirm extends DialogFragment {
    private static final String ARG_TEXT = "ARG_TEXT";

    private String text;

    private OnDialogConfirmListener mListener;

    public static DialogConfirm open(FragmentManager fragmentManager, String tag, String text) {
        DialogConfirm dialogConfirm = new DialogConfirm();

        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        dialogConfirm.setArguments(args);

        dialogConfirm.show(fragmentManager, tag);
        return dialogConfirm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            text = getArguments().getString(ARG_TEXT);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(text)
               .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       mListener.onConfirmationFinish(getTag(), true);
                       mListener = null; // To avoid calling it again in onDismiss
                   }
               })
               .setNegativeButton(android.R.string.no, null);


        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDialogConfirmListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDialogConfirmListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null) {
            mListener.onConfirmationFinish(getTag(), false);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnDialogConfirmListener {
        public void onConfirmationFinish(String tag, boolean confirmed);
    }

}

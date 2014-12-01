package com.capstone.giveout.dialogs;

import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.capstone.giveout.R;

public class DialogLoading extends BaseRetainedDialog {
    private static final String ARG_TITLE = "ARG_TITLE";
    private static final String TAG = "DialogLoading";


    public static DialogLoading open(FragmentManager fragmentManager) {
        DialogLoading dialog = null;
        try {
            dialog = (DialogLoading) fragmentManager.findFragmentByTag(TAG);
        } catch (ClassCastException ignored) {}

        if (dialog == null) {
            dialog = new DialogLoading();
            dialog.show(fragmentManager, TAG);
        }
        return dialog;
    }

    public static void close(FragmentManager fragmentManager) {
        DialogLoading dialog = null;
        try {
            dialog = (DialogLoading) fragmentManager.findFragmentByTag(TAG);
        } catch (ClassCastException ignored) {}

        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_loading, container, false);
    }
}

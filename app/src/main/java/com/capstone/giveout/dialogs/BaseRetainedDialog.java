package com.capstone.giveout.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

/**
 * This class allows derived fragment to save data and get it later.
 * IMPORTANT: As this is a retained fragment, be sure the derived fragment nullifies all UI and
 * related listeners in on {@link android.app.DialogFragment#onDestroyView()}
 */
public class BaseRetainedDialog extends DialogFragment {
    private Object data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();

        // Work around bug: http://code.google.com/p/android/issues/detail?id=17423
        if ((dialog != null) && getRetainInstance())
            dialog.setDismissMessage(null);
        super.onDestroyView();
    }

    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

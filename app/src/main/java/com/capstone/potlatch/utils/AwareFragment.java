package com.capstone.potlatch.utils;

/**
 * Created by alvaro on 20/11/14.
 *
 * Interface that all fragments in a ViewPager fragment should implement
 * if the want to be notified when some events occurs in the Activity they belongs
 */
public interface AwareFragment {
    interface OnViewPagerFragmentSelected {
        public void onSelected();
    }
    interface OnUserLogin {
        public void onLoginSuccess();
        public void onLoginCanceled();
    }
    interface OnDialogConfirmation {
        public void onConfirmation(String tag, boolean confirmed);
    }
}

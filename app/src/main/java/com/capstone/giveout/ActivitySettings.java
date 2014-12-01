package com.capstone.giveout;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.capstone.giveout.base.Config;
import com.capstone.giveout.utils.SyncManager;

public class ActivitySettings extends PreferenceActivity {
    public static final String KEY_SYNC_FREQ = "sync_freq";
    public static final String KEY_NO_INAPPROPRIATE_GIFT = "no_inappropriate_gifts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(KEY_SYNC_FREQ));
        findPreference(KEY_NO_INAPPROPRIATE_GIFT).setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index]
                                               : null);

            }

            if (KEY_SYNC_FREQ.equals(preference.getKey())) {
                int syncMinutes = Integer.parseInt(((ListPreference) preference).getValue());
                SyncManager.setAlarm(preference.getContext(), SyncManager.UPDATE_DATA_ACTION, syncMinutes * 60 * 1000);
            } else if (KEY_NO_INAPPROPRIATE_GIFT.equals(preference.getKey())) {
                Config.noInappropriateGifts = (boolean) value;
                SyncManager.sendBroadcast(preference.getContext(), SyncManager.RELOAD_DATA_ACTION);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
}

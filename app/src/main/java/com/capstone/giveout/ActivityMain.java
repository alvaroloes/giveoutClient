package com.capstone.giveout;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.capstone.giveout.base.BaseActivity;
import com.capstone.giveout.dialogs.BaseRetainedDialog;
import com.capstone.giveout.dialogs.DialogConfirm;
import com.capstone.giveout.dialogs.DialogLogin;
import com.capstone.giveout.utils.AwareFragment;
import com.capstone.giveout.utils.SyncManager;

import java.util.ArrayList;
import java.util.List;


public class ActivityMain extends BaseActivity implements DialogLogin.OnLoginListener, DialogConfirm.OnDialogConfirmListener {
    private SectionsAdapter adapter;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle args;
        List<SectionData> sectionsData = new ArrayList<SectionData>();

        // Create the sections data list
        sectionsData.add(new SectionData(getString(R.string.section_gifts), SectionGifts.class));
        sectionsData.add(new SectionData(getString(R.string.section_gifts_chains),SectionGiftChains.class));
        sectionsData.add(new SectionData(getString(R.string.section_top_givers),SectionTopGivers.class));
        args = new Bundle();
        args.putBoolean(SectionGifts.ARG_FOR_CURRENT_USER, true);
        sectionsData.add(new SectionData(getString(R.string.section_my_gifts),SectionGifts.class, args));

        adapter = new SectionsAdapter(getFragmentManager(), sectionsData);

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setOnPageChangeListener(new OnSectionSelectedListener());
        tabs.setViewPager(pager);
    }

    @Override
    protected void onDestroy() {
        SyncManager.cancelAlarm(this, SyncManager.UPDATE_DATA_ACTION);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_options, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent i = new Intent(this, ActivitySettings.class);
                startActivity(i);
                return true;
            case R.id.action_new_gift:
                i = new Intent(this, ActivityCreateUpdateGift.class);
                startActivity(i);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoginFinish(BaseRetainedDialog dialogFragment, String tag, boolean success) {
        Fragment f = adapter.getActiveFragments().get(pager.getCurrentItem());
        if (f instanceof AwareFragment.OnUserLogin) {
            ((AwareFragment.OnUserLogin) f).onLogin(dialogFragment, tag, success);
        }
    }

    @Override
    public void onConfirmationFinish(BaseRetainedDialog dialogFragment, String tag, boolean confirmed) {
        Fragment f = adapter.getActiveFragments().get(pager.getCurrentItem());
        if (f != null && f instanceof AwareFragment.OnDialogConfirmation) {
            ((AwareFragment.OnDialogConfirmation) f).onConfirmation(dialogFragment, tag, confirmed);
        }
    }

    class OnSectionSelectedListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            Fragment f = adapter.getActiveFragments().get(position);
            if (f != null && f instanceof AwareFragment.OnViewPagerFragmentSelected) {
                ((AwareFragment.OnViewPagerFragmentSelected) f).onSelected();
            }
        }
    }

    class SectionsAdapter extends FragmentPagerAdapter {
        private SparseArray<Fragment> activeFragments = new SparseArray<Fragment>();
        private final List<SectionData> sectionsData = new ArrayList<SectionData>();

        public SectionsAdapter(FragmentManager fm, List<SectionData> sectionsData) {
            super(fm);
            this.sectionsData.addAll(sectionsData);
        }

        @Override
        public CharSequence getPageTitle(int i) {
            return sectionsData.get(i).title;
        }

        @Override
        public Fragment getItem(int i) {
            // In a FragmentPagerAdapter, a very good practice is to create the fragment instances
            // HERE. Doing so, new fragment instances are not recreated if the activity has it already
            // attached (Ending up with duplicated fragment instances).
            // What is more, doing this is a MUST in case any fragment is retained to avoid memory leaks.
            try {
                SectionData sectionData = sectionsData.get(i);
                Fragment f = sectionData.sectionClass.getConstructor().newInstance();
                f.setArguments(sectionData.arguments);
                return f;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int getCount() {
            return sectionsData.size();
        }

        // With this two methods we keep track of the fragments that are alive.
        // This allows us to send them events such us when the user is logged in, etc.

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment f = (Fragment) super.instantiateItem(container, position);
            activeFragments.put(position, f);
            return f;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            activeFragments.remove(position);
        }

        public SparseArray<Fragment> getActiveFragments() {
            return activeFragments;
        }
    }

    static class SectionData {
        String title;
        Class<? extends Fragment> sectionClass;
        Bundle arguments;

        SectionData(String title, Class<? extends Fragment> sectionClass, Bundle arguments) {
            this.title = title;
            this.sectionClass = sectionClass;
            this.arguments = arguments;
        }

        SectionData(String title, Class<? extends Fragment> sectionClass) {
            this(title, sectionClass, null);
        }
    }
}

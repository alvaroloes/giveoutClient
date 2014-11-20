package com.capstone.potlatch;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.capstone.potlatch.models.User;
import com.capstone.potlatch.utils.AwareFragment;

import java.util.ArrayList;
import java.util.List;


public class ActivityMain extends Activity implements DialogLogin.OnLoginListener {
    private List<TitleSectionPair> sections = new ArrayList<TitleSectionPair>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sections.add(new TitleSectionPair(getString(R.string.section_gifts), SectionGifts.newInstance(false)));
        sections.add(new TitleSectionPair(getString(R.string.section_gifts), SectionGifts.newInstance(false)));
        sections.add(new TitleSectionPair(getString(R.string.section_gifts), SectionGifts.newInstance(false)));
        sections.add(new TitleSectionPair(getString(R.string.section_my_gifts), SectionGifts.newInstance(true)));

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new SectionsAdapter(getFragmentManager()));

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setOnPageChangeListener(new OnSectionSelectedListener());
        tabs.setViewPager(pager);
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
        switch(id) {
            case R.id.action_settings:
                Intent i = new Intent(this, ActivitySettings.class);
                startActivity(i);
                return true;
            case R.id.action_new_gift:
                i = new Intent(this, ActivityCreateGift.class);
                startActivity(i);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoginSuccess(User user) {
        for (TitleSectionPair pair : sections) {
            if (! pair.section.isVisible()) {
                continue;
            }
            if (pair.section instanceof AwareFragment.OnUserLogin) {
                ((AwareFragment.OnUserLogin) pair.section).onLoginSuccess();
            }
        }
    }

    @Override
    public void onLoginCanceled() {
        for (TitleSectionPair pair : sections) {
            if (! pair.section.isVisible()) {
                continue;
            }
            if (pair.section instanceof AwareFragment.OnUserLogin) {
                ((AwareFragment.OnUserLogin) pair.section).onLoginCanceled();
            }
        }
    }

    class OnSectionSelectedListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            TitleSectionPair f = sections.get(position);
            if (f.section instanceof AwareFragment.OnViewPagerFragmentSelected) {
                ((AwareFragment.OnViewPagerFragmentSelected) f.section).onSelected();
            }
        }
    }

    class SectionsAdapter extends FragmentPagerAdapter {

        public SectionsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int i) {
            return sections.get(i).title;
        }

        @Override
        public Fragment getItem(int i) {
            return sections.get(i).section;
        }

        @Override
        public int getCount() {
            return sections.size();
        }
    }

}

class TitleSectionPair {
    String title;
    Fragment section;

    TitleSectionPair(String title, Fragment section) {
        this.title = title;
        this.section = section;
    }
}
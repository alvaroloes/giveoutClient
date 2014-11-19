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

import java.util.ArrayList;
import java.util.List;


public class ActivityMain extends Activity {
    private List<TitleSectionPair> sections = new ArrayList<TitleSectionPair>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sections.add(new TitleSectionPair(getString(R.string.section_gifts), SectionGifts.newInstance()));
        sections.add(new TitleSectionPair(getString(R.string.section_gifts), SectionGifts.newInstance()));
        sections.add(new TitleSectionPair(getString(R.string.section_gifts), SectionGifts.newInstance()));
        sections.add(new TitleSectionPair(getString(R.string.section_gifts), SectionGifts.newInstance()));

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new SectionsAdapter(getFragmentManager()));

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
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
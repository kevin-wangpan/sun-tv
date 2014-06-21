package com.jiaoyang.tv;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;

import com.jiaoyang.tv.detail.DetailFragment;
import com.jiaoyang.video.tv.R;

public class DetailActivity extends JyBaseActivity {
    private DetailFragment mDetailFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = null;
        if (getIntent() != null) {
            arguments = getIntent().getExtras();
        }


        mDetailFragment = (DetailFragment) Fragment.instantiate(this, DetailFragment.class.getName(), arguments);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.content_frame, mDetailFragment);
        t.commit();
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        
        if(mDetailFragment != null && mDetailFragment.isLoading()) {
            return true;
        }
        
        return super.dispatchKeyEvent(event);
    }
}

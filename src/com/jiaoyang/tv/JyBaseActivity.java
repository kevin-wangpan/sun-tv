package com.jiaoyang.tv;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import butterknife.ButterKnife;
import cn.com.iresearch.mapptracker.IRMonitor;

import com.jiaoyang.tv.content.RootRelativeLayout;
import com.jiaoyang.tv.util.Logger;
import com.jiaoyang.video.tv.R;

/**
 * TV中使用的Activity，主要完成以下工作
 * 3. 切换主Fragment
 *
 */
public class JyBaseActivity extends FragmentActivity {
    private static final Logger LOG = Logger.getLogger(JyBaseActivity.class);
    private PopupWindow popupWindow;
    private TransitionDrawable mBg;
    private boolean mReverseBg;
    private boolean mHasMenu = true;

    RootRelativeLayout mRootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_frame);
        ButterKnife.inject(this);
        mRootView = (RootRelativeLayout) findViewById(R.id.tv_activity_root);
        setupBackground();
    }

    private void setupBackground() {
        getWindow().setBackgroundDrawableResource(R.drawable.activity_background);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.content_frame);
        fragment = Fragment.instantiate(this, fragment.getClass().getName(), intent.getExtras());
        fm.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            } else {
                finish();
            }
            break;

        default:
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    protected void setContentFragment(Class<? extends JyBaseFragment> fragmentClass) {
        Bundle arguments = null;
        if (getIntent() != null) {
            arguments = getIntent().getExtras();
        }
        setContentFragment(fragmentClass, arguments);
    }

    protected void setContentFragment(Class<? extends JyBaseFragment> fragmentClass, Bundle arguments) {
        LOG.debug("set content fragment. class={}", fragmentClass.getName());

        Fragment fragment = Fragment.instantiate(this, fragmentClass.getName(), arguments);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.content_frame, fragment);
        t.commit();
    }

    protected void openActivity(Class<? extends JyBaseActivity> activityClass, Bundle arguments) {
        LOG.debug("open activity. class={}", activityClass.getName());

        Intent intent = new Intent(this, activityClass);
        if (arguments != null) {
            intent.putExtras(arguments);
        }
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
                return true;
            }
            View popupView = getLayoutInflater().inflate(R.layout.popup_menu, null);

            final View toBeFocused = setupMenu(popupView);
            if (!mHasMenu) {
                return super.onKeyDown(keyCode, event);
            }
            popupWindow = new PopupWindow(popupView, LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT, true);
            popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.black_bg));

            popupWindow.getContentView().setFocusableInTouchMode(true);
            popupWindow.setAnimationStyle(R.style.popup_menu_anim);
            popupWindow.showAtLocation(
                    getWindow().getDecorView().findViewById(android.R.id.content),
                    Gravity.BOTTOM, 0, 0);
            if (toBeFocused != null) {
                toBeFocused.requestFocus();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            for(JyBaseFragment fragment : getAvailableFragment()){
                if(fragment.onBackKeyDown()){
                    return true;
                }
            }
            return super.onKeyDown(keyCode, event);
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }
    
    /**
     * 获取可见JyBaseFragment
     */
    private List<JyBaseFragment> getAvailableFragment(){
        List<JyBaseFragment> res = new ArrayList<JyBaseFragment>();
        List<Fragment> list = getSupportFragmentManager().getFragments();
        for(Fragment fragment : list){
            if(fragment instanceof JyBaseFragment && fragment.isVisible()){
                res.add((JyBaseFragment)fragment);
            }
        }
        return res;
    }

    private View setupMenu(View root) {
        View wannafocused = null;
        TvMenu menu = new TvMenu();
        menu.contributor = this;
        onCreateTvMenu(menu);
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof JyBaseFragment && f.isVisible()) {
                menu.contributor = f;
                ((JyBaseFragment) f).onCreateTvMenu(menu);
            }
        }
        //menu菜单创建完成以后，不要保留任何对menu来源的引用
        menu.contributor = null;
        if (menu.getCount() <= 0) {
            mHasMenu = false;
            return null;
        }

        LinearLayout leftContainer = (LinearLayout) root.findViewById(R.id.left_menus);
        LinearLayout rightContainer = (LinearLayout) root.findViewById(R.id.right_menus);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        param.rightMargin = 40;
        View firstRightMenuItem = null;
        View lastRightMenuItem = null;
        View firstLeftMenuItem = null;
        View lastLeftMenuItem = null;
        for (TvMenuItem item : menu.items) {
            View menuItem = getLayoutInflater().inflate(R.layout.menu_item, null);
            ImageView iv = (ImageView) menuItem.findViewById(R.id.icon);
            TextView tv = (TextView) menuItem.findViewById(R.id.title);
            iv.setImageDrawable(item.icon);
            tv.setText(item.title);
            if (item.allignRight) {
                rightContainer.addView(menuItem, param);
                if (firstRightMenuItem == null) {
                    firstRightMenuItem =  menuItem;
                }
                lastRightMenuItem = menuItem;
            } else {
                leftContainer.addView(menuItem, param);
                if (firstLeftMenuItem == null) {
                    firstLeftMenuItem =  menuItem;
                }
                lastLeftMenuItem = menuItem;
            }
            if (wannafocused == null && item.focusable) {
                wannafocused = menuItem;
            }else if(item.focused){
                wannafocused = menuItem;
            }
            menuItem.setId(item.id);
            menuItem.setTag(item.contributor);
            menuItem.setOnKeyListener(mKeyListener);
            menuItem.setOnClickListener(mMenuOnClickLisener);
            menuItem.setFocusable(item.focusable);
        }
        View firstMenuItem = firstLeftMenuItem != null ?
                firstLeftMenuItem : firstRightMenuItem;
        View lastMenuItem = lastRightMenuItem != null ?
                lastRightMenuItem : lastLeftMenuItem;
        if (firstMenuItem != null && lastMenuItem != null) {
            firstMenuItem.setNextFocusLeftId(lastMenuItem.getId());
            lastMenuItem.setNextFocusRightId(firstMenuItem.getId());
        }
        return wannafocused;
    }
    
    private OnClickListener mMenuOnClickLisener = new OnClickListener(){

        @Override
        public void onClick(View v) {
            onMenuSelected(v);
            return;
        }
    };

private OnKeyListener mKeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_DOWN || event.getRepeatCount() != 0) {
                return false;
            }
            if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                return onMenuSelected(v);
            } else if (keyCode == KeyEvent.KEYCODE_MENU) {
                if (popupWindow != null) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
            }
            return false;
        }
    };
    
    private boolean onMenuSelected(View v){
        Object o = v.getTag();
        if (o == null) {
            return false;
        }
        if (o == JyBaseActivity.this) {
            onTvMenuItemClicked(v.getId());
        }
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof JyBaseFragment && f == o) {
                ((JyBaseFragment) f).onTvMenuItemClicked(v.getId());
            }
        }
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }
        return true;
    }

    public static final class TvMenuItem {
        private int id;
        private Drawable icon;
        private String title;
        private boolean allignRight;
        private boolean focusable = true;
        private boolean focused;
        private Object contributor = null;

        public TvMenuItem(int id, Drawable icon, String title) {
            this.id = id;
            this.icon = icon;
            this.title = title;
        }

        public TvMenuItem(int id, Drawable icon, String title, boolean allignRight) {
            this.id = id;
            this.icon = icon;
            this.title = title;
            this.allignRight = allignRight;
        }

        public void setFocusable(boolean enable){
            focusable = enable;
        }
        
        public void setFocused(boolean focused){
            this.focused = focused;
        }
    }

    public static final class TvMenu {
        private ArrayList<TvMenuItem> items = new ArrayList<TvMenuItem>(5);
        private Object contributor = null;

        public void add(TvMenuItem item) {
            if (item == null) {
                throw new NullPointerException("");
            } else if (item.icon == null && item.title == null) {
                throw new NullPointerException("icon and title are all NULL you stupid!");
            }
            item.contributor = contributor;
            items.add(item);
        }

        public void remove(TvMenuItem item) {
            for (TvMenuItem i : items) {
                if (i == item) {
                    items.remove(i);
                    return;
                }
            }
        }

        public int getCount() {
            return items.size();
        }

    }

    public void onCreateTvMenu(TvMenu menu) {
        //No Default menu item now.
        //if you wanna add some menu item, override this method
    }

    public void onTvMenuItemClicked(int menuId) {
    }

    protected void animateBg() {
        if (mBg == null) {
            return;
        }
        mReverseBg = !mReverseBg;
        if (mReverseBg) {
            mBg.startTransition(500);
        } else {
            mBg.reverseTransition(500);
        }
    }

    protected RootRelativeLayout getRootView() {
        return mRootView;
    }

    @Override
    protected void onResume() {
        IRMonitor.getInstance(this).onResume();
        super.onResume();
    }
    @Override
    protected void onPause() {
        IRMonitor.getInstance(this).onPause();;
        super.onPause();
    };
}

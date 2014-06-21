package com.jiaoyang.tv;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.jiaoyang.base.caching.ImageCache;
import com.jiaoyang.base.caching.ImageFetcher;
import com.jiaoyang.tv.JyBaseActivity.TvMenu;
import com.jiaoyang.tv.app.JiaoyangTvApplication;
import com.jiaoyang.tv.content.NavigationalControlFragment;
import com.jiaoyang.tv.util.Logger;
import com.jiaoyang.tv.util.Util;
import com.jiaoyang.video.tv.R;

/**
 * Jiaoyang应用的Fragment基类
 * 主要功能有：<p>
 *   1. 提供一个ImageFetcher<p>
 *   2. 提供切换Fragment的接口<p>
 *   3. 提供显示简单对话框的接口<p>
 *   4. 提供管理ActionBar标题的接口<p>
 *   5. 提供显示Toast的接口<p>
 *
 */
public abstract class JyBaseFragment extends Fragment {
    private static final Logger LOG = Logger.getLogger(JyBaseFragment.class);

    private ImageFetcher mImageFetcher;

    private AlertDialog mSimpleDialog;
    public CharSequence getTitle() {
        return getActivity().getTitle();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopImageFetcher();
    }

    private void stopImageFetcher() {
        if (mImageFetcher != null) {
            mImageFetcher.setExitTasksEarly(true);
            mImageFetcher = null;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopImageFetcher();
    }

    protected void replaceFragment(Class<?> fregmentClass, Bundle arguments) {
        replaceFragment(fregmentClass, arguments, NavigationalControlFragment.INVALID_DIRECTION);
    }

    /**
     * 将当前Fragment所在Activity的R.id.content_frame替换为指定的Fragment
     * @param fregmentClass
     * @param arguments
     * @param direction 替换前后两个Fragment的相对方向，用来实现相应的动画平移
     */
    protected void replaceFragment(Class<?> fregmentClass, Bundle arguments, int direction) {
        LOG.debug("replace fragment. class={}", fregmentClass.getName());

        Fragment fragment = Fragment.instantiate(getActivity(), fregmentClass.getName(), arguments);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (direction == View.FOCUS_LEFT) {
            transaction.setCustomAnimations(R.anim.left_in, R.anim.right_out);
        } else if (direction == View.FOCUS_RIGHT) {
            transaction.setCustomAnimations(R.anim.right_in, R.anim.left_out);
        } else {
            //nothing to be done.
        }
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
        if (getActivity() instanceof JyBaseActivity) {
            ((JyBaseActivity)getActivity()).animateBg();
        }
    }

    protected void openFragment(Fragment fromFragment, Class<?> fregmentClass, Bundle arguments) {
        LOG.debug("open fragment. class={}", fregmentClass.getName());

        Fragment fragment = Fragment.instantiate(getActivity(), fregmentClass.getName(), arguments);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.hide(fromFragment);
        transaction.add(R.id.content_frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    protected ImageCache getImageCache() {
        ImageCache cache = null;

        JiaoyangTvApplication ca = (JiaoyangTvApplication) getActivity().getApplication();
        if (ca != null) {
            cache = ca.getImageCache();
        }

        return cache;
    }
    
    protected ImageFetcher getImageFetcher(Activity activity) {
        if (activity == null) {
            return getImageFetcher(getActivity(), R.drawable.poster_default);
        } else {
            return getImageFetcher(activity, R.drawable.poster_default);
        }
    }

    protected ImageFetcher getImageFetcher() {
        return getImageFetcher(getActivity(), R.drawable.poster_default);
    }
    
    protected ImageFetcher getImageFetcher(int loadingImageResId) {
        return getImageFetcher(getActivity(), loadingImageResId);
    }

    protected ImageFetcher getImageFetcher(Activity activity, int loadingImageResId) {
        if (activity == null) {
            return null;
        }
        if (mImageFetcher == null) {
            mImageFetcher = ImageFetcher.getInstance(getActivity());
            mImageFetcher.setImageCache(getImageCache());
        }
        mImageFetcher.setLoadingImage(loadingImageResId);

        return mImageFetcher;
    }

    protected void finish() {
        getFragmentManager().popBackStack();
    }

    protected void showToast(String text, int duration) {
        Context context = getActivity();
        if (context != null)
            Util.showToast(context, text, duration);
    }

    protected void showToast(int resId, int duration) {
        showToast(getString(resId), duration);
    }

    protected void showSipleDialog(int titleResId, int msgResId) {
        showSipleDialog(getString(titleResId), getString(msgResId));
    }

    protected void showSipleDialog(String title, String msg) {
        if (mSimpleDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            mSimpleDialog = builder.create();
        }
        mSimpleDialog.setTitle(title);
        mSimpleDialog.setMessage(msg);
        mSimpleDialog.show();
    }

    protected void openActivity(Class<? extends JyBaseActivity> activityClass, Bundle arguments) {
        LOG.debug("open activity. class={}", activityClass.getName());

        Intent intent = new Intent(getActivity(), activityClass);
        if (arguments != null) {
            intent.putExtras(arguments);
        }
        startActivity(intent);
    }

    public void onCreateTvMenu(TvMenu menu) {
        //No Default menu item now.
        //if you wanna add some menu item, override this method
    }

    public void onTvMenuItemClicked(int menuId) {
    }
    
    /**
     * 接收返回键按下事件
     * @Title: onBackKeyDown
     * @return boolean  false:back键事件未处理，向下传递。  true：消费掉该事件。
     * @date 2014-3-10 上午11:15:33
     */
    protected boolean onBackKeyDown(){
        LOG.i("on back key down fragment:"+this.getClass().getName());
        return false;
    }
}

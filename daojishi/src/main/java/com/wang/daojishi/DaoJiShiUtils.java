package com.wang.daojishi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashSet;

/**
 * 每个一段时间发送回调的倒计时工具类
 * <p>
 * 一个handler管理所有的倒计时，防止创建过多的handler
 */
public class DaoJiShiUtils {

    private final HashSet<OnDaoJiShiListener> mSetListeners = new HashSet<>();

    private final int mHandlerTime;
    @Nullable
    private final Activity mActivity;

    @SuppressLint("HandlerLeak")//本代码不会出现内存泄漏
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (mSetListeners.size() == 0 || (mActivity != null && mActivity.isFinishing())) {
                return;
            }

            Object[] listeners = mSetListeners.toArray();
            for (Object listener : listeners) {
                //noinspection SuspiciousMethodCalls 做这一步判断是因为遍历过程中可能会被删掉
                if (mSetListeners.contains(listener)) {
                    ((OnDaoJiShiListener) listener).onZeroOneSecond();
                }
            }
            sendEmptyMessageDelayed(1, mHandlerTime);
        }
    };

    /**
     * @param activity  防止出现内存泄漏，此处强制要求传入当前Activity。不传也可以，但必须自己手动关掉{@link #stopAll}
     * @param intervals 发送间隔毫秒值，比如100表示0.1秒发一次、1000表示1秒发一次
     */
    @MainThread
    public DaoJiShiUtils(@Nullable Activity activity, int intervals) {
        mActivity = activity;
        mHandlerTime = intervals;
    }

    /**
     * 添加倒计时回调
     */
    @MainThread
    public void addTimeListener(@NonNull OnDaoJiShiListener listener) {
        if (mSetListeners.size() == 0) {
            mHandler.removeMessages(1);
            mHandler.sendEmptyMessageDelayed(1, mHandlerTime);
        }
        mSetListeners.add(listener);
    }

    /**
     * 删除倒计时
     */
    @MainThread
    public boolean removeTimeListener(@NonNull OnDaoJiShiListener listener) {
        return mSetListeners.remove(listener);
    }

    /**
     * 如果没传Activity，则需要手动停止
     */
    @MainThread
    public void stopAll() {
        mSetListeners.clear();
        mHandler.removeCallbacksAndMessages(null);
    }


    /**
     * 监听
     */
    public interface OnDaoJiShiListener {
        /**
         * 每隔一段时间回调一次
         */
        void onZeroOneSecond();
    }
}

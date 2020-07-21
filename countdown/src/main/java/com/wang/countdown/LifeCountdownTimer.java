package com.wang.countdown;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import java.util.HashSet;

/**
 * 每个一段时间发送回调的倒计时工具类
 * <p>
 * 一个handler管理所有的倒计时，防止创建过多的handler
 */
public class LifeCountdownTimer {

    private final HashSet<OnCountdownListener> mSetListeners = new HashSet<>();

    private final int mHandlerTime;

    @Nullable
    private final LifecycleOwner mLifeOwner;

    @SuppressLint("HandlerLeak")//本代码不会出现内存泄漏
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (mSetListeners.isEmpty()) {
                return;
            }
            if ((mLifeOwner != null && mLifeOwner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED)) {
                Object[] listeners = mSetListeners.toArray();
                for (Object listener : listeners) {
                    //noinspection SuspiciousMethodCalls 做这一步判断是因为遍历过程中可能会被删掉
                    if (mSetListeners.contains(listener)) {
                        ((OnCountdownListener) listener).onUiDestroyed();
                    }
                }
                return;
            }

            Object[] listeners = mSetListeners.toArray();
            for (Object listener : listeners) {
                //noinspection SuspiciousMethodCalls 做这一步判断是因为遍历过程中可能会被删掉
                if (mSetListeners.contains(listener)) {
                    ((OnCountdownListener) listener).onTick();
                }
            }
            sendEmptyMessageDelayed(1, mHandlerTime);
        }
    };

    /**
     * @param owner     防止出现内存泄漏，此处强制要求传入。不传也可以，但必须自己手动关掉{@link #stopAll}
     * @param intervals 发送间隔毫秒值，比如100表示0.1秒发一次、1000表示1秒发一次
     */
    @MainThread
    public LifeCountdownTimer(@Nullable LifecycleOwner owner, int intervals) {
        mLifeOwner = owner;
        mHandlerTime = intervals;
    }

    /**
     * 添加倒计时回调
     * 第一次add，会延时传入的时间后回调
     */
    @MainThread
    public void addTimeListener(@NonNull OnCountdownListener listener) {
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
    public boolean removeTimeListener(@NonNull OnCountdownListener listener) {
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
    public interface OnCountdownListener {
        /**
         * 每隔一段时间回调一次
         */
        void onTick();

        /**
         * 回调还在进行中，但ui已经destroy了，会停止循环并调用
         */
        default void onUiDestroyed() {
        }
    }
}

package com.wang.example;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import com.wang.adapters.adapter.BaseAdapterRvList;
import com.wang.adapters.base.BaseViewHolder;
import com.wang.daojishi.DaoJiShiUtils;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRv;
    private BaseAdapterRvList<BaseViewHolder, Long> mAdapter;
    private DaoJiShiUtils mDjs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mRv = new RecyclerView(this));
        mDjs = new DaoJiShiUtils(this, 10);
        mRv.setLayoutManager(new GridLayoutManager(this, 3));

        //随机当天1小时内数据
        Calendar calendar = Calendar.getInstance();
        final ArrayList<Long> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            long suiJi = calendar.getTimeInMillis() + (long) (Math.random() * TimeUtils.TIME_HOUR);//1小时内随机
            list.add(suiJi);
        }
        //一个封装好的ListAdapter
        mAdapter = new BaseAdapterRvList<BaseViewHolder, Long>(this, list) {

            @Override
            protected void onBindVH(BaseViewHolder holder, int listPosition, Long aLong) {
                MyDaoJiShiView tv = (MyDaoJiShiView) holder.itemView;
                tv.startDaoJiShi(aLong);
                tv.setTag(listPosition);
            }

            @NonNull
            @Override
            protected BaseViewHolder onCreateVH(ViewGroup parent, LayoutInflater inflater) {
                MyDaoJiShiView tv = new MyDaoJiShiView(mActivity);
                tv.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tv.setGravity(Gravity.CENTER);
                tv.setPadding(0, 10, 0, 10);
                final BaseViewHolder holder = new BaseViewHolder(tv);
                tv.setListener(new OnDaoJiShiViewListener() {
                    @Override
                    public void onOneSecond(long shenYuShiJian) {
                        //此处每0.1秒回调一次
                    }

                    @Override
                    public void onEnd() {
                        int layoutPosition = holder.getLayoutPosition();
                        Toast.makeText(mActivity, "第" + (layoutPosition + 1) + "个倒计时结束啦", Toast.LENGTH_SHORT).show();
                        waitDelete(mList.get(layoutPosition));
                    }
                });
                return holder;
            }

            @Override
            public void onViewRecycled(@NonNull BaseViewHolder holder) {
                super.onViewRecycled(holder);
                ((MyDaoJiShiView) holder.itemView).stopDaoJiShi();//防止躺缓存池里面还在倒计时回调
            }
        };
        mRv.setAdapter(mAdapter);
    }

    /**
     * 递归等待滑动停止后再删除数据（RecyclerView通病，ui变化中不能刷新数据）
     *
     * @param deleteData 不建议使用position，因为如果出现多个等待position将会出错
     */
    private void waitDelete(final Long deleteData) {
        if (mRv.isComputingLayout()) {
            mRv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    waitDelete(deleteData);
                }
            }, 100);
        } else {
            mAdapter.getList().remove(deleteData);
            mAdapter.notifyDataSetChanged();
        }
    }

    private class MyDaoJiShiView extends AppCompatTextView {
        private long mTargetTime;
        private OnDaoJiShiViewListener mListener;
        private DaoJiShiUtils.OnDaoJiShiListener mTimeListener = new DaoJiShiUtils.OnDaoJiShiListener() {
            @Override
            public void onZeroOneSecond() {
                if (((Activity) getContext()).isFinishing()) {
                    mDjs.removeTimeListener(mTimeListener);//Activity关闭了自然需要停止倒计时。停止倒计时不是必须的，但Activity Destroy了执行下面的代码可能会崩溃
                    return;
                }
                long shengYuShiJian = mTargetTime - System.currentTimeMillis();
                if (shengYuShiJian > 0) {
                    String[] timeSt = TimeUtils.getMillsTimeSt(shengYuShiJian);
                    long hm = shengYuShiJian % 1000 / 10;//0.01秒
                    setText("剩余：" + timeSt[0] + ":" + timeSt[1] + ":" + timeSt[2] + "." + TimeUtils.get2TimeSt((int) hm));
                    if (mListener != null) mListener.onOneSecond(shengYuShiJian);
                    mIsRequestLayout = false;
                } else {
                    //已经结束
                    setText("剩余：00:00:00.00");
                    mDjs.removeTimeListener(mTimeListener);
                    if (mListener != null) mListener.onEnd();
                    mIsRequestLayout = true;
                }
            }
        };

        /**
         * 是否调用{@link #requestLayout}
         * ui改变比较快并且布局本身并没有发生变化,此时会拦截不再请求改变布局
         */
        private boolean mIsRequestLayout = true;

        public MyDaoJiShiView(Context context) {
            super(context);
        }

        public MyDaoJiShiView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public MyDaoJiShiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public void requestLayout() {
            if (mIsRequestLayout) {
                super.requestLayout();
            }
        }

        /**
         * 请使用这个 方法
         */
        public void startDaoJiShi(long targetTime) {
            mTargetTime = targetTime;
            mIsRequestLayout = true;
            mTimeListener.onZeroOneSecond();//初始化一次
            mDjs.addTimeListener(mTimeListener);
        }

        public void stopDaoJiShi() {
            mDjs.removeTimeListener(mTimeListener);
        }

        /**
         * @param listener 倒计时的监听
         */
        public void setListener(OnDaoJiShiViewListener listener) {
            mListener = listener;
        }
    }

    private interface OnDaoJiShiViewListener {
        /**
         * 每0.1秒回调一次
         *
         * @param shenYuShiJian 还剩多少时间
         */
        void onOneSecond(long shenYuShiJian);

        /**
         * 当结束时
         */
        void onEnd();
    }
}

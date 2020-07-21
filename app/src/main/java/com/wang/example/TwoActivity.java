package com.wang.example;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.collection.ArraySet;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wang.adapters.adapter.BaseAdapterRvList;
import com.wang.adapters.base.BaseViewHolder;
import com.wang.countdown.LifeCountdownTimer;

import java.util.ArrayList;

public class TwoActivity extends AppCompatActivity {

    private LifeCountdownTimer mDjs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecyclerView rv = new RecyclerView(this);
        setContentView(rv);
        mDjs = new LifeCountdownTimer(this, 10);
        rv.setLayoutManager(new GridLayoutManager(this, 3));

        //随机当天1小时内数据
        final ArrayList<MainActivity.TestBean> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            long suiJi = System.currentTimeMillis() + (long) (Math.random() * TimeUtils.TIME_HOUR);//1小时内随机
            MainActivity.TestBean tb = new MainActivity.TestBean();
            tb.endTime = suiJi;
            list.add(tb);
        }
        //一个封装好的ListAdapter
        BaseAdapterRvList<BaseViewHolder, MainActivity.TestBean> adapter = new BaseAdapterRvList<BaseViewHolder, MainActivity.TestBean>(this, list) {
            ArraySet<BaseViewHolder> mSetItems = new ArraySet<>();

            //内部类没有构造
            {
                mDjs.addTimeListener(() -> {
                    for (BaseViewHolder holder : mSetItems) {
                        int position = holder.getLayoutPosition();
                        changeText(holder, get(position));
                    }
                });
            }

            private void changeText(BaseViewHolder holder, MainActivity.TestBean bean) {
                CustomTextView tv = (CustomTextView) holder.itemView;
                if (bean.isEnd) {
                    tv.mIsRequestLayout = true;
                    tv.setText("已结束");
                } else {
                    long time = bean.endTime - System.currentTimeMillis();
                    if (time > 0) {
                        String[] timeSt = TimeUtils.getMillsTimeSt(time);
                        long hm = time % 1000 / 10;//0.01秒
                        String newText = "剩：" + timeSt[0] + ":" + timeSt[1] + ":" + timeSt[2] + "." + TimeUtils.get2TimeSt((int) hm);
                        CharSequence oldText = tv.getText();
                        tv.mIsRequestLayout = oldText == null || oldText.length() != newText.length();//文本大小没有变化不需要重新计算布局
                        tv.setText(newText);
                    } else {
                        bean.isEnd = true;
                        Toast.makeText(mActivity, "第" + (holder.getLayoutPosition() + 1) + "个倒计时结束啦", Toast.LENGTH_SHORT).show();
                        changeText(holder, bean);
                    }
                }
            }

            @Override
            protected void onBindVH(BaseViewHolder holder, int listPosition, MainActivity.TestBean bean) {
                mSetItems.add(holder);
                changeText(holder, bean);
            }

            @NonNull
            @Override
            protected BaseViewHolder onCreateVH(ViewGroup parent, LayoutInflater inflater) {
                CustomTextView tv = new CustomTextView(mActivity);
                tv.setBackgroundColor(0xffeeeeee);
                tv.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                return new BaseViewHolder(tv);
            }

            @Override
            public void onViewRecycled(@NonNull BaseViewHolder holder) {
                super.onViewRecycled(holder);
                mSetItems.remove(holder);
            }
        };
        rv.setAdapter(adapter);
    }

    private static class CustomTextView extends AppCompatTextView {

        /**
         * 是否调用{@link #requestLayout}
         * ui改变比较快并且布局本身并没有发生变化,此时会拦截不再请求改变布局
         * <p>
         * 在高端手机上可能感受不到，放一些复杂布局或放到底端手机、模拟器就可以感受到卡顿的酸爽了
         */
        private boolean mIsRequestLayout = true;

        public CustomTextView(Context context) {
            super(context);
        }

        public CustomTextView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public CustomTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public void requestLayout() {
            Log.e("哈哈", "requestLayout了: " + mIsRequestLayout);
            if (mIsRequestLayout) {
                super.requestLayout();
            }
        }
    }
}

package com.wang.example;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {

    private LifeCountdownTimer mDjs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView rv = findViewById(R.id.rv_main);
        findViewById(R.id.tv_two).setOnClickListener((view) -> startActivity(new Intent(this, TwoActivity.class)));

        mDjs = new LifeCountdownTimer(this, 1000);
        rv.setLayoutManager(new GridLayoutManager(this, 3));

        //随机当天1小时内数据
        final ArrayList<TestBean> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            long suiJi = System.currentTimeMillis() + (long) (Math.random() * TimeUtils.TIME_HOUR);//1小时内随机
            TestBean tb = new TestBean();
            tb.endTime = suiJi;
            list.add(tb);
        }
        //一个封装好的ListAdapter
        BaseAdapterRvList<BaseViewHolder, TestBean> adapter = new BaseAdapterRvList<BaseViewHolder, TestBean>(this, list) {
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

            @SuppressLint("SetTextI18n")
            private void changeText(BaseViewHolder holder, TestBean bean) {
                TextView tv = (TextView) holder.itemView;
                if (bean.isEnd) {
                    tv.setText("已结束");
                } else {
                    long time = bean.endTime - System.currentTimeMillis();
                    if (time > 0) {
                        String[] timeSt = TimeUtils.getMillsTimeSt(time);
                        tv.setText("剩余：" + timeSt[0] + ":" + timeSt[1] + ":" + timeSt[2]);
                    } else {
                        bean.isEnd = true;
                        changeText(holder, bean);
                    }
                }
            }

            @Override
            protected void onBindVH(BaseViewHolder holder, int listPosition, TestBean bean) {
                mSetItems.add(holder);
                changeText(holder, bean);
            }

            @NonNull
            @Override
            protected BaseViewHolder onCreateVH(ViewGroup parent, LayoutInflater inflater) {
                return new BaseViewHolder(new AppCompatTextView(mActivity));
            }

            @Override
            public void onViewRecycled(@NonNull BaseViewHolder holder) {
                super.onViewRecycled(holder);
                mSetItems.remove(holder);
            }
        };
        rv.setAdapter(adapter);
    }

    static class TestBean {
        long endTime;
        /**
         * 倒计时是否结束，防止循环刷新
         */
        boolean isEnd = false;
    }
}

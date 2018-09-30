package com.yrtech.datepickerpopupwindow;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.pick_date_tv)
    TextView pickDateTv;//地址选择器的文本框
    @BindView(R.id.pick_date_cl)
    ConstraintLayout pickDateCl;//布局

    Handler handler;//用以实现后续操作

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        handler = new Handler(new Handler.Callback() {//通过handler改变textview的值
            @Override
            public boolean handleMessage(Message msg) {
                pickDateTv.setText((String) msg.obj);
                return false;
            }
        });
    }

    @OnClick(R.id.pick_date_cl)
    public void onViewClicked() {//弹出popuopwindow
        DatePickerPopupwindow datePickerPopupwindow = new DatePickerPopupwindow(MainActivity.this, handler);
        datePickerPopupwindow.showAtLocation(pickDateCl, Gravity.BOTTOM, 0, 0);
    }
}

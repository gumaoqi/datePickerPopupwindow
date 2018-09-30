package com.yrtech.datepickerpopupwindow;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import entity.Province;
import widget.OnWheelChangedListener;
import widget.StrericWheelAdapter;
import widget.WheelView;

public class DatePickerPopupwindow extends PopupWindow {
    private final String TAG = "DatePickerPopupwindow";

    @BindView(R.id.popupwindow_date_picker_background_ll)
    LinearLayout popupwindowDatePickerBackgroundLl;     //定义来玩玩
    @BindView(R.id.popupwindow_date_picker_cancel_tv)
    TextView popupwindowDatePickerCancelTv;             //取消
    @BindView(R.id.popupwindow_date_picker_title_tv)
    TextView popupwindowDatePickerTitleTv;              //标题
    @BindView(R.id.popupwindow_date_picker_sure_tv)
    TextView popupwindowDatePickerSureTv;               //确定
    @BindView(R.id.popupwindow_date_picker_first_wv)
    WheelView popupwindowDatePickerFirstWv;             //第一个滑轮，这里表示省
    @BindView(R.id.popupwindow_date_picker_second_wv)
    WheelView popupwindowDatePickerSecondWv;            //第二个滑轮，这里表示市
    @BindView(R.id.popupwindow_date_picker_third_wv)
    WheelView popupwindowDatePickerThirdWv;             //第三个滑轮，这里表示县/区
    @BindView(R.id.popupwindow_date_picker_ll)
    LinearLayout popupwindowDatePickerLl;               //整体popupwindow的布局，后面用以点击popupwindow外将其diss

    private View mMenuView;//解析出来的布局
    private Context context;
    private Handler handler;//用以修改MainActivity中的控件
    List<Province> provinceList;//省集合数据，需要通过gson去解析得到
    List<String> firstList;//省名集合
    List<String> secondList;//市名集合
    List<String> thirdList;//县名集合

    int provinceValue = 0;//当前在哪一个省

    StrericWheelAdapter firstStrericWheelAdapter;//省集合的adapter
    StrericWheelAdapter secondStrericWheelAdapter;//市集合的adapter
    StrericWheelAdapter thirdStrericWheelAdapter;//县集合的adapter

    public DatePickerPopupwindow(Context context, Handler handler) {
        super(context);
        this.context = context;
        this.handler = handler;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.popupwindow_date_picker, null);
        ButterKnife.bind(this, mMenuView);
        //设置标题
        popupwindowDatePickerTitleTv.setText("选择地址");
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(800);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int height = mMenuView.findViewById(R.id.popupwindow_date_picker_ll).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
        iniData();
    }

    void iniData() {
        firstList = new ArrayList<>();
        secondList = new ArrayList<>();
        thirdList = new ArrayList<>();
        String string = readAssertResource(context, "province.txt");
        provinceList = JSON.parseArray(string, Province.class);
        for (Province province : provinceList) {//拿到省名集合
            firstList.add(province.getName());
        }
        for (Province.CityBean cityBean : provinceList.get(0).getCity()) {//拿到第一个省市的名集合
            secondList.add(cityBean.getName());
        }
        for (String area : provinceList.get(0).getCity().get(0).getArea()) {//拿到第一个省第一个市的县名名集合
            thirdList.add(area);
        }
        firstStrericWheelAdapter = new StrericWheelAdapter(firstList);
        secondStrericWheelAdapter = new StrericWheelAdapter(secondList);
        thirdStrericWheelAdapter = new StrericWheelAdapter(thirdList);
        popupwindowDatePickerFirstWv.setAdapter(firstStrericWheelAdapter);
        popupwindowDatePickerSecondWv.setAdapter(secondStrericWheelAdapter);
        popupwindowDatePickerThirdWv.setAdapter(thirdStrericWheelAdapter);
        popupwindowDatePickerFirstWv.addChangingListener(new OnWheelChangedListener() {//设置一级联动，当省改变时，对应的市，县改变
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                secondList.clear();
                for (Province.CityBean cityBean : provinceList.get(newValue).getCity()) {
                    secondList.add(cityBean.getName());
                }
                secondStrericWheelAdapter = new StrericWheelAdapter(secondList);
                popupwindowDatePickerSecondWv.setAdapter(secondStrericWheelAdapter);
                provinceValue = newValue;
                thirdList.clear();
                for (String area : provinceList.get(provinceValue).getCity().get(0).getArea()) {
                    thirdList.add(area);
                }
                thirdStrericWheelAdapter = new StrericWheelAdapter(thirdList);
                popupwindowDatePickerThirdWv.setAdapter(thirdStrericWheelAdapter);
                popupwindowDatePickerSecondWv.setCurrentItem(0);
                popupwindowDatePickerThirdWv.setCurrentItem(0);
            }
        });
        popupwindowDatePickerSecondWv.addChangingListener(new OnWheelChangedListener() {//设置二级联动，当市改变时，对应的县改变
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                thirdList.clear();
                for (String area : provinceList.get(provinceValue).getCity().get(newValue).getArea()) {
                    thirdList.add(area);
                }
                thirdStrericWheelAdapter = new StrericWheelAdapter(thirdList);
                popupwindowDatePickerThirdWv.setAdapter(thirdStrericWheelAdapter);
                popupwindowDatePickerThirdWv.setCurrentItem(0);
            }
        });
    }

    /**
     * 将txt文件的内容转换为String
     *
     * @param context
     * @param strAssertFileName
     * @return
     */
    private String readAssertResource(Context context, String strAssertFileName) {//将数据从text中读出
        AssetManager assetManager = context.getAssets();
        String strResponse = "";
        try {
            InputStream ims = assetManager.open(strAssertFileName);
            strResponse = getStringFromInputStream(ims);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResponse;
    }

    /**
     * 将流的内容转换为string
     *
     * @param a_is 需要转换的流
     * @return
     */
    private String getStringFromInputStream(InputStream a_is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(a_is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
        return sb.toString();
    }

    @OnClick({R.id.popupwindow_date_picker_background_ll, R.id.popupwindow_date_picker_cancel_tv, R.id.popupwindow_date_picker_title_tv, R.id.popupwindow_date_picker_sure_tv, R.id.popupwindow_date_picker_first_wv, R.id.popupwindow_date_picker_second_wv, R.id.popupwindow_date_picker_third_wv, R.id.popupwindow_date_picker_ll})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.popupwindow_date_picker_background_ll:
                dismiss();
                break;
            case R.id.popupwindow_date_picker_cancel_tv:
                dismiss();
                break;
            case R.id.popupwindow_date_picker_sure_tv:
                String province = provinceList.get(popupwindowDatePickerFirstWv.getCurrentItem()).getName();
                String city = provinceList.get(popupwindowDatePickerFirstWv.getCurrentItem()).getCity().get(popupwindowDatePickerSecondWv.getCurrentItem()).getName();
                String county = provinceList.get(popupwindowDatePickerFirstWv.getCurrentItem()).getCity().
                        get(popupwindowDatePickerSecondWv.getCurrentItem()).getArea().get(popupwindowDatePickerThirdWv.getCurrentItem());
                Message message = handler.obtainMessage();
                message.arg1 = 1;
                message.obj = province + city + county;
                handler.sendMessage(message);
                dismiss();
                break;
        }
    }
}

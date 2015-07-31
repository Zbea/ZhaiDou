package com.zhaidou.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.zhaidou.R;
import com.zhaidou.model.Address;
import com.zhaidou.model.Area;
import com.zhaidou.model.City;
import com.zhaidou.model.Province;
import com.zhaidou.utils.PixelUtil;

import java.util.List;

/**
 * Created by wangclark on 15/7/30.
 */
public class WheelViewContainer extends LinearLayout{
    private Context mContext;
    private WheelView provinceWheel;
    private WheelView cityWheel;
    private WheelView areaWheel;
    private int SELECTED_PROVINCE_INCEX=1;
    private int SELECTED_CITY_INDEX=1;
    private int SELECTED_AREA_INDEX=1;
    private List<Province> provinceList;
    public WheelViewContainer(Context context) {

        super(context);
        Log.i("WheelViewContainer(Context context)","WheelViewContainer(Context context)");
        mContext=context;
        initWheelView();
        setOrientation(LinearLayout.HORIZONTAL);
    }

    public WheelViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i("WheelViewContainer(Context context, AttributeSet attrs)","WheelViewContainer(Context context, AttributeSet attrs)");
        mContext=context;
        initWheelView();
        setOrientation(LinearLayout.HORIZONTAL);
    }

    public WheelViewContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.i("WheelViewContainer(Context context, AttributeSet attrs, int defStyle)","WheelViewContainer(Context context, AttributeSet attrs, int defStyle)");
        mContext=context;
        initWheelView();
        setOrientation(LinearLayout.HORIZONTAL);
    }

    private void initWheelView(){
        Log.i("mContext----------------->",mContext.toString());
        provinceWheel = new WheelView(mContext);
        provinceWheel.setBackgroundColor(getResources().getColor(R.color.gray_9));
        ScrollView.LayoutParams provinceParams=new ScrollView.LayoutParams(PixelUtil.dp2px(100,mContext),PixelUtil.dp2px(100,mContext));
        provinceWheel.setLayoutParams(provinceParams);

        cityWheel = new WheelView(mContext);
        ViewGroup.LayoutParams cityParams=new ViewGroup.LayoutParams(PixelUtil.dp2px(100,mContext),PixelUtil.dp2px(100,mContext));
        cityWheel.setLayoutParams(cityParams);
//
        areaWheel = new WheelView(mContext);
        ViewGroup.LayoutParams areaParams=new ViewGroup.LayoutParams(PixelUtil.dp2px(100,mContext),PixelUtil.dp2px(100,mContext));
        areaWheel.setLayoutParams(areaParams);
        addView(provinceWheel);
        addView(cityWheel);
        addView(areaWheel);

    }

    public void setData(List<Province> provinceList){
        for(Province province:provinceList){
            Log.i("province----->",province.getName());
            List<City> cityList=province.getCityList();
            for (City city:cityList){
                Log.i("city---------->",city.getName());
                List<Area> areas=city.getAreas();
                for (Area area:areas){
                    Log.i("area----------->",area.getName());
                }
            }
        }


        this.provinceList=provinceList;
        provinceWheel.setItems(provinceList);
        provinceWheel.setOffset(SELECTED_PROVINCE_INCEX);
        provinceWheel.setOnWheelViewListener(new WheelView.OnWheelViewListener(){
            @Override
            public void onSelected(int selectedIndex, Address item) {
                SELECTED_PROVINCE_INCEX=selectedIndex;
                Log.i("selectedIndex------------>",selectedIndex+"");
                Log.i("Address------------->",item.toString());

            }
        });

        cityWheel.setItems(provinceList.get(SELECTED_PROVINCE_INCEX-1).getCityList());
        cityWheel.setOffset(SELECTED_CITY_INDEX);
        cityWheel.setOnWheelViewListener(new WheelView.OnWheelViewListener(){
            @Override
            public void onSelected(int selectedIndex, Address item) {
                SELECTED_CITY_INDEX=selectedIndex;
//                Log.i("selectedIndex------------>",selectedIndex+"");
//                Log.i("Address------------->",item.toString());
                notifyDataChange("city",selectedIndex);
            }
        });
//
        areaWheel.setItems(provinceList.get(SELECTED_PROVINCE_INCEX-1).getCityList().get(SELECTED_CITY_INDEX-1).getAreas());
        areaWheel.setOffset(SELECTED_AREA_INDEX);
        areaWheel.setOnWheelViewListener(new WheelView.OnWheelViewListener(){
            @Override
            public void onSelected(int selectedIndex, Address item) {
                SELECTED_AREA_INDEX=selectedIndex;
                Log.i("selectedIndex------------>",selectedIndex+"");
                Log.i("Address------------->",item.toString());
                notifyDataChange("area",selectedIndex);
            }
        });
    }

    private void notifyDataChange(String type,int selectedPosition){
        if ("area".equalsIgnoreCase(type)){
            areaWheel.notifyDataSetChange(provinceList.get(SELECTED_PROVINCE_INCEX-1).getCityList().get(SELECTED_CITY_INDEX-1).getAreas(),selectedPosition);
            return;
        }
        cityWheel.notifyDataSetChange(provinceList.get(SELECTED_PROVINCE_INCEX-1).getCityList(),selectedPosition);
        areaWheel.notifyDataSetChange(provinceList.get(SELECTED_PROVINCE_INCEX-1).getCityList().get(SELECTED_CITY_INDEX-1).getAreas(),selectedPosition);
    }

}

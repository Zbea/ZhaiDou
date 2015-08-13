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
 import com.zhaidou.utils.CollectionUtils;
 import com.zhaidou.utils.PixelUtil;

 import java.util.ArrayList;
 import java.util.List;

/**
  * Created by wangclark on 15/7/30.
  */
 public class WheelViewContainer extends LinearLayout {
     private Context mContext;
     private WheelView provinceWheel;
     private WheelView cityWheel;
     private WheelView areaWheel;
     private int SELECTED_PROVINCE_INCEX = 1;
     private int SELECTED_CITY_INDEX = 1;
     private int SELECTED_AREA_INDEX = 1;
     private List<Province> provinceList;

     public WheelViewContainer(Context context) {

         super(context);
         Log.i("WheelViewContainer(Context context)", "WheelViewContainer(Context context)");
         mContext = context;
         initWheelView();
         setOrientation(LinearLayout.HORIZONTAL);
     }

     public WheelViewContainer(Context context, AttributeSet attrs) {
         super(context, attrs);
         Log.i("WheelViewContainer(Context context, AttributeSet attrs)", "WheelViewContainer(Context context, AttributeSet attrs)");
         mContext = context;
         initWheelView();
         setOrientation(LinearLayout.HORIZONTAL);
     }

     public WheelViewContainer(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         Log.i("WheelViewContainer(Context context, AttributeSet attrs, int defStyle)", "WheelViewContainer(Context context, AttributeSet attrs, int defStyle)");
         mContext = context;
         initWheelView();
         setOrientation(LinearLayout.HORIZONTAL);
     }

     private void initWheelView() {
         Log.i("mContext----------------->", mContext.toString());
         provinceWheel = new WheelView(mContext);
         provinceWheel.setBackgroundColor(getResources().getColor(R.color.gray_9));
         ScrollView.LayoutParams provinceParams = new ScrollView.LayoutParams(PixelUtil.dp2px(100, mContext), PixelUtil.dp2px(100, mContext));
         provinceWheel.setLayoutParams(provinceParams);

         cityWheel = new WheelView(mContext);
         ViewGroup.LayoutParams cityParams = new ViewGroup.LayoutParams(PixelUtil.dp2px(100, mContext), PixelUtil.dp2px(100, mContext));
         cityWheel.setLayoutParams(cityParams);
         //
         areaWheel = new WheelView(mContext);
         ViewGroup.LayoutParams areaParams = new ViewGroup.LayoutParams(PixelUtil.dp2px(100, mContext), PixelUtil.dp2px(100, mContext));
         areaWheel.setLayoutParams(areaParams);
         addView(provinceWheel);
         addView(cityWheel);
         addView(areaWheel);

     }

     public void setData(List<Province> provinceList) {
         for (Province province : provinceList) {
             Log.i("province----->", province.getName());
             List<City> cityList = province.getCityList();
             for (City city : cityList) {
                 Log.i("city---------->", city.getName());
                 List<Area> areas = city.getAreas();
                 for (Area area : areas) {
                     Log.i("area----------->", area.getName());
                 }
             }
         }


         this.provinceList = provinceList;
         provinceWheel.setItems(provinceList);
         provinceWheel.setOffset(SELECTED_PROVINCE_INCEX);
         provinceWheel.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
             @Override
             public void onSelected(int selectedIndex, Address item) {
                 SELECTED_PROVINCE_INCEX = selectedIndex;
                 SELECTED_CITY_INDEX = 1;
                 SELECTED_AREA_INDEX = 1;
                 Log.i("selectedIndex------------>", selectedIndex + "");
                 Log.i("Address------------->", item.toString());
                 notifyDataChange("province", SELECTED_PROVINCE_INCEX);
                 notifyDataChange("city", SELECTED_CITY_INDEX);
                 notifyDataChange("area", SELECTED_AREA_INDEX);

             }
         });

         cityWheel.setItems(provinceList.get(SELECTED_PROVINCE_INCEX - 1).getCityList());
         cityWheel.setOffset(SELECTED_CITY_INDEX);
         cityWheel.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
             @Override
             public void onSelected(int selectedIndex, Address item) {
                 SELECTED_CITY_INDEX = selectedIndex;
                 SELECTED_AREA_INDEX = 1;
                 notifyDataChange("city", selectedIndex);
                 notifyDataChange("area", 1);
             }
         });
         //
         areaWheel.setItems(provinceList.get(SELECTED_PROVINCE_INCEX - 1).getCityList().get(SELECTED_CITY_INDEX - 1).getAreas());
         areaWheel.setOffset(SELECTED_AREA_INDEX);
         areaWheel.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
             @Override
             public void onSelected(int selectedIndex, Address item) {
                 SELECTED_AREA_INDEX = selectedIndex;
                 Log.i("selectedIndex------------>", selectedIndex + "");
                 Log.i("Address------------->", item.toString());
                 notifyDataChange("area", selectedIndex);
             }
         });
     }

     private void notifyDataChange(String type, int selectedPosition) {
         if ("area".equalsIgnoreCase(type)) {
             if (provinceList.size() > 0 && provinceList.get(SELECTED_PROVINCE_INCEX - 1).getCityList().size() > 0) {
                 areaWheel.notifyDataSetChange(provinceList.get(SELECTED_PROVINCE_INCEX - 1).getCityList().get(SELECTED_CITY_INDEX - 1).getAreas(), selectedPosition);
             } else {
                 areaWheel.notifyDataSetChange(new ArrayList<Address>(), 0);
             }
             return;
         }
         if (provinceList.size() > 0)
             cityWheel.notifyDataSetChange(provinceList.get(SELECTED_PROVINCE_INCEX - 1).getCityList(), selectedPosition);
         if (provinceList.size() > 0 && provinceList.get(SELECTED_PROVINCE_INCEX - 1).getCityList().size() > 0)
             areaWheel.notifyDataSetChange(provinceList.get(SELECTED_PROVINCE_INCEX - 1).getCityList().get(SELECTED_CITY_INDEX - 1).getAreas(), selectedPosition);
     }

     public Province getProvince() {
         if (CollectionUtils.isNotNull(provinceList)) {
             Province province = provinceList.get(SELECTED_PROVINCE_INCEX - 1);
             Log.i("getProvince-------------------->", province.toString());
             return province;
         }
         return null;
     }

     public City getCity() {
         if (CollectionUtils.isNotNull(provinceList)) {
             Province province = provinceList.get(SELECTED_PROVINCE_INCEX - 1);
             if (CollectionUtils.isNotNull(province.getCityList())) {
                 City city = province.getCityList().get(SELECTED_CITY_INDEX - 1);
                 Log.i("city-------------->", city.toString());
                 return city;
             }
             return null;
         }
         return null;
     }

     public Area getArea() {
         if (CollectionUtils.isNotNull(provinceList)) {
             Province province = provinceList.get(SELECTED_PROVINCE_INCEX - 1);
             if (CollectionUtils.isNotNull(province.getCityList())) {
                 City city = province.getCityList().get(SELECTED_CITY_INDEX - 1);
                 if (CollectionUtils.isNotNull(city.getAreas())) {
                     Area area = city.getAreas().get(SELECTED_AREA_INDEX - 1);
                     Log.i("area------------>", area.toString());
                     return area;
                 }
             }
             return null;
         }
         return null;
     }
 }

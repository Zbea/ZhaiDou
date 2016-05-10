package com.zhaidou.fragments;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.MultipartRequest1;
import com.zhaidou.model.Order;
import com.zhaidou.model.OrderItem1;
import com.zhaidou.model.ReturnItem;
import com.zhaidou.model.Store;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.utils.DialogUtils;
import com.zhaidou.utils.NetworkUtils;
import com.zhaidou.utils.PhotoUtil;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class OrderAfterSaleFragment extends BaseFragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "store";
    private static final String ARG_STATUS = "status";
    private Store mStore;
    private String mStatus;

    private View rootView;
    private TextView mOldPrice, mTitleView;
    private EditText mEditText;
    private RequestQueue requestQueue;
    private ListView mListView;
    private GridView mImgGrid;
    private ImageAdapter imageAdapter;
    private final int UPDATE_RETURN_LIST = 1;
    private AfterSaleAdapter afterSaleAdapter;
    private TextView tv_return, tv_exchange, lastSelected, tv_commit;
    List<OrderItem1> orderItems = new ArrayList<OrderItem1>();
    private List<OrderItem1> returnItem = new ArrayList<OrderItem1>();
    private PhotoMenuFragment menuFragment;
    private FrameLayout mMenuContainer;
    boolean isFromCamera = false;// 区分拍照旋转
    int degree = 0;
    private ImageView iv_return_img;
    private final int MENU_CAMERA_SELECTED = 0;
    private final int MENU_PHOTO_SELECTED = 1;
    private final int UPDATE_UPLOAD_IMG_GRID = 2;
    private final int ORDER_RETURN_SUCCESS = 3;

    public String filePath = "";
    private String token;
    private Context mContext;
    private List<String> imagePath = new ArrayList<String>();
    private Order.OrderListener orderListener;
    private DialogUtils mDialogUtils;
    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_RETURN_LIST:
                    afterSaleAdapter.notifyDataSetChanged();
                    break;
                case UPDATE_UPLOAD_IMG_GRID:
                    if (imagePath.size() >= 4)
                        imagePath.remove("");
                    imageAdapter.notifyDataSetChanged();
                    break;
                case ORDER_RETURN_SUCCESS:
                    Order order = (Order) msg.obj;
                    if (orderListener != null)
                        orderListener.onOrderStatusChange(order);
                    ToolUtils.setToast(getActivity(), "恭喜,申请退货成功");
                    ((BaseActivity) getActivity()).popToStack(OrderAfterSaleFragment.this);
                    break;
            }
        }
    };

    public static OrderAfterSaleFragment newInstance(Store store, String status) {
        OrderAfterSaleFragment fragment = new OrderAfterSaleFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, store);
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    public OrderAfterSaleFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStore = (Store) getArguments().getSerializable(ARG_PARAM1);
            mStatus = getArguments().getString(ARG_STATUS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(R.layout.fragment_after_sale, container, false);
            initView(rootView);// 控件初始化
        }
        return rootView;
    }

    private void initView(View view) {
        mContext = getActivity();
        mDialogUtils = new DialogUtils(mContext);
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        tv_commit = (TextView) view.findViewById(R.id.tv_commit);
        tv_commit.setOnClickListener(this);
        mTitleView = (TextView) view.findViewById(R.id.tv_title);
        mEditText = (EditText) view.findViewById(R.id.et_msg);
        mOldPrice = (TextView) view.findViewById(R.id.tv_outdated);
        mImgGrid = (GridView) view.findViewById(R.id.gv_img);
        mMenuContainer = (FrameLayout) view.findViewById(R.id.rl_header_menu);
        TextPaint textPaint = mOldPrice.getPaint();
        textPaint.setAntiAlias(true);
        textPaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        requestQueue = Volley.newRequestQueue(getActivity());
        mListView = (ListView) view.findViewById(R.id.lv_aftersale);
        afterSaleAdapter = new AfterSaleAdapter(getActivity(), new ArrayList<OrderItem1>());
        mListView.setAdapter(afterSaleAdapter);
        FetchOrderDetail(mStore);

        iv_return_img = (ImageView) view.findViewById(R.id.iv_return_img);
        iv_return_img.setOnClickListener(this);
        menuFragment = PhotoMenuFragment.newInstance("", "");
        if (menuFragment != null)
            getChildFragmentManager().beginTransaction().replace(R.id.rl_header_menu, menuFragment).addToBackStack("").hide(menuFragment).commit();
        imagePath.add("");
        imageAdapter = new ImageAdapter(getActivity(), imagePath);
        mImgGrid.setAdapter(imageAdapter);

        afterSaleAdapter.setOnInViewClickListener(R.id.cb_return, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                CheckBox checkBox = (CheckBox) v;
                OrderItem1 item = (OrderItem1) values;
                if (checkBox.isChecked()) {
                    returnItem.add(item);
                } else {
                    returnItem.remove(item);
                }
            }
        });

        menuFragment.setMenuSelectListener(new PhotoMenuFragment.MenuSelectListener() {
            @Override
            public void onMenuSelect(int position, String tag) {
                switch (position) {
                    case MENU_CAMERA_SELECTED:
                        File dir = new File(ZhaiDou.MyAvatarDir);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        // 原图
                        File file = new File(dir, new SimpleDateFormat("yyMMddHHmmss")
                                .format(new Date()));
                        filePath = file.getAbsolutePath();// 获取相片的保存路径
                        Uri imageUri = Uri.fromFile(file);

                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(intent,
                                MENU_CAMERA_SELECTED);
                        break;
                    case MENU_PHOTO_SELECTED:
                        Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                        intent1.setDataAndType(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                        startActivityForResult(intent1,
                                MENU_PHOTO_SELECTED);
                        break;
                }
                toggleMenu();
            }
        });


        imageAdapter.setOnInViewClickListener(R.id.iv_img, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                final String imgPath = (String) values;
                final ImageView imageView = (ImageView) v;
                if (TextUtils.isEmpty(imgPath)) {
                    mMenuContainer.setVisibility(View.VISIBLE);
                    toggleMenu();
                } else {
                    PhotoViewFragment photoViewFragment = PhotoViewFragment.newInstance(position, imgPath);
                    ((MainActivity) getActivity()).navigationToFragment(photoViewFragment);
                    photoViewFragment.setPhotoListener(new PhotoViewFragment.PhotoListener() {
                        @Override
                        public void onPhotoDelete(int position, String url) {
                            mHashMap.clear();
                            if (!TextUtils.isEmpty(url)) {
                                imagePath.remove(position);
                                if (imagePath.size() < 3 && !imagePath.contains("")) {
                                    imagePath.add("");
                                }
                                imageAdapter.notifyDataSetChanged();
                                if (position == imagePath.size() - 1)
                                    imageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_add));
                            }
                        }
                    });
                }
            }
        });
    }

    private void FetchOrderDetail(Store mStore) {
        Map<String,String> map=new HashMap<String, String>();
        map.put("orderCode",mStore.parentOrderCode);
        map.put("childOrderCode",mStore.orderCode);
        System.out.println("map = " + map);
        ZhaiDouRequest request=new ZhaiDouRequest(mContext, Request.Method.POST,ZhaiDou.ORDER_RETURN_DETAIL,map,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                System.out.println("jsonObject = " + jsonObject);
                int status = jsonObject.optInt("status");
                String message = jsonObject.optString("message");
                if (status==200){
                    JSONArray data = jsonObject.optJSONArray("data");
                    List<OrderItem1> orderItem1s = JSON.parseArray(data.toString(), OrderItem1.class);
                    initData(orderItem1s);
                    return;
                }
                Toast.makeText(mContext,message,Toast.LENGTH_SHORT).show();
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        ((ZDApplication)mContext.getApplicationContext()).mRequestQueue.add(request);
    }

    private void initData(List<OrderItem1> orderItems) {
        afterSaleAdapter.addAll(orderItems);
        afterSaleAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_return_img:
                mMenuContainer.setVisibility(View.VISIBLE);
                toggleMenu();
                break;
            case R.id.tv_commit:
                if (returnItem != null && returnItem.size() == 0) {
                    ShowToast("请选择退货商品");
                    return;
                }
                if (TextUtils.isEmpty(mEditText.getText().toString().trim())) {
                    ShowToast("请填写描述");
                    return;
                }
                if ((imagePath != null && imagePath.size() == 0) || (imagePath != null && imagePath.size() == 1 && TextUtils.isEmpty(imagePath.get(0).toString().trim()))) {
                    ShowToast("请上传图片");
                    return;
                }
                if (NetworkUtils.isNetworkAvailable(mContext)) {
                    applyReturn();
                } else {
                    ShowToast("网络异常");
                }
                break;
        }
    }

    public class AfterSaleAdapter extends BaseListAdapter<OrderItem1> {
        public AfterSaleAdapter(Context context, List<OrderItem1> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_order_return1, null);
            TextView tv_name = ViewHolder.get(convertView, R.id.tv_name);
            TextView tv_specification = ViewHolder.get(convertView, R.id.tv_specification);
            TextView tv_count = ViewHolder.get(convertView, R.id.tv_count);
            ImageView iv_order_img = ViewHolder.get(convertView, R.id.iv_order_img);
            TextView tv_old_price = ViewHolder.get(convertView, R.id.orderItemFormalPrice);
            TextView tv_price = ViewHolder.get(convertView, R.id.orderItemCurrentPrice);
            LinearLayout ll_count = ViewHolder.get(convertView, R.id.ll_count);
            TextView tv_zero_msg = ViewHolder.get(convertView, R.id.tv_zero_msg);
            CheckBox mCheckBox = ViewHolder.get(convertView, R.id.cb_return);
//            TextView mCouponMsg=ViewHolder.get(convertView,R.id.couponMsg);
            TextView mPayMoney=ViewHolder.get(convertView,R.id.payMoney);
            TextView mCouponMoney=ViewHolder.get(convertView,R.id.couponMoney);
            OrderItem1 item = getList().get(position);
            if (item.productType != 2) {
                mCheckBox.setVisibility(View.VISIBLE);
                ll_count.setVisibility(View.VISIBLE);
                tv_zero_msg.setVisibility(View.GONE);
            } else {
                mCheckBox.setVisibility(View.INVISIBLE);
                ll_count.setVisibility(View.GONE);
                tv_zero_msg.setVisibility(View.VISIBLE);
            }
            tv_name.setText(item.productName);
            tv_specification.setText(item.specifications);
            tv_count.setText(item.quantity + "");
            tv_price.setText("￥" + item.price);
            tv_old_price.setText("￥" + item.marketPrice);
            TextPaint textPaint = tv_old_price.getPaint();
            textPaint.setAntiAlias(true);
            textPaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            ToolUtils.setImageCacheUrl(item.pictureMiddleUrl, iv_order_img, R.drawable.icon_loading_defalut);
            mPayMoney.setText("￥"+item.paidAmount);
            mCouponMoney.setText("￥"+item.favorableAmount1+"");
            return convertView;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MENU_CAMERA_SELECTED:// 拍照修改头像
                if (resultCode == getActivity().RESULT_OK) {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(getActivity(), "SD不可用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isFromCamera = true;
                    File file = new File(filePath);
                    degree = PhotoUtil.readPictureDegree(file.getAbsolutePath());
                    if (imagePath != null && !TextUtils.isEmpty(filePath.trim()) && imagePath.size() < 3) {
                        imagePath.remove("");
                        imagePath.add(filePath);
                        imagePath.add("");
                        handler.sendEmptyMessage(UPDATE_UPLOAD_IMG_GRID);
                    }

                }
                break;
            case MENU_PHOTO_SELECTED:// 本地修改头像
                Uri uri = null;
                if (data == null) {
                    return;
                }
                if (resultCode == getActivity().RESULT_OK) {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(getActivity(), "SD不可用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isFromCamera = false;
                    uri = data.getData();

                    try {
                        Bitmap bm = null;
                        ContentResolver resolver = getActivity().getContentResolver();
                        bm = MediaStore.Images.Media.getBitmap(resolver, uri);
                        String[] proj = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getActivity().managedQuery(uri, proj, null, null, null);
                        //按我个人理解 这个是获得用户选择的图片的索引值
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        //将光标移至开头 ，这个很重要，不小心很容易引起越界
                        cursor.moveToFirst();
                        //最后根据索引值获取图片路径
                        String path = cursor.getString(column_index);
                        ToolUtils.setImageCacheUrl("file://" + path, iv_return_img);
                        if (imagePath != null && !TextUtils.isEmpty(path.trim()) && imagePath.size() <= 3) {
                            imagePath.remove("");
                            imagePath.add(path);
                            imagePath.add("");
                            handler.sendEmptyMessage(UPDATE_UPLOAD_IMG_GRID);
                        }
                    } catch (Exception e) {

                    }
                } else {
                    Toast.makeText(getActivity(), "照片获取失败", Toast.LENGTH_SHORT).show();
                }

                break;
            case 2:// 裁剪头像返回
                if (data == null) {
                    Toast.makeText(getActivity(), "取消选择", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    saveCropAvator(data);
                }

                // 初始化文件路径
                filePath = "";
                break;
            default:
                break;
        }
    }

    /**
     * 保存裁剪的头像
     *
     * @param data
     */
    private void saveCropAvator(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap bitmap = extras.getParcelable("data");
            String base64str = PhotoUtil.bitmapToBase64(bitmap);

        }
    }

    public void toggleMenu() {
        if (menuFragment != null) {
            if (menuFragment.isHidden()) {
                getChildFragmentManager().beginTransaction().show(menuFragment).commit();
            } else {
                getChildFragmentManager().beginTransaction().hide(menuFragment).commit();
            }
        }
    }

    public class ImageAdapter extends BaseListAdapter<String> {

        public ImageAdapter(Context context, List<String> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            convertView = mHashMap.get(position);
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_img_grid, null);
            ImageView iv_img = ViewHolder.get(convertView, R.id.iv_img);
            iv_img.setBackgroundDrawable(null);
            String img = getList().get(position);
            if (TextUtils.isEmpty(img)) {
                iv_img.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_add));
            } else {
                ToolUtils.setImageCacheUrl("file://" + img, iv_img);
            }
            mHashMap.put(position, convertView);
            return convertView;
        }
    }

    private void applyReturn() {

        Object id = SharedPreferencesUtil.getData(getActivity(), "userId", 0);
        final Dialog dialog = mDialogUtils.showLoadingDialog();
        List<ReturnItem> items = new ArrayList<ReturnItem>();
        Map<String, String> params = new HashMap<String, String>();
        List<File> files = new ArrayList<File>();
        JSONArray array = new JSONArray(items);
        for (int k = 0; k < returnItem.size(); k++) {
            OrderItem1 item1 = returnItem.get(k);
            int orderItemId = item1.orderItemId;
            int quantity = item1.quantity;
            String remark = mEditText.getText().toString().trim();
            Map<String, String> map = new HashMap<String, String>();
            map.put("orderItemId", orderItemId + "");
            map.put("quantity", quantity + "");
            map.put("remark", remark);
            JSONObject object = new JSONObject(map);
            array.put(object);
        }
        params.put("businessType", "01");
        params.put("clientType", "ANDROID");
        params.put("version", versionName);
        params.put("isHasInvoice", "0");
        params.put("operationType", "1");
        params.put("mallReturnFlowDetailPOList", array.toString());
        params.put("userId", id + "");
        params.put("orderCode", mStore.orderCode);
        Bitmap bitmap;
        for (int i = 0; i < imagePath.size(); i++) {
            String path = imagePath.get(i);
            if (!TextUtils.isEmpty(path)) {
                File file = new File(path);
                String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + System.currentTimeMillis() + file.getName();
                bitmap = BitmapFactory.decodeFile(path, PhotoUtil.getBitmapOption(2));
                File thumbFile = PhotoUtil.saveBitmapFile(bitmap, absolutePath);
                files.add(thumbFile);
            }
        }

        MultipartRequest1 multipartRequest1 = new MultipartRequest1(ZhaiDou.URL_ORDER_RETURN_APPLY, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dialog.dismiss();
                ShowToast("网络错误");
            }
        }, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                dialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    int status = jsonObject.optInt("status");
                    String message = jsonObject.optString("message");
                    if (200 == status) {
                        ShowToast("退货申请成功,请前往退款/退货订单中查看");
                        mStore.returnGoodsFlag=1;
                        if (onReturnSuccess!=null)
                            onReturnSuccess.onSuccess(mStore);
                        ((MainActivity) getActivity()).popToStack(OrderAfterSaleFragment.this);
                    } else {
                        ShowToast(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, "uploadfile", files, params);
        multipartRequest1.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        requestQueue.add(multipartRequest1);

    }

    public void setOrderListener(Order.OrderListener orderListener) {
        this.orderListener = orderListener;
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_after_sale));
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_after_sale));
    }
}

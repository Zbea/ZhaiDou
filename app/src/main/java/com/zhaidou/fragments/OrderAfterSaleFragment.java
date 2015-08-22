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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
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

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Order;
import com.zhaidou.model.OrderItem;
import com.zhaidou.model.Receiver;
import com.zhaidou.utils.CollectionUtils;
import com.zhaidou.utils.PhotoUtil;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class OrderAfterSaleFragment extends BaseFragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_STATUS = "status";
    private String mOrderId;
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
    List<OrderItem> orderItems = new ArrayList<OrderItem>();
    private List<OrderItem> returnItem=new ArrayList<OrderItem>();
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
    private Dialog mDialog;
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
                    ToolUtils.setToast(getActivity(),"恭喜,申请退款成功");
                    ((BaseActivity) getActivity()).popToStack(OrderAfterSaleFragment.this);
                    break;
            }
        }
    };

    public static OrderAfterSaleFragment newInstance(String orderId, String status) {
        OrderAfterSaleFragment fragment = new OrderAfterSaleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, orderId);
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
            mOrderId = getArguments().getString(ARG_PARAM1);
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
        mDialog = CustomLoadingDialog.setLoadingDialog(mContext, "loading");
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        tv_commit = (TextView) view.findViewById(R.id.tv_commit);
        tv_commit.setOnClickListener(this);
        mTitleView = (TextView) view.findViewById(R.id.tv_title);
        mEditText = (EditText) view.findViewById(R.id.et_msg);
        tv_return = (TextView) view.findViewById(R.id.tv_return);
        tv_exchange = (TextView) view.findViewById(R.id.tv_exchange);
        mOldPrice = (TextView) view.findViewById(R.id.tv_outdated);
        mImgGrid = (GridView) view.findViewById(R.id.gv_img);
        mMenuContainer = (FrameLayout) view.findViewById(R.id.rl_header_menu);
        TextPaint textPaint = mOldPrice.getPaint();
        textPaint.setAntiAlias(true);
        textPaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        requestQueue = Volley.newRequestQueue(getActivity());
        mListView = (ListView) view.findViewById(R.id.lv_aftersale);
        afterSaleAdapter = new AfterSaleAdapter(getActivity(), orderItems);
        mListView.setAdapter(afterSaleAdapter);
        FetchOrderDetail(mOrderId);

        tv_exchange.setText("return_money".equalsIgnoreCase(mStatus) ? "退款" : "退货");
        tv_exchange.setOnClickListener(this);
        tv_return.setOnClickListener(this);

        iv_return_img = (ImageView) view.findViewById(R.id.iv_return_img);
        iv_return_img.setOnClickListener(this);
        menuFragment = PhotoMenuFragment.newInstance("", "");
        if (menuFragment != null)
            getChildFragmentManager().beginTransaction().replace(R.id.rl_header_menu, menuFragment).addToBackStack("").hide(menuFragment).commit();
        imagePath.add("");
        imageAdapter = new ImageAdapter(getActivity(), imagePath);
        mImgGrid.setAdapter(imageAdapter);
        if (("" + ZhaiDou.STATUS_PAYED).equalsIgnoreCase(mStatus)) {
            mTitleView.setText(mContext.getResources().getString(R.string.order_return_money));
            tv_exchange.setText("退款");
        } else {
            mTitleView.setText(mContext.getResources().getString(R.string.order_return_good));
            tv_exchange.setText("退货");
        }

        afterSaleAdapter.setOnInViewClickListener(R.id.cb_return,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                CheckBox checkBox=(CheckBox)v;
                OrderItem item=(OrderItem)values;
                if (checkBox.isChecked()){
                    returnItem.add(item);
                }else {
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
                if (TextUtils.isEmpty(imgPath)) {
                    imagePath.remove("");
                    mMenuContainer.setVisibility(View.VISIBLE);
                    toggleMenu();
                } else {
                    PhotoViewFragment photoViewFragment = PhotoViewFragment.newInstance(position, imgPath);
                    ((MainActivity) getActivity()).navigationToFragment(photoViewFragment);
                    photoViewFragment.setPhotoListener(new PhotoViewFragment.PhotoListener() {
                        @Override
                        public void onPhotoDelete(int position, String url) {
                            System.out.println("OrderAfterSaleFragment.onPhotoDelete---->"+position+"---"+url);
                            if (!TextUtils.isEmpty(url)) {
                                imagePath.remove(position);
                                if (imagePath.size() < 3 && !imagePath.contains("")){
                                    System.out.println("OrderAfterSaleFragment.onPhotoDeleteimagePath.size() < 3 && !imagePath.contains");
                                    imagePath.add("");
                                }
                                imageAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_exchange:
                if (lastSelected != null)
                    lastSelected.setSelected(false);
                tv_exchange.setSelected(true);
                lastSelected = tv_exchange;
                break;
            case R.id.tv_return:
                if (lastSelected != null)
                    lastSelected.setSelected(false);
                tv_return.setSelected(true);
                lastSelected = tv_return;
                break;
            case R.id.iv_return_img:
                mMenuContainer.setVisibility(View.VISIBLE);
                toggleMenu();
                break;
            case R.id.tv_commit:
                if (returnItem!=null&&returnItem.size()==0){
                    ShowToast("请选择退货商品");
                    return;
                }
                if (TextUtils.isEmpty(mEditText.getText().toString().trim())){
                    ShowToast("请填写描述");
                    return;
                }
                if (imagePath!=null&&imagePath.size()==1){
                    ShowToast("请上传图片");
                    return;
                }
                new CommitTask().execute();
                break;
        }
    }

    private void FetchOrderDetail(String id) {
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.URL_ORDER_LIST + "/" + id, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                System.out.println("OrderAfterSaleFragment.onResponse-------->"+jsonObject.toString());
                if (mDialog != null) mDialog.dismiss();
                if (jsonObject != null) {
                    JSONObject orderObj = jsonObject.optJSONObject("order");
                    double amount = orderObj.optDouble("amount");
                    int id = orderObj.optInt("id");
                    String status = orderObj.optString("status");
                    String created_at_for = orderObj.optString("created_at_for");
                    String receiver_address = orderObj.optString("receiver_address");
                    String created_at = orderObj.optString("created_at");
                    String status_ch = orderObj.optString("status_ch");
                    String number = orderObj.optString("number");
                    String receiver_phone = orderObj.optString("receiver_phone");
                    String deliver_number = orderObj.optString("deliver_number");
                    String receiver_name = orderObj.optString("receiver_name");

                    JSONObject receiverObj = orderObj.optJSONObject("receiver");
                    int receiverId = receiverObj.optInt("id");
                    String address = receiverObj.optString("address");
                    String phone = receiverObj.optString("phone");
                    String name = receiverObj.optString("name");
                    Receiver receiver = new Receiver(receiverId, address, phone, name);


                    JSONArray order_items = orderObj.optJSONArray("order_items");
//                    List<OrderItem> orderItems=new ArrayList<OrderItem>();
                    if (order_items != null && order_items.length() > 0) {
                        for (int i = 0; i < order_items.length(); i++) {
                            JSONObject item = order_items.optJSONObject(i);
                            int itemId = item.optInt("id");
                            double itemPrice = item.optDouble("price");
                            int count = item.optInt("count");
                            double cost_price = item.optDouble("cost_price");
                            String merchandise = item.optString("merchandise");
                            String specification = item.optString("specification");
                            int merchandise_id = item.optInt("merchandise_id");
                            String merch_img = item.optString("merch_img");
                            int sale_cate = item.optInt("sale_cate");
                            OrderItem orderItem = new OrderItem(itemId, itemPrice, count, cost_price, merchandise, specification, merchandise_id, merch_img);
                            orderItem.setSale_cate(sale_cate);
                            orderItems.add(orderItem);
                        }
                    }
                    Order order = new Order("", id, number, amount, status, status_ch, created_at_for, created_at, receiver, orderItems, receiver_address, receiver_phone, deliver_number, receiver_name);
                    Message message = new Message();
                    message.obj = order;
                    message.what = UPDATE_RETURN_LIST;
                    handler.sendMessage(message);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (mDialog != null) mDialog.dismiss();
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("SECAuthorization", token);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public class AfterSaleAdapter extends BaseListAdapter<OrderItem> {
        public AfterSaleAdapter(Context context, List<OrderItem> list) {
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
            CheckBox mCheckBox=ViewHolder.get(convertView,R.id.cb_return);

            OrderItem item = getList().get(position);
            if (item.getSale_cate() == 0) {
                mCheckBox.setVisibility(View.VISIBLE);
                ll_count.setVisibility(View.VISIBLE);
                tv_zero_msg.setVisibility(View.GONE);
            } else {
                mCheckBox.setVisibility(View.INVISIBLE);
                ll_count.setVisibility(View.GONE);
                tv_zero_msg.setVisibility(View.VISIBLE);
            }
            tv_name.setText(item.getMerchandise());
            tv_specification.setText(item.getSpecification());
            tv_count.setText(item.getCount() + "");
            tv_price.setText("￥" + item.getPrice());
            tv_old_price.setText("￥" + item.getCost_price());
            TextPaint textPaint = tv_old_price.getPaint();
            textPaint.setAntiAlias(true);
            textPaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            ToolUtils.setImageCacheUrl(item.getMerch_img(), iv_order_img);
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
                    Log.i("拍照修改头像------------>", "拍照修改头像");
                    isFromCamera = true;
                    File file = new File(filePath);
                    Log.i("MENU_CAMERA_SELECTED-------------->", filePath + "------->" + imagePath.size());
                    degree = PhotoUtil.readPictureDegree(file.getAbsolutePath());
//                    ToolUtils.setImageCacheUrl("file://" + filePath, iv_return_img);
                    if (imagePath != null && imagePath.size() < 3) {
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
                    Log.i("MENU_PHOTO_SELECTED------------>", uri.getPath());

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
                        Log.i("MENU_PHOTO_SELECTED------------>path", path + "---------------->" + imagePath.size());
                        ToolUtils.setImageCacheUrl("file://" + path, iv_return_img);
                        if (imagePath != null && imagePath.size() < 3) {
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
        private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();

        public ImageAdapter(Context context, List<String> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            System.out.println("ImageAdapter.bindView------->"+imagePath.toString());
            convertView = mHashMap.get(position);
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_img_grid, null);
            ImageView iv_img = ViewHolder.get(convertView, R.id.iv_img);
            iv_img.setBackgroundDrawable(null);
            String img = getList().get(position);
            System.out.println("ImageAdapter.bindView---------->"+img);
            if (TextUtils.isEmpty(img)) {
                System.out.println("TextUtils.isEmpty(img)");
                iv_img.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_add));
            } else {
                ToolUtils.setImageCacheUrl("file://" + img, iv_img);
            }
            mHashMap.put(position, convertView);
            return convertView;
        }
    }

    private class CommitTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            if (mDialog != null)
                mDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = applyReturn();
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            if (mDialog != null)
                mDialog.hide();
            try {
                JSONObject jsonObject = new JSONObject(s);
                int status = jsonObject.optInt("status");
                JSONObject orderObj = jsonObject.optJSONObject("order");
                int id = orderObj.optInt("id");
                String number = orderObj.optString("number");
                double amount = orderObj.optDouble("amount");
                String orderStatus = orderObj.optString("status");
                String merch_img = orderObj.optString("merch_img");
                String status_ch = orderObj.optString("status_ch");
                String created_at = orderObj.optString("created_at");
                String over_at = orderObj.optString("over_at");
                String created_at_for = orderObj.optString("created_at_for");
                String deliver_number = orderObj.optString("deliver_number");
                Order order = new Order(id, number, amount, orderStatus, status_ch, created_at_for, created_at, over_at, 0);
                if (201 == status) {
                    Message message = new Message();
                    message.what = ORDER_RETURN_SUCCESS;
                    message.obj = order;
                    handler.sendMessage(message);
                }
            } catch (Exception e) {

            }
        }
    }

    private String applyReturn() {

        String result = null;
        BufferedReader in = null;
        try {
            // 定义HttpClient
            HttpClient client = new DefaultHttpClient();


            // 实例化HTTP方法
            HttpPost request = new HttpPost(ZhaiDou.URL_ORDER_LIST + "/" + mOrderId + "/return_items");
            request.addHeader("SECAuthorization", token);

            // 创建名/值组列表
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("sale_return_item[return_category_id]", "" + mOrderId));
            params.add(new BasicNameValuePair("sale_return_item[node]", mEditText.getText().toString().trim()));
            for (int i = 0; i < imagePath.size(); i++) {
                Log.i("imagePath---------->", imagePath.get(i).toString());
                String path = imagePath.get(i);
                if (!TextUtils.isEmpty(path)) {
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    String base64Str = PhotoUtil.bitmapToBase64(bitmap);
                    Log.i("base64Str---------->", base64Str);
                    params.add(new BasicNameValuePair("sale_return_item[attachments_attributes][][picture]", "data:image/png;base64," + base64Str));
                }
            }

            for (int k = 0; k < returnItem.size(); k++) {
                params.add(new BasicNameValuePair("sale_return_item[order_item_ids][]", orderItems.get(k).getId() + ""));
            }

            // 创建UrlEncodedFormEntity对象
            UrlEncodedFormEntity formEntiry = new UrlEncodedFormEntity(
                    params, HTTP.UTF_8);
            request.setEntity(formEntiry);
            // 执行请求
            HttpResponse response = client.execute(request);

            in = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();
            result = sb.toString();
            Log.i("result------------>", result.toString());
            return result;

        } catch (Exception e) {

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public void setOrderListener(Order.OrderListener orderListener) {
        this.orderListener = orderListener;
    }
}

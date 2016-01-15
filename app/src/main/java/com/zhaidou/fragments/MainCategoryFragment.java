package com.zhaidou.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.dialog.CustomLoadingDialog;
import com.zhaidou.model.Category;
import com.zhaidou.model.CategoryItem;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全类别
 */
public class MainCategoryFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private long lastClickTime = 0L;

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    List<Category> categoryList = new ArrayList<Category>();

    private RequestQueue mRequestQueue;
    private final int UPDATE_CATEGORY_DATA = 0;

    private Dialog mDialog;
    private ListView mCategoryListView;
    private GridView mGridView;

    private CategoryAdapter mCategoryAdapter;
    private CategoryItemAdapter mCategoryItemAdapter;
    private int mCheckPosition = 0;
    private HashMap<Integer, View> mCategoryView = new HashMap<Integer, View>();
    private HashMap<Integer, View> mCategoryItemView = new HashMap<Integer, View>();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_CATEGORY_DATA:
                    break;
            }
        }
    };

    public static MainCategoryFragment newInstance(String param1, String param2) {
        MainCategoryFragment fragment = new MainCategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MainCategoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        view.findViewById(R.id.searchLayout).setOnClickListener(this);
        mCategoryListView = (ListView) view.findViewById(R.id.category);
        mGridView = (GridView) view.findViewById(R.id.categoryItem);
        mCategoryAdapter = new CategoryAdapter(getActivity(), categoryList);
        mCategoryListView.setAdapter(mCategoryAdapter);
        mCategoryItemAdapter = new CategoryItemAdapter(getActivity(), new ArrayList<CategoryItem>());
        mGridView.setAdapter(mCategoryItemAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                SearchFragment searchFragment = SearchFragment.newInstance(mCategoryItemAdapter.getList().get(position).categoryId, 2);
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(searchFragment);
            }
        });
        mRequestQueue = Volley.newRequestQueue(getActivity());
        getCategoryData();
        mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(), "loading");
        mCategoryAdapter.setOnInViewClickListener(R.id.categoryView, new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                mCheckPosition = position;
                mCategoryAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }


    private void getCategoryData() {
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.HomeCategoryUrl, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (mDialog != null)
                    mDialog.dismiss();
                int status = jsonObject.optInt("status");
                String message = jsonObject.optString("message");
                if (200 == status) {
                    JSONArray children = jsonObject.optJSONObject("data").optJSONArray("children");
                    List<Category> mCategoryList = JSON.parseArray(children.toString(), Category.class);
                    categoryList.addAll(mCategoryList);
                    mCategoryAdapter.notifyDataSetChanged();
                } else {
                    ShowToast(message);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (mDialog != null)
                    mDialog.dismiss();
                ToolUtils.setToast(mContext, "加载失败");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.searchLayout:
                SearchFragment searchFragment = SearchFragment.newInstance("", 1);
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(searchFragment);
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (categoryList == null | categoryList.size() < 1) {
                getCategoryData();
            }
        }

    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_all_category)); //统计页面
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_all_category));
    }

    public class CategoryAdapter extends BaseListAdapter<Category> {
        public CategoryAdapter(Context context, List<Category> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
//            convertView= mCategoryView.get(position);
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_category_list, null);
            TextView tv_item = ViewHolder.get(convertView, R.id.categoryView);
            System.out.println("position = [" + position + "], convertView = [" + convertView + "], parent = [" + parent + "]");
            Category category = getList().get(position);
            tv_item.setText(category.categoryName);
            tv_item.setTextColor(getResources().getColor(mCheckPosition == position ? R.color.green_color : R.color.text_main_color));
            convertView.setBackgroundResource(mCheckPosition == position ? R.drawable.icon_category_list_bg : R.color.base_bg);
            if (mCheckPosition == position) {
                List<CategoryItem> children = category.children;
                mCategoryItemAdapter.setList(children);
                mCategoryItemAdapter.notifyDataSetChanged();
            }
//            mCategoryView.put(position,convertView);
            return convertView;
        }
    }

    public class CategoryItemAdapter extends BaseListAdapter<CategoryItem> {
        public CategoryItemAdapter(Context context, List<CategoryItem> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            convertView = mCategoryItemView.get(position);
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.item_categoty_item_list, null);
            TextView mCategoryName = ViewHolder.get(convertView, R.id.categoryName);
            ImageView mIcon = ViewHolder.get(convertView, R.id.icon);
            CategoryItem categoryItem = getList().get(position);
            mCategoryName.setText(categoryItem.categoryName);
            ToolUtils.setImageCacheUrl(categoryItem.categoryPicUrl, mIcon,R.drawable.icon_loading_category);
            mCategoryItemView.put(position, convertView);
            return convertView;
        }
    }

}

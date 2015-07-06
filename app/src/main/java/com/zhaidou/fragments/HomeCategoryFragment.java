package com.zhaidou.fragments;



import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Article;
import com.zhaidou.model.Category;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeCategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class HomeCategoryFragment extends BaseFragment implements  View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RelativeLayout mRelativeLayout;

    private GridView mGridView;
    private TextView mAllCategory;
    private int mCheckedPosition=-1;
    private List<Category> mCategoryList;
    private RequestQueue mRequestQueue;
    public static String TAG=HomeCategoryFragment.class.getSimpleName();

    private CategorySelectedListener mCategorySelectedListener;
    private CategoryAdapter mCategoryAdapter;
    private static final int UPDATE_CATEGORY_LIST=0;
    private Handler mHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_CATEGORY_LIST:

            }
        }
    };

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeCategoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeCategoryFragment newInstance(String param1, String param2) {
        HomeCategoryFragment fragment = new HomeCategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public HomeCategoryFragment() {
        // Required empty public constructor
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
        // Inflate the layout for this fragment
        Log.i("HomeCategoryFragment--------->","onCreateView");
        View view=inflater.inflate(R.layout.item_popupwindows, container, false);
        mRelativeLayout = (RelativeLayout)view.findViewById(R.id.ll_category_close);
        mAllCategory=(TextView)view.findViewById(R.id.tv_category_all);
        mAllCategory.setPressed(true);
        mRelativeLayout.setOnClickListener(this);
        mRequestQueue = Volley.newRequestQueue(getActivity());
        mCategoryList=new ArrayList<Category>();
        mCategoryAdapter = new CategoryAdapter(getActivity(),mCategoryList);
        mGridView =(GridView)view.findViewById(R.id.gv_category);
        mGridView.setAdapter(mCategoryAdapter);
        FetchCatogoryData();

        mAllCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckedPosition=-1;
                notifyDataSetChanged();
                mCategorySelectedListener.onCategorySelected(null);
            }
        });

        mCategoryAdapter.setOnInViewClickListener(R.id.tv_category_item,new BaseListAdapter.onInternalClickListener() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Object values) {
                Log.i("position--------->",position+"");

                Category category = mCategoryList.get(position);
                Log.i("category------------->",category.toString());
                mCategorySelectedListener.onCategorySelected(category);
                mCheckedPosition=position;
            }
        });
        return view;
    }

    private void FetchCatogoryData(){
        JsonObjectRequest jr = new JsonObjectRequest(ZhaiDou.INDEX_CATEGORY_FILTER,null,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
//                Log.i("FetchCatogoryData", response.toString());
                JSONArray categoryArray = response.optJSONArray("article_categories");

                for (int i=0;i<categoryArray.length();i++){
                    JSONObject categoryObj=categoryArray.optJSONObject(i);
                    int id = categoryObj.optInt("id");
                    String name = categoryObj.optString("name");
                    Log.i("id----->",id+"");
                    Log.i("name----->",name);
                    Category c =new Category(id,name);
                    mCategoryList.add(c);
                }
                mHandler.sendEmptyMessage(UPDATE_CATEGORY_LIST);
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.i("onErrorResponse------->",error.getMessage());
            }
        });
        mRequestQueue.add(jr);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_category_close:
                ((HomeFragment)getParentFragment()).toggleMenu();
                break;
        }
    }

    public class CategoryAdapter extends BaseListAdapter<Category> {
        public CategoryAdapter(Context context, List<Category> list) {
            super(context, list);
        }

        @Override
        public View bindView(int position, View convertView, ViewGroup parent) {
            if (convertView==null)
                convertView=mInflater.inflate(R.layout.category_item_gv,null);
            TextView tv_item = ViewHolder.get(convertView, R.id.tv_category_item);
            Category item = getList().get(position);
            if (mCheckedPosition==position){
                tv_item.setPressed(true);
            }else {
                tv_item.setPressed(false);
//                tv_item.setBackgroundDrawable(getResources().getDrawable(R.drawable.category_item_selector));
            }
            tv_item.setText(item.getName());

            return convertView;
        }
    }

    public void setCategorySelectedListener(CategorySelectedListener mCategorySelectedListener) {
        this.mCategorySelectedListener = mCategorySelectedListener;
    }

    public interface CategorySelectedListener{
        public void onCategorySelected(Category category);
    }

    public void notifyDataSetChanged(){
        if (mCheckedPosition==-1){
            mAllCategory.setPressed(true);
            mCategoryAdapter.notifyDataSetChanged();
        }else if (mCategoryAdapter!=null&&mCheckedPosition!=-1){
            mAllCategory.setPressed(false);
            mCategoryAdapter.notifyDataSetChanged();
        }

    }
}

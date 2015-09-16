package com.zhaidou.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Category;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class HomeCategoryFragment extends BaseFragment implements  View.OnClickListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private static final int UPDATE_CATEGORY_LIST=0;
    private static final int UPDATE_EMPTY_LIST=1;

    private RelativeLayout mRelativeLayout;
    private View view;

    private GridView mGridView;
    private TextView mAllCategory;
    private int mCheckedPosition=-1;
    private List<Category> mCategoryList=new ArrayList<Category>();;
    private RequestQueue mRequestQueue;
    public static String TAG=HomeCategoryFragment.class.getSimpleName();

    private CategorySelectedListener mCategorySelectedListener;
    private CategoryAdapter mCategoryAdapter;
    private ImageView refreshBtn;
    private Animation animation;


    private Handler mHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_CATEGORY_LIST:
                    if (animation!=null)
                    animation.cancel();
                    mCategoryAdapter.notifyDataSetChanged();
                    break;
                case UPDATE_EMPTY_LIST:
                    animation.cancel();
                    break;
            }
        }
    };


    public static HomeCategoryFragment newInstance(String param1, String param2) {
        HomeCategoryFragment fragment = new HomeCategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public HomeCategoryFragment() {
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

        if(view==null)
        {
            view=inflater.inflate(R.layout.item_popupwindows, container, false);
            mContext = getActivity();
            initView();

        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null)
        {
            parent.removeView(view);
        }
        return view;
    }

    private void initView()
    {
        mRelativeLayout = (RelativeLayout)view.findViewById(R.id.ll_category_close);
        mAllCategory=(TextView)view.findViewById(R.id.tv_category_all);
        mAllCategory.setPressed(true);
        mRelativeLayout.setOnClickListener(this);
        mRequestQueue = ZDApplication.mRequestQueue;

        refreshBtn= (ImageView)view.findViewById(R.id.categoryRefresh);
        refreshBtn.setOnClickListener(this);

        mCategoryAdapter = new CategoryAdapter(getActivity(),mCategoryList);
        mGridView =(GridView)view.findViewById(R.id.gv_category);
        mGridView.setAdapter(mCategoryAdapter);

        animation= AnimationUtils.loadAnimation(mContext,R.anim.dialog_rotate);
        refreshBtn.setAnimation(animation);
        animation.start();

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
                Category category = mCategoryList.get(position);
                mCategorySelectedListener.onCategorySelected(category);
                mCheckedPosition=position;

            }
        });

        FetchCategoryData();
    }

    private void FetchCategoryData(){
        JsonObjectRequest jr = new JsonObjectRequest(ZhaiDou.INDEX_CATEGORY_FILTER,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                mCategoryList.clear();
                JSONArray categoryArray = response.optJSONArray("article_categories");
                for (int i=0;i<categoryArray.length();i++){
                    JSONObject categoryObj=categoryArray.optJSONObject(i);
                    int id = categoryObj.optInt("id");
                    String name = categoryObj.optString("name");
                    Category c =new Category(id,name);
                    mCategoryList.add(c);
                }
                mHandler.sendEmptyMessage(UPDATE_CATEGORY_LIST);
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mHandler.sendEmptyMessage(UPDATE_EMPTY_LIST);
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
            case R.id.categoryRefresh:
                animation.start();
                FetchCategoryData();
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
            }
            tv_item.setText(item.getName());

            mAllCategory.setWidth(tv_item.getMeasuredWidth());
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

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden&&mCategoryList.size()==0){
//            FetchCategoryData();
        }
    }

    @Override
    public void onResume() {
//        view.findViewById(R.id.ll_popup).setAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_in_from_top));
        super.onResume();
    }
}

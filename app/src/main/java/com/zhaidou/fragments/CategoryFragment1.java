package com.zhaidou.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.CategoriesActivity;
import com.zhaidou.activities.CategoryActivity;
import com.zhaidou.activities.HomeActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Article;
import com.zhaidou.model.Category;
import com.zhaidou.model.CategoryItem;
import com.zhaidou.utils.AsyncImageLoader1;
import com.zhaidou.view.ChildGridView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.zhaidou.fragments.CategoryFragment1.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link com.zhaidou.fragments.CategoryFragment1#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CategoryFragment1 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private long lastClickTime = 0L;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    List<Category> categoryList = new ArrayList<Category>();

    private RequestQueue mRequestQueue;
    private AsyncImageLoader1 imageLoader;
    private CategoryExpandeAdapter categoryExpandeAdapter;
    private ExpandableListView expandableListView;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // 设置默认展开
            int groupCount = expandableListView.getCount();
            for (int i = 0; i < groupCount; i++) {
                expandableListView.expandGroup(i);
            }
            categoryExpandeAdapter.notifyDataSetChanged();
        }
    };

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CategoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CategoryFragment1 newInstance(String param1, String param2) {
        CategoryFragment1 fragment = new CategoryFragment1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public CategoryFragment1() {
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
        View view=inflater.inflate(R.layout.fragment_category1, container, false);
        mRequestQueue = Volley.newRequestQueue(getActivity());
        getCategoryData();
        expandableListView=(ExpandableListView)view.findViewById(R.id.el_category);
        categoryExpandeAdapter=new CategoryExpandeAdapter(getActivity(),categoryList);
        expandableListView.setAdapter(categoryExpandeAdapter);


        // 子类点击事件
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Log.i("onChildClick------------->","onChildClick");
                return true;
            }
        });
        // 父类点击事件
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return true;
            }
        });
        // 设置点击背景色
//        expandableListView.setSelector(getResources().getColor(R.color.base_bg));
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }


    private void getCategoryData(){

        String url=ZhaiDou.CATEGORY_ITEM_URL;
        JsonObjectRequest request =new JsonObjectRequest(url,new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonObject) {
//                Log.i("getCategoryData-------------->",jsonObject.toString());
                Category category=null;
                JSONArray categoryArr = jsonObject.optJSONArray("item_categories");
                for (int i=0;i<categoryArr.length();i++){
                    JSONObject categoryJson = categoryArr.optJSONObject(i);
                    int id =categoryJson.optInt("id");
                    String name = categoryJson.optString("name");
                    JSONObject avatar = categoryJson.optJSONObject("avatar").optJSONObject("avatar");
                    String url = avatar.optString("url");
                    String thumb =avatar.optJSONObject("thumb").optString("url");

                    JSONArray children = categoryJson.optJSONArray("children");

                    CategoryItem childItem = null;
                    List<CategoryItem> childList = new ArrayList<CategoryItem>();
                    for (int k=0;k<children.length();k++){
                        JSONObject child = children.optJSONObject(k);
                        int childId =child.optInt("id");
                        int parentId = child.optInt("parent_id");
                        int lft=child.optInt("lft");
                        int rgt=child.optInt("rgt");
                        String childName =child.optString("name");
                        JSONObject childAvatar = child.optJSONObject("avatar");
                        String childUrl = childAvatar.optString("url");
                        String childThumb = childAvatar.optJSONObject("thumb").optString("url");
                        int childLevel =child.optInt("level");
//                        Log.i("childId------->",childId+"");
//                        Log.i("parentId------->",parentId+"");
//                        Log.i("lft------->",lft+"");
//                        Log.i("rgt------->",rgt+"");
//                        Log.i("childName------->",childName+"");
//                        Log.i("childUrl------->",childUrl+"");
//                        Log.i("childThumb------->",childThumb+"");
                        childItem = new CategoryItem(childId,parentId,lft,rgt,childName,childUrl,childThumb,childLevel);
                        childList.add(childItem);
                    }

                    category = new Category(id,name,url,thumb,childList);
                    if (id==26){
                        categoryList.add(0,category);
                    }else {
                        categoryList.add(category);
                    }

                    mHandler.sendEmptyMessage(0);
                }
                Log.i("categoryList------------->",categoryList.size()+"");
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        mRequestQueue.add(request);
    }

    private class CategoryExpandeAdapter extends BaseExpandableListAdapter{
        private Context context;
        private List<Category> mCatrgoryList;

        public CategoryExpandeAdapter(Context context, List<Category> categories) {
            this.context = context;
            mCatrgoryList=categories;
            imageLoader=new AsyncImageLoader1(context);
        }
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return categoryList.get(groupPosition).getCategoryItems().get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView==null)
                convertView=LayoutInflater.from(context).inflate(R.layout.fragment_category_child_grid,null);
            ChildGridView gridView = ViewHolder.get(convertView,R.id.gv_category_child);
            Log.i("gridView--->getChildView",gridView.toString());

            List<CategoryItem> categoryItems = categoryList.get(groupPosition).getCategoryItems();
            ChildAdapter childAdapter =new ChildAdapter(context,categoryItems,gridView);
            gridView.setAdapter(childAdapter);

            childAdapter.setOnInViewClickListener(R.id.rl_grid_category,new BaseListAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position, Object values) {
                    Log.i("position----->",position+"");
                    Log.i("values----->",values+"");
                    CategoryItem item = (CategoryItem)values;
                    Log.i("CategoryItem item----------------->",item.getId()+"");
                    Intent intent = new Intent(getActivity(),CategoryActivity.class);
                    intent.putExtra("id",item.getId());
                    intent.putExtra("title",item.getName());
                    startActivity(intent);
                }
            });

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return categoryList.get(groupPosition);
        }
        @Override
        public int getGroupCount() {
            return categoryList.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            if (convertView==null)
               convertView = LayoutInflater.from(context).inflate(R.layout.fragment_category_group_item,
                    null);
            TextView textview = ViewHolder.get(convertView,R.id.tv_category_group_name);
            textview.setText(categoryList.get(groupPosition).getName());
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    private class ChildAdapter extends BaseListAdapter<CategoryItem>{

        private WeakHashMap<Integer,View> weakHashMap = new WeakHashMap<Integer, View>();
        private ChildGridView gridView;
        public ChildAdapter(Context context, List<CategoryItem> list,ChildGridView gridView) {
            super(context, list);
            this.gridView=gridView;
        }

        @Override
        public View bindView(int position, View view, ViewGroup parent) {
            view =weakHashMap.get(position);
            if (view==null){
                view=mInflater.inflate(R.layout.fragment_category_child_item,null);
                AbsListView.LayoutParams param = new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,1080/4);
                view.setLayoutParams(param);
                view.setBackgroundResource(R.drawable.grid_category_selector);
            }

            ImageView imageView =ViewHolder.get(view,R.id.iv_category_item);
//            Log.i("imageView------->",imageView.toString());
            TextView textView=ViewHolder.get(view,R.id.tv_category_name);
            CategoryItem item = getList().get(position);

//            Log.i("item.getThumb()-------->",item.getThumb());
//            Log.i("item.getName()-------->",item.getName());
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();

            layoutParams.height=80;
            layoutParams.width=80;
            imageView.setLayoutParams(layoutParams);
            imageLoader.LoadImage("http://"+item.getThumb(),imageView);
//            imageLoader.LoadSmallImage("http://"+item.getThumb(),imageView);
            textView.setText(item.getName());

            weakHashMap.put(position,view);
            return view;
        }
    }
}

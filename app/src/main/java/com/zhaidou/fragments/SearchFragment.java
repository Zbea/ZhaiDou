package com.zhaidou.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.viewpagerindicator.TabPageIndicator;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.adapter.SearchAdapter;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.view.AutoGridView;
import com.zhaidou.view.CustomEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by roy on 15/10/9.
 */
public class SearchFragment extends BaseFragment
{
    private static final String DATA = "page";
    private static final String INDEX = "index";
    private View mView;
    private String mPage="";
    private int mIndex;
    private Context mContext;
    private GridView gv_hot;
    private CustomEditText mEditText;
    private ImageView mSearchiv;
    private TextView mDeleteView, mSearchView;
    private ViewPager mViewPager;
    private LinearLayout ll_viewpager;
    private LinearLayout mBackView, mSearchLayout;
    private ImageView mSortView;
    private TabPageIndicator indicator;

    private SearchAdapter mHotAdapter;
    private List<String> list;
    private List<Fragment> mFragments;
    private SearchFragmentAdapter mSearchFragmentAdapter;
    private SharedPreferences mSharedPreferences;
    InputMethodManager inputMethodManager;

    private final int UPDATE_CONTENT = 0;
    private final int UPDATE_HOTDATA = 1;
    private final int UPDATE_HISTORY = 3;

    private List<String> mHotList = new ArrayList<String>();
    private Set<String> mHistorys;
    private List<String> mHistoryList = new ArrayList<String>();
    private int historyCount = 0;
    private String search_Str;

    private SearchSortFragment mSearchSortFragment;

    private GoodsSingleListFragment mSpecialGoodsFragment;
    private GoodsSingleListFragment mtaobaoGoodsFragment;

    private boolean isHidenKeyBoard = false;

    private int sort = 0;
    private AutoGridView autoGridView;
    private RequestQueue mRequestQueue;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_CONTENT:
                    if (mSpecialGoodsFragment==null)
                    {
                        mSpecialGoodsFragment = GoodsSingleListFragment.newInstance(search_Str, "goods", mIndex);
                        mFragments.add(mSpecialGoodsFragment);
                    }
                    else
                    {
                        if (mIndex==1)
                        {
                            mSpecialGoodsFragment.FetchSpecialData(search_Str, sort, 1);
                        }
                            else
                        {
                            mSpecialGoodsFragment.FetchSpecialIdData(search_Str, sort, 1);
                        }
                    }
//                    if (mFragments.size()<2){
//                    mSpecialGoodsFragment = GoodsSingleListFragment.newInstance(text, "goods", 1);
//                        mtaobaoGoodsFragment= GoodsSingleListFragment.newInstance(text, text,2);
//                        mStrategyFragment= SearchArticleListFragment.newInstance(text, text);
//                    mFragments.add(mSpecialGoodsFragment);
//                        mFragments.add(mtaobaoGoodsFragment);
//                        mFragments.add(mStrategyFragment);
//                    }else if (mFragments.size()==2){
//                        mSpecialGoodsFragment.FetchSpecialData(text, sort, 1);
//                        mtaobaoGoodsFragment.FetchData(text,sort,1);
//                    }
                    mSearchFragmentAdapter.notifyDataSetChanged();
                    indicator.notifyDataSetChanged();
                    ll_viewpager.setVisibility(View.VISIBLE);
                    break;
                case UPDATE_HOTDATA:
                    mHotAdapter.setList(mHotList);
                    break;
                case UPDATE_HISTORY:
                    autoGridView.setHistoryList(mHistoryList);
                    break;
                default:
                    break;
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.iv_search:
                    mIndex=1;
                    onSearch();
                    break;
                case R.id.tv_delete:
                    mSearchLayout.setVisibility(View.GONE);
                    autoGridView.clear();
                    SharedPreferencesUtil.clearSearchHistory(mContext);
                    break;
                case R.id.tv_cancel:
                    if (!TextUtils.isEmpty(search_Str))
                    {
                        if (inputMethodManager.isActive())
                            inputMethodManager.hideSoftInputFromWindow(((BaseActivity) mContext).getWindow().peekDecorView().getApplicationWindowToken(), 0);
                        mIndex=1;
                        onSearch();
                    } else
                    {
                        mEditText.setShakeAnimation();
                        Toast.makeText(mContext, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.ll_back:
                    ((BaseActivity) getActivity()).popToStack(SearchFragment.this);
                    break;
                case R.id.iv_sort:
                    toggleSortMenu();
                    break;
                default:
                    break;
            }
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Adapter adapter = parent.getAdapter();
            String item = (String) adapter.getItem(position);
            mEditText.setText(item);
            onSearch();
        }
    };

    private SearchSortFragment.RefreshDataListener refreshDataListener = new SearchSortFragment.RefreshDataListener()
    {
        @Override
        public void refreshData(int index)
        {
            sort = index;
            mViewPager.setFocusable(true);
            int page = mViewPager.getCurrentItem();
            if (page == 0)
            {
                if (mIndex==1)
                {
                    mSpecialGoodsFragment.FetchSpecialData(search_Str, index, 1);
                }
                else
                {
                    mSpecialGoodsFragment.FetchSpecialIdData(search_Str, 0, 1);
                }

            } else if (page == 1)
            {
                mtaobaoGoodsFragment.FetchData(search_Str, index, 1);
            }
        }
    };

    private AutoGridView.OnHistoryItemClickListener onHistoryItemClickListener = new AutoGridView.OnHistoryItemClickListener()
    {
        @Override
        public void onHistoryItemClick(int position, String history)
        {
            mEditText.setText(history);
            onSearch();
        }
    };

    public static SearchFragment newInstance(String page, int index)
    {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATA, page);
        args.putSerializable(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public SearchFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mPage = mPage+getArguments().getString(DATA);
            mIndex = getArguments().getInt(INDEX);//1为普通搜索2为分类搜索
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        if (mView == null)
        {
            mView = inflater.inflate(R.layout.activity_search, container, false);
            mContext = getActivity();
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null)
        {
            parent.removeView(mView);
        }
        return mView;
    }

    private void initView()
    {
        mEditText = (CustomEditText) mView.findViewById(R.id.et_search);
        mEditText.setHint(mPage.length()>0?mPage:"搜索");
        search_Str=mPage;
        mSearchiv = (ImageView) mView.findViewById(R.id.iv_search);
        mDeleteView = (TextView) mView.findViewById(R.id.tv_delete);
        mSearchView = (TextView) mView.findViewById(R.id.tv_cancel);
        mBackView = (LinearLayout) mView.findViewById(R.id.ll_back);
        mSortView = (ImageView) mView.findViewById(R.id.iv_sort);
        mSearchLayout = (LinearLayout) mView.findViewById(R.id.ll_history);
        indicator = (TabPageIndicator) mView.findViewById(R.id.indicator);
        mViewPager = (ViewPager) mView.findViewById(R.id.vp_search);
        ll_viewpager = (LinearLayout) mView.findViewById(R.id.ll_viewpager);
        mSharedPreferences = mContext.getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        mHistorys = mSharedPreferences.getStringSet("history", new LinkedHashSet<String>());
        historyCount = (Integer) SharedPreferencesUtil.getData(mContext, "historyCount", 0);
        mSearchLayout.setVisibility(View.GONE);
        if (historyCount != 0)
        {
            for (int i = 0; i < historyCount; i++)
            {
                String history = (String) SharedPreferencesUtil.getData(mContext, "history_" + i, "");
                if (!TextUtils.isEmpty(history))
                {
                    mHistoryList.add(history);
                }
            }
            mSearchLayout.setVisibility(View.VISIBLE);
        }

        autoGridView = (AutoGridView) mView.findViewById(R.id.ag_search_history);
        autoGridView.setOnHistoryItemClickListener(onHistoryItemClickListener);
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mHandler.sendEmptyMessage(UPDATE_HISTORY);
            }
        }, 500);

        mFragments = new ArrayList<Fragment>();
        mSearchFragmentAdapter = new SearchFragmentAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mSearchFragmentAdapter);
        indicator.setViewPager(mViewPager);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int i, float v, int i2)
            {
            }

            @Override
            public void onPageSelected(int i)
            {
                indicator.setCurrentItem(i);
            }

            @Override
            public void onPageScrollStateChanged(int i)
            {
            }
        });

        mSearchiv.setOnClickListener(onClickListener);
        mDeleteView.setOnClickListener(onClickListener);
        mSearchView.setOnClickListener(onClickListener);
        mBackView.setOnClickListener(onClickListener);
        mSortView.setOnClickListener(onClickListener);

        gv_hot = (GridView) mView.findViewById(R.id.gv_hot_search);
        mHotAdapter = new SearchAdapter(mContext, mHotList);
        gv_hot.setAdapter(mHotAdapter);
        gv_hot.setOnItemClickListener(onItemClickListener);
        inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputMethodManager.isActive())
            inputMethodManager.hideSoftInputFromWindow(((BaseActivity) mContext).getWindow().peekDecorView().getApplicationWindowToken(), 0);
        mEditText.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent)
            {
                if (i == KeyEvent.KEYCODE_ENTER)
                {
                    if (isHidenKeyBoard = !isHidenKeyBoard)
                    {
                        if (!TextUtils.isEmpty(mEditText.getText().toString().trim()))
                        {
                            mIndex=1;
                            onSearch();
                        } else
                        {
                            mEditText.setShakeAnimation();
                        }
                    }

                    return true;
                }
                return false;
            }
        });
        mEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {
                search_Str=charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                mSortView.setVisibility(View.GONE);
                mSearchView.setVisibility(View.VISIBLE);
            }
        });

        mRequestQueue = Volley.newRequestQueue(mContext);
        if (mIndex==1)
        {
            FetchHotsData();
        }
        else
        {
            onSearch();
        }
        if (mSearchSortFragment == null)
            mSearchSortFragment = SearchSortFragment.newInstance("", 0);
        mSearchSortFragment.setRefreshDataListener(refreshDataListener);
        getChildFragmentManager().beginTransaction().add(R.id.rl_sort, mSearchSortFragment, SearchSortFragment.TAG)
                .hide(mSearchSortFragment).commit();

    }

    private void onSearch()
    {
        mBackView.setVisibility(View.VISIBLE);
        if (mIndex==1)
        {
            mSortView.setVisibility(View.VISIBLE);
            mSearchView.setVisibility(View.GONE);
            if (mHistoryList.contains(search_Str))
            {
                mHistoryList.remove(search_Str);
            }
            mHistoryList.add(search_Str);
            SharedPreferencesUtil.saveHistoryData(mContext, mHistoryList);
        }
        else
        {
            mSortView.setVisibility(View.GONE);
            mSearchView.setVisibility(View.VISIBLE);
            search_Str=mPage;
        }
        mHandler.sendEmptyMessage(UPDATE_CONTENT);
    }

    public void FetchHotsData()
    {
        JsonObjectRequest newMissRequest = new JsonObjectRequest(ZhaiDou.SearchHotUrl, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject json)
            {
                if (json != null)
                {
                    JSONArray array = json.optJSONArray("data");
                    if(array!=null)
                    for (int i = 0; i < array.length(); i++)
                    {
                        mHotList.add(array.optString(i));
                    }
                    mHandler.sendEmptyMessage(UPDATE_HOTDATA);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        if (mRequestQueue == null) mRequestQueue = Volley.newRequestQueue(mContext);
        mRequestQueue.add(newMissRequest);
    }


    /**
     * 切换到淘宝页面
     */
    public void cutTaobaoGoods()
    {
        indicator.setCurrentItem(1);
    }

    private class SearchFragmentAdapter extends FragmentPagerAdapter
    {
        public SearchFragmentAdapter(FragmentManager fragmentManager)
        {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int i)
        {
            return mFragments.get(i);
        }

        @Override
        public int getCount()
        {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return "特卖单品";
//            if (position == 0)
//            {
//
//            } else
//            {
//                return "淘宝单品";
//            }
//            else
//            {
//                return "攻略";
//            }
        }

        ;
    }

    public void toggleSortMenu()
    {
        mViewPager.setOnTouchListener(null);
        if (mSearchSortFragment.isHidden())
        {
            mSearchSortFragment.setData(mViewPager.getCurrentItem(), 0);
            getChildFragmentManager().beginTransaction().show(mSearchSortFragment).commit();
        } else
        {
            getChildFragmentManager().beginTransaction().hide(mSearchSortFragment).commit();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("搜索单品、攻略页面");
        MobclickAgent.onResume(mContext);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("搜索单品、攻略页面");
        MobclickAgent.onPause(mContext);
    }
}

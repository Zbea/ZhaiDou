package com.zhaidou.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.TabPageIndicator;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.adapter.SearchAdapter;
import com.zhaidou.fragments.SingleFragment;
import com.zhaidou.fragments.SortFragment;
import com.zhaidou.fragments.StrategyFragment1;
import com.zhaidou.utils.CollectionUtils;
import com.zhaidou.utils.HtmlFetcher;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.view.AutoGridView;

import org.json.JSONArray;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class SearchActivity extends FragmentActivity implements View.OnClickListener,AdapterView.OnItemClickListener,
                              SortFragment.RefreshDataListener,AutoGridView.OnHistoryItemClickListener{

    private GridView gv_hot;
    private EditText mEditText;
    private ImageView mClearView,mSearchiv;
    private TextView mDeleteView,mSearchView;
    private ViewPager mViewPager;
    private LinearLayout ll_viewpager;
    private LinearLayout mBackView,mSearchLayout;
    private ImageView mSortView;
    private TabPageIndicator indicator;

    private SearchAdapter mHotAdapter;
    private SearchAdapter mHistoryAdapter;
    private List<String> list;
    private List<Fragment> mFragments;
    private SearchFragmentAdapter mSearchFragmentAdapter;
    private SharedPreferences mSharedPreferences;
    InputMethodManager inputMethodManager;

    private final int UPDATE_HISTORY=0;
    private final int UPDATE_HOTDATA=1;

    private List<String> mHotList = new ArrayList<String>();
    private Set<String> mHistorys;
    private List<String> mHistoryList=new ArrayList<String>();
    private int historyCount=0;

    private SortFragment mSortFragment;

    private SingleFragment mSingleFragment;
    private StrategyFragment1 mStrategyFragment;

    private boolean isHidenKeyBoard=false;

    private String keyWord;

    private int sort=0;
    private AutoGridView autoGridView;
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_HISTORY:
                    String text = (String)msg.obj;
                    autoGridView.setHistoryList(mHistoryList);
                    if (mFragments.size()<2){
                        Log.i("mFragments.size()<2",mFragments.size()+"");
                        mSingleFragment=SingleFragment.newInstance(text,text);
                        mStrategyFragment=StrategyFragment1.newInstance(text, text);
                        mFragments.add(mSingleFragment);
                        mFragments.add(mStrategyFragment);
                    }else if (mFragments.size()==2){
                        Log.i("mFragments.size()==2",mSingleFragment.toString());
                        mSingleFragment.FetchData(text,sort,1);
                        mStrategyFragment.FetchData(text,sort,1);
                    }

                    mSearchFragmentAdapter.notifyDataSetChanged();
                    indicator.notifyDataSetChanged();
                    ll_viewpager.setVisibility(View.VISIBLE);
                    break;
                case UPDATE_HOTDATA:
                    mHotAdapter.setList(mHotList);
                    autoGridView.setHistoryList(mHistoryList);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initView();
        getHotSearch();
        if (mSortFragment==null)
            mSortFragment=SortFragment.newInstance("",0);
        mSortFragment.setRefreshDataListener(this);
        getSupportFragmentManager().beginTransaction().add(R.id.rl_sort,mSortFragment,SortFragment.TAG)
                .hide(mSortFragment).commit();
    }

    private void initView(){

        gv_hot=(GridView)findViewById(R.id.gv_hot_search);
        mEditText=(EditText)findViewById(R.id.et_search);
        mClearView=(ImageView)findViewById(R.id.iv_cancel);
        mSearchiv=(ImageView)findViewById(R.id.iv_search);
        mDeleteView=(TextView)findViewById(R.id.tv_delete);
        mSearchView=(TextView)findViewById(R.id.tv_cancel);
        mViewPager=(ViewPager)findViewById(R.id.vp_search);
        mBackView=(LinearLayout)findViewById(R.id.ll_back);
        mSortView=(ImageView)findViewById(R.id.iv_sort);
        mSearchLayout=(LinearLayout)findViewById(R.id.ll_history);
        indicator = (TabPageIndicator)findViewById(R.id.indicator);
        ll_viewpager=(LinearLayout)findViewById(R.id.ll_viewpager);
        mSharedPreferences=getSharedPreferences("zhaidou",Context.MODE_PRIVATE);

        autoGridView=(AutoGridView)findViewById(R.id.ag_search_history);
        autoGridView.setOnHistoryItemClickListener(this);
        mHistorys = mSharedPreferences.getStringSet("history",new LinkedHashSet<String>());
        historyCount=(Integer)SharedPreferencesUtil.getData(this,"historyCount",0);
        Log.i("historyCount----->",historyCount+"");
        mSearchLayout.setVisibility(View.GONE);
        if (historyCount!=0){
            for (int i=0;i<historyCount;i++){
                String history=(String)SharedPreferencesUtil.getData(this,"history_"+i,"");
                if (!TextUtils.isEmpty(history)){
                    mHistoryList.add(history);
                }
                Log.i("history------------->",history);
            }
            mSearchLayout.setVisibility(View.VISIBLE);
        }
//        if (mHistorys!=null&&mHistorys.size()>0)
//             mHistoryList.addAll(CollectionUtils.set2list(mHistorys));

        mFragments = new ArrayList<Fragment>();
        mSearchFragmentAdapter=new SearchFragmentAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSearchFragmentAdapter);
        indicator.setViewPager(mViewPager);

        mClearView.setOnClickListener(this);
        mSearchiv.setOnClickListener(this);
        mDeleteView.setOnClickListener(this);
        mSearchView.setOnClickListener(this);
        mBackView.setOnClickListener(this);
        mSortView.setOnClickListener(this);

        mHotAdapter=new SearchAdapter(SearchActivity.this,mHotList);

        Log.i("mHistoryList------>",mHistoryList.size()+"");


        mHistoryAdapter=new SearchAdapter(SearchActivity.this,mHistoryList);
        gv_hot.setAdapter(mHotAdapter);
        inputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputMethodManager.isActive())
            inputMethodManager.hideSoftInputFromWindow(getWindow().peekDecorView().getApplicationWindowToken(),0);
        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i==KeyEvent.KEYCODE_ENTER){

                    if(inputMethodManager.isActive()){
                        inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
                    }
                    if (isHidenKeyBoard=!isHidenKeyBoard){
                        if (!TextUtils.isEmpty(mEditText.getText().toString().trim())){
                            onSearch();
                        }else {
                            Toast.makeText(SearchActivity.this,"还没有输入关键词哦。。。",Toast.LENGTH_SHORT).show();
                        }
                    }

                    return true;
                }
                return false;
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mSortView.setVisibility(View.GONE);
                mSearchView.setVisibility(View.VISIBLE);
            }
        });

        gv_hot.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_cancel:
                mEditText.setText("");
                break;
            case R.id.iv_search:
                Log.i("iv_search--------->","onClick");

                onSearch();

                break;
            case R.id.tv_delete:
//                historyCancel();
                mSearchLayout.setVisibility(View.GONE);
                autoGridView.clear();
                SharedPreferencesUtil.clearSearchHistory(this);
                break;
            case R.id.tv_cancel:
                Log.i("tv_cancel------------>","tv_cancel");
//                finish();
                if (!TextUtils.isEmpty(mEditText.getText().toString().trim())){
                    if (inputMethodManager.isActive())
                        inputMethodManager.hideSoftInputFromWindow(getWindow().peekDecorView().getApplicationWindowToken(),0);
                    onSearch();
                }else {
                    Toast.makeText(SearchActivity.this,"还没输入搜索关键词哦！",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ll_back:
                finish();
                break;
            case R.id.iv_sort:
                toggleSortMenu();
                break;
            default:
                break;
        }
    }
    private void historyCancel(){
        mHistorys.clear();
        mHistoryAdapter.clear();
        SharedPreferences.Editor editor= mSharedPreferences.edit();
        editor.putStringSet("history",new LinkedHashSet<String>()).commit();
    }

    private void onSearch(){

        mBackView.setVisibility(View.VISIBLE);
        mSortView.setVisibility(View.VISIBLE);
        mSearchView.setVisibility(View.GONE);

        keyWord= mEditText.getText().toString();
//        SharedPreferences.Editor editor=mSharedPreferences.edit();
        Log.i("msg-------->",keyWord);
//        mHistorys.add(keyWord);
//        editor.putStringSet("history",mHistorys);
//        editor.commit();

        Log.i("mHistoryList.contains(keyWord)--->",mHistoryList.contains(keyWord)+"");
        if (mHistoryList.contains(keyWord)){
            mHistoryList.remove(keyWord);
        }
        mHistoryList.add(keyWord);
        SharedPreferencesUtil.saveHistoryData(this,mHistoryList);

        Message message = new Message();
        message.what=UPDATE_HISTORY;
        message.obj=keyWord;
        mHandler.sendMessage(message);
    }
    private void getHotSearch(){
        new Thread(){
            @Override
            public void run() {
                try {
                    URL url = new URL(ZhaiDou.HOT_SEARCH_URL);
                    String jsonContent = HtmlFetcher.fetch(url);
                    Log.d("HOT", "-------> 加载jsonContent: " + jsonContent);
                    JSONArray array = new JSONArray(jsonContent);
                    for (int i=0;i<array.length();i++){
                        mHotList.add(array.getString(i));
                    }
                    mHandler.sendEmptyMessage(UPDATE_HOTDATA);
                }catch (Exception e){
                    Log.e("HOT", "不能加载数据: " + e);
                }
            }
        }.start();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Adapter adapter =adapterView.getAdapter();
        String item =(String)adapter.getItem(position);
        mEditText.setText(item);
    }

    private class SearchFragmentAdapter extends FragmentPagerAdapter{
        public SearchFragmentAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }
        @Override
        public Fragment getItem(int i) {
            return mFragments.get(i);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position==0)
                return "单品";
            return "攻略";
        }

    }

    public void toggleSortMenu(){
        mViewPager.setOnTouchListener(null);
        if (mSortFragment.isHidden()){
            mSortFragment.setData(mViewPager.getCurrentItem(),0);
            getSupportFragmentManager().beginTransaction().show(mSortFragment).commit();
        }else {
            getSupportFragmentManager().beginTransaction().hide(mSortFragment).commit();
        }
    }

    @Override
    public void refreshData(int index) {
        sort=index;
        mViewPager.setFocusable(true);
        int page = mViewPager.getCurrentItem();
        if (page==0){
            mSingleFragment.FetchData(keyWord,index,1);
        }else {
            mStrategyFragment.FetchData(keyWord,index,1);
        }
    }

    @Override
    public void onHistoryItemClick(int position,String history) {
        Log.i("position---------------->",position+"----->"+history);
        mEditText.setText(history);
    }
}

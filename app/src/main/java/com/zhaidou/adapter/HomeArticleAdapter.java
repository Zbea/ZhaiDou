package com.zhaidou.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Article;
import com.zhaidou.utils.DeviceUtils;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by Zbea on 16/6/29.
 */
public class HomeArticleAdapter extends BaseListAdapter<Article>
{
    private WeakHashMap<Integer, View> mHashMap = new WeakHashMap<Integer, View>();
    private Context context;
    private int screenWidth;
    private int type=1;//type=1为正常案例，2为设计师案例

    public HomeArticleAdapter(Context context, List<Article> list,int type)
    {
        super(context, list);
        this.context = context;
        this.type=type;
        screenWidth= DeviceUtils.getScreenWidth(context);
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent)
    {
//        convertView = mHashMap.get(position);
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.item_home_article_list, null);
        ImageView cover = ViewHolder.get(convertView, R.id.cover);
        cover.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth * 400/ 750));
        TextView title = ViewHolder.get(convertView, R.id.title);
        TypeFaceTextView info = ViewHolder.get(convertView, R.id.info);
        TypeFaceTextView comments = ViewHolder.get(convertView, R.id.comments);

        Article article = getList().get(position);
        ToolUtils.setImageCacheUrl(article.getImg_url(), cover, R.drawable.icon_loading_item);
        title.setText(article.getTitle());
        info.setText(article.getInfo());
        comments.setText("评论:"+article.getReviews());
        comments.setVisibility(type==1?View.VISIBLE:View.GONE);
//        mHashMap.put(position, convertView);
        return convertView;
    }
}

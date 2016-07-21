package com.zhaidou.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.activities.PhotoViewActivity;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Comment;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.CircleImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 文章评论列表适配器Created by Zbea on 16/6/28.
 */
public class CommentAdapter extends BaseListAdapter<Comment>
{
    Context context;

    public CommentAdapter(Context context, List<Comment> list)
    {
        super(context, list);
        this.context = context;
    }

    public void upDate(List<Comment> list)
    {
        List<Comment> comments1=new ArrayList<Comment>();
        if (list.size()>5)
        {
            for (int i = 0; i < 5; i++)
            {
                comments1.add(list.get(i));
            }
        }
        else
        {
            comments1.addAll(list);
        }
        this.list=comments1;
        notifyDataSetChanged();
    }

    @Override
    public View bindView(final int position, View convertView, ViewGroup parent)
    {
//            convertView = mHashMap.get(position);
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.item_comment_message, null);
        CircleImageView header = ViewHolder.get(convertView, R.id.commentHeader);
        TextView name = ViewHolder.get(convertView, R.id.commentNameTv);
        TextView time = ViewHolder.get(convertView, R.id.commentTimeTv);
        LinearLayout commentLine = ViewHolder.get(convertView, R.id.commentLine);
        LinearLayout commentImageLine = ViewHolder.get(convertView, R.id.commentImageLine);
        TextView commentInfo = ViewHolder.get(convertView, R.id.commentInfoTv);

        LinearLayout commentReplyLine = ViewHolder.get(convertView, R.id.commentReplyLine);
        LinearLayout commentImageFormerLine = ViewHolder.get(convertView, R.id.commentImageFormerLine);
        TextView commentInfoFormer = ViewHolder.get(convertView, R.id.commentInfoFormerTv);
        TextView commentNameFormer = ViewHolder.get(convertView, R.id.commentNameFormerTv);

        LinearLayout commentImageReplyLine = ViewHolder.get(convertView, R.id.commentImageReplyLine);
        TextView commentReply= ViewHolder.get(convertView, R.id.commentInfoReplyTv);

        Comment comment=getList().get(position);
        commentImageLine.removeAllViews();
        commentImageFormerLine.removeAllViews();
        commentImageReplyLine.removeAllViews();

        if (TextUtils.isEmpty(comment.commentFormer)&comment.imagesFormer.size()==0)
        {
            ToolUtils.setImageCacheUrl(comment.userImage, header, R.drawable.icon_loading_defalut);
            name.setText(comment.userName);
            time.setText(comment.time);
            commentLine.setVisibility(View.VISIBLE);
            commentReplyLine.setVisibility(View.GONE);

            if (comment.images==null|comment.images.size()==0)
            {
                commentImageLine.setVisibility(View.GONE);
            }
            else
            {
                commentImageLine.setVisibility(View.VISIBLE);
                addImageView(commentImageLine,comment.images);
            }
            commentInfo.setText(comment.comment);
            commentInfo.setVisibility(!TextUtils.isEmpty(comment.comment)?View.VISIBLE: View.GONE);
            if(comment.status.equals("F"))
            {
                commentImageLine.setVisibility(View.GONE);
                commentInfo.setTextColor(mContext.getResources().getColor(R.color.text_gary_color));
            }
            else
            {
                commentInfo.setTextColor(mContext.getResources().getColor(R.color.text_normal_color));
            }

        }
        else
        {
            ToolUtils.setImageCacheUrl(comment.userImage, header, R.drawable.icon_loading_defalut);
            name.setText(comment.userName);
            time.setText(comment.time);
            commentLine.setVisibility(View.GONE);
            commentReplyLine.setVisibility(View.VISIBLE);

            if (comment.imagesFormer ==null|comment.imagesFormer.size()==0)
            {
                commentImageFormerLine.setVisibility(View.GONE);
            }
            else
            {
                commentImageFormerLine.setVisibility(View.VISIBLE);
                addImageView(commentImageFormerLine,comment.imagesFormer);
            }
            commentNameFormer.setText(comment.userNameFormer);
            commentInfoFormer.setText(comment.commentFormer);
            commentInfoFormer.setVisibility(TextUtils.isEmpty(comment.commentFormer)?View.GONE: View.VISIBLE);
            if(comment.statusFormer.equals("F"))
            {
                commentImageFormerLine.setVisibility(View.GONE);
                commentInfoFormer.setTextColor(mContext.getResources().getColor(R.color.text_gary_color));
            }
            else
            {
                commentInfoFormer.setTextColor(mContext.getResources().getColor(R.color.text_normal_color));
            }

            if (comment.images==null|comment.images.size()==0)
            {
                commentImageReplyLine.setVisibility(View.GONE);
            }
            else
            {
                commentImageReplyLine.setVisibility(View.VISIBLE);
                addImageView(commentImageReplyLine,comment.images);
            }
            if(comment.status.equals("F"))
            {
                commentImageReplyLine.setVisibility(View.GONE);
                commentReply.setTextColor(mContext.getResources().getColor(R.color.text_gary_color));
            }
            else
            {
                commentReply.setTextColor(mContext.getResources().getColor(R.color.text_normal_color));
            }
            commentReply.setText(comment.comment);
            commentReply.setVisibility(!TextUtils.isEmpty(comment.comment)?View.VISIBLE: View.GONE);
        }
//            mHashMap.put(position, convertView);
        return convertView;
    }

    /**
     * 选择相片添加布局以及相关逻辑处理
     */
    private void addImageView(LinearLayout viewLayout, final List<String> ims)
    {
        for (int i = 0; i < ims.size(); i++)
        {
            final int position=i;
            View mView = LayoutInflater.from(mContext).inflate(R.layout.item_comment_image, null);
            ImageView imageIv = ( ImageView ) mView.findViewById(R.id.imageBg_iv);
            TextView btn=( TextView ) mView.findViewById(R.id.imageBgBtn);
            btn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent=new Intent(mContext, PhotoViewActivity.class);
                    intent.putExtra("images",ims.toArray(new String[]{}));
                    intent.putExtra("position",position);
                    mContext.startActivity(intent);

                }
            });
            ToolUtils.setImageCacheUrl(ims.get(i), imageIv, R.drawable.icon_loading_defalut);
            viewLayout.addView(mView);
        }


    }



}

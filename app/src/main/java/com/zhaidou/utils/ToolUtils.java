package com.zhaidou.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.HomeCompetitionActivity;
import com.zhaidou.activities.ItemDetailActivity;
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.fragments.GoodsDetailsFragment;
import com.zhaidou.fragments.HomeArticleListFragment;
import com.zhaidou.fragments.HomeBeautifulFragment;
import com.zhaidou.fragments.HomeFeatrueFragment;
import com.zhaidou.fragments.HomeWeixinListFragment;
import com.zhaidou.fragments.MagicClassicCaseDetailsFragment;
import com.zhaidou.fragments.MagicClassicCaseFragment;
import com.zhaidou.fragments.MagicDesignFragment;
import com.zhaidou.fragments.MagicImageCaseFragment;
import com.zhaidou.fragments.ShopTodaySpecialFragment;
import com.zhaidou.fragments.SpecialSaleFragment;
import com.zhaidou.model.Category;
import com.zhaidou.model.SwitchImage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by roy on 15/7/15.
 */
public class ToolUtils
{

    /**
     * 获得时间差别
     * @param date
     * @return
     */
    public static String getDateDiff(String date) throws ParseException
    {
        SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date mDate=new Date();
        long diff=df.parse(date).getTime()-mDate.getTime();
        if (diff>=24*60*60*1000)
        {
            return date.split(" ")[0];
        }
        else
        {
            String time=date.split(" ")[1];
            return time.substring(0,time.length()-3);
        }
    }

    /**
     * 处理价格为.0或者.00时取整数
     * @return
     */
    public static String isIntPrice(String price)
    {
        String mPrice=null;
        for (int i=0 ;i<price.length(); i++)
        {
            char c = price.charAt(i);
            //当不为整数的时候
            if ('.' == c)
            {
                //当存在小数点一位的时候
                if (price.length() == (i + 2))
                {
                    if (String.valueOf(price.charAt(i+1)).equals(""+0))
                    {
                        mPrice=price.substring(0,i);
                        return mPrice;
                    }
                }
                //当存在小数点两位时候
               if (price.length()==(i+3))
               {
                   if (String.valueOf(price.charAt(i+1)).equals(""+0)&&String.valueOf(price.charAt(i+2)).equals(""+0))
                   {
                       mPrice=price.substring(0,i);
                       return mPrice;
                   }
                   if (!String.valueOf(price.charAt(i+1)).equals(""+0)&&String.valueOf(price.charAt(i+2)).equals(""+0))
                   {
                       mPrice=price.substring(0,i+2);
                       return mPrice;
                   }
               }
            }
        }
        mPrice=price;
        return mPrice;
    }

    /**
     * 是否存在sdcard
     * @return
     */
    public static boolean hasSdcard()
    {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED) && Environment.getExternalStorageDirectory().exists())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * 判断手机号码格式是否正确
     * @param phone
     * @return
     */
    public static boolean isPhoneOk(String phone)
    {
        if (TextUtils.isEmpty(phone))
        {
            return false;
        }
        Pattern p=Pattern.compile("(1[3456789]\\d{9})");
//        Pattern p=Pattern.compile("(1[358]\\d{9})|(14[57]\\d{8})|(17[0678]\\d{8})");
        Matcher m=p.matcher(phone);
        return m.matches();
    }

    /**
     * 判断邮箱格式是否正确
     * @param email
     * @return
     */
    public static boolean isEmailOK(String email)
    {
        if (email!=null&email.length()>0)
        {
//            Pattern p=Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+");
            Pattern p=Pattern.compile("^[a-zA-Z][\\\\w\\\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\\\w\\\\.-]*[a-zA-Z0-9]\\\\.[a-zA-Z][a-zA-Z\\\\.]*[a-zA-Z]$");
            Matcher m=p.matcher(email);
            return m.matches();
        }
        else
        {
            return false;
        }
    }


    /**
     * 图片异步加载（缓存图片方法）
     * @param url
     * @param imageView
     */
    public static final void setImageCacheUrl(String url,ImageView imageView)
    {
        DisplayImageOptions options=new DisplayImageOptions.Builder()
//                .showImageForEmptyUri(R.drawable.icon_loading_defalut)
//                .showImageOnFail(R.drawable.icon_loading_defalut)
                .resetViewBeforeLoading(false)//default 设置图片在加载前是否重置、复位
                .cacheInMemory(true) // default  设置下载的图片是否缓存在内存中
                .cacheOnDisk(true) // default  设置下载的图片是否缓存在SD卡中
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();

        ImageLoader.getInstance().displayImage(url, imageView,options);
    }

    /**
     * 图片异步加载（缓存图片方法）切圆角
     * @param url
     * @param imageView
     */
    public static final void setImageCacheRoundUrl(String url,ImageView imageView ,int i,int resId)
    {
        DisplayImageOptions options=new DisplayImageOptions.Builder()
                	.displayer(new RoundedBitmapDisplayer(i))//设置圆角半径
                .showImageOnLoading(resId)
                .showImageForEmptyUri(resId)
                .showImageOnFail(resId)
                .resetViewBeforeLoading(false)//default 设置图片在加载前是否重置、复位
                .cacheInMemory(true) // default  设置下载的图片是否缓存在内存中
                .cacheOnDisk(true) // default  设置下载的图片是否缓存在SD卡中
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .build();

        ImageLoader.getInstance().displayImage(url, imageView,options);
    }

    /**
     * 图片异步加载（缓存图片方法）
     * @param url
     * @param imageView
     * @param resId 设置加载过程中背景底图
     */
    public static final void setImageCacheUrl(String url,ImageView imageView,int resId)
    {
        DisplayImageOptions options=new DisplayImageOptions.Builder()
                .showImageOnLoading(resId)
                .showImageForEmptyUri(resId)
                .showImageOnFail(resId)
                .cacheInMemory(true) // default  设置下载的图片是否缓存在内存中
                .cacheOnDisk(true) // default  设置下载的图片是否缓存在SD卡中
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
//                .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                .build();

        ImageLoader.getInstance().displayImage(url, imageView,options);
    }

    /**
     * 设置图片不复位
     * @param url
     * @param imageView
     * @param resId
     */
    public static final void setImageNoResetUrl(String url,final ImageView imageView,int resId)
    {
        DisplayImageOptions options=new DisplayImageOptions.Builder()
                .showImageOnLoading(resId)
                .showImageForEmptyUri(resId)
                .showImageOnFail(resId)
                .resetViewBeforeLoading(false)//default 设置图片在加载前是否重置、复位
                .cacheInMemory(true) // default  设置下载的图片是否缓存在内存中
                .cacheOnDisk(true) // default  设置下载的图片是否缓存在SD卡中
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .build();

        ImageLoader.getInstance().loadImage(url, options, new SimpleImageLoadingListener()
        {
            @Override
            public void onLoadingStarted(String s, View view)
            {
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason)
            {
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap)
            {
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void onLoadingCancelled(String s, View view)
            {
            }
        });
    }

    /**
     * 图片异步加载（不缓存图片设置）
     * @param url
     * @param imageView
     */
    public static final void setImageUrl(String url,ImageView imageView)
    {
        DisplayImageOptions options=new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.icon_loading_osale)
                .showImageOnFail(R.drawable.icon_loading_osale)
                .resetViewBeforeLoading(false)//default 设置图片在加载前是否重置、复位
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        ImageLoader.getInstance().displayImage(url, imageView,options);
    }

    /**
     * 图片异步加载（不缓存图片设置）
     * @param url
     * @param imageView
     * @param resId 设置加载过程中背景底图
     */
    public static final void setImageUrl(String url,ImageView imageView,int resId)
    {
        DisplayImageOptions options=new DisplayImageOptions.Builder()
                .showImageOnLoading(resId)
                .showImageForEmptyUri(resId)
                .showImageOnFail(resId)
                .resetViewBeforeLoading(false)//default 设置图片在加载前是否重置、复位
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        ImageLoader.getInstance().displayImage(url, imageView,options);
    }

    /**
     * 本地图片异步加载（不缓存图片设置，处理内存溢出）
     * @param url
     * @param imageView
     */
    public static final void setImagePreventMemoryLeaksUrl(String url,ImageView imageView)
    {
        DisplayImageOptions options=new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(false)//default 设置图片在加载前是否重置、复位
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .build();
        ImageLoader.getInstance().displayImage(url, imageView,options);
    }

    /**
     * 打印信息
     * @param msg
     */
    public static final void setLog(String msg)
    {
        Log.i("zhaidou",msg);
    }

    /**
     * Toast显示短时间
     * @param mContext
     * @param msg
     */
    public static final void setToast(Context mContext,String msg)
    {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Toast显示短时间
     * @param mContext
     * @param msgResId
     */
    public static final void setToast(Context mContext,int msgResId)
    {
        Toast.makeText(mContext, msgResId, Toast.LENGTH_SHORT).show();
    }

    /**
     * 长显示
     * @param mContext
     * @param msg
     */
    public static final void setToastLong(Context mContext,String msg)
    {
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * 长显示
     * @param mContext
     * @param msgResId
     */
    public static final void setToastLong(Context mContext,int msgResId)
    {
        Toast.makeText(mContext, msgResId, Toast.LENGTH_LONG).show();
    }

    /**
     * 设置banner的跳转
     * @param item
     */
    public static final void setBannerGoto(SwitchImage item,Context mContext)
    {
        if (item.type == 0)
        {
            SpecialSaleFragment specialSaleFragment = SpecialSaleFragment.newInstance("", item.typeValue);
            ((MainActivity) mContext).navigationToFragment(specialSaleFragment);
        } else if (item.type == 1)
        {
            if (item.title.equals("天天刮奖"))
            {
                Intent detailIntent = new Intent(mContext, HomeCompetitionActivity.class);
                detailIntent.putExtra("url", item.typeValue);
                detailIntent.putExtra("from", "lottery");
                detailIntent.putExtra("title", "天天刮奖");
                mContext.startActivity(detailIntent);
            }
            else
            {
                Intent intent = new Intent();
                intent.putExtra("url", item.typeValue);
                intent.setClass(mContext, WebViewActivity.class);
                mContext.startActivity(intent);
            }
        } else if (item.type == 2)
        {
            Intent detailIntent = new Intent(mContext, ItemDetailActivity.class);
            detailIntent.putExtra("id", item.id + "");
            detailIntent.putExtra("from", "product");
            detailIntent.putExtra("title", item.title);
            detailIntent.putExtra("cover_url", item.imageUrl);
            detailIntent.putExtra("url", ZhaiDou.ARTICLE_DETAIL_URL + item.typeValue);
            detailIntent.putExtra("show_header", true);
            mContext.startActivity(detailIntent);
        } else if (item.type == 3)
        {
            GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance("", 0+"");
            Bundle bundle = new Bundle();
            bundle.putString("index",item.typeValue);
            bundle.putString("page", item.title);
            goodsDetailsFragment.setArguments(bundle);
            ((MainActivity) mContext).navigationToFragment(goodsDetailsFragment);
        } else if(item.type==4)
        {
            Category category = new Category();
            category.setId(Integer.parseInt(item.typeValue));
            HomeArticleListFragment shopTodayHomeArticleListFragment = HomeArticleListFragment.newInstance("", category);
            ((MainActivity) mContext).navigationToFragment(shopTodayHomeArticleListFragment);
        }
        else if(item.type==5)
        {
            ShopTodaySpecialFragment shopTodaySpecialFragment = ShopTodaySpecialFragment.newInstance(item.title,  item.typeValue, item.imageUrl);
            ((MainActivity) mContext).navigationToFragmentWithAnim(shopTodaySpecialFragment);
        }
        else if(item.type==6)
        {
            HomeFeatrueFragment homeFeatrueFragment = HomeFeatrueFragment.newInstance(item.title, item.typeValue, item.imageUrl);
            ((MainActivity) mContext).navigationToFragmentWithAnim(homeFeatrueFragment);
        }
        else if(item.type==7)
        {
            HomeWeixinListFragment homeFeatrueFragment = HomeWeixinListFragment.newInstance(item.title, item.typeValue);
            ((MainActivity) mContext).navigationToFragmentWithAnim(homeFeatrueFragment);
        }
        else if(item.type==8)
        {
            HomeBeautifulFragment goodsDetailsFragment = HomeBeautifulFragment.newInstance(item.title, 0+"");
            ((MainActivity) mContext).navigationToFragment(goodsDetailsFragment);
        }
        else if(item.type==9)
        {
            MagicDesignFragment magicDesignFragment = MagicDesignFragment.newInstance(item.title, item.typeValue);
            ((MainActivity) mContext).navigationToFragmentWithAnim(magicDesignFragment);
        }
        else if(item.type==10)
        {
            MagicClassicCaseDetailsFragment magicClassicCaseDetailsFragment = MagicClassicCaseDetailsFragment.newInstance(item.title, item.typeValue);
            ((MainActivity) mContext).navigationToFragmentWithAnim(magicClassicCaseDetailsFragment);
        }
        else if(item.type==11)
        {
            MagicClassicCaseFragment magicClassicCaseFragment = MagicClassicCaseFragment.newInstance("", "");
            ((MainActivity) mContext).navigationToFragment(magicClassicCaseFragment);
        }
        else if(item.type==12)
        {
            MagicImageCaseFragment magicImageCaseFragment = MagicImageCaseFragment.newInstance("", "");
            ((MainActivity) mContext).navigationToFragment(magicImageCaseFragment);
        }
        else
        {

        }
    }

}

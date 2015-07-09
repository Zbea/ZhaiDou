package com.zhaidou.utils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zhaidou.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.os.Handler;
import android.widget.ImageView;
/**
 * Created by wangclark on 15/6/8.
 */
public class AsyncImageLoader1 {
    // 为了加快速度，在内存中开启缓存
    private Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();
    // 缩略图缓存
    private Map<String, SoftReference<Drawable>> smallImageCache = new HashMap<String, SoftReference<Drawable>>();
    private ExecutorService executorService = Executors.newFixedThreadPool(4); // 固定4个线程来执行任务
    private final Handler handler = new Handler();

    private Context mContext;

    public AsyncImageLoader1(Context context) {
        mContext = context;
    }

    /**
     *
     * @param imageUrl
     *            图像url地址
     * @param callback
     *            回调接口
     * @return 返回内存中缓存的图像，第一次加载返回null
     */
    public Drawable loadDrawable(final String imageUrl,
                                 final ImageCallback callback) {
        // 如果缓存过就从缓存中取出数据
        if (imageCache.containsKey(imageUrl)) {
            SoftReference<Drawable> softReference = imageCache.get(imageUrl);
            if (softReference.get() != null) {
                return softReference.get();
            }
        } else if (useTheImage(imageUrl) != null) {
            return useTheImage(imageUrl);
        }
        // 缓存中没有图像，则从网络上取出数据，并将取出的数据缓存到内存中
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    final Drawable drawable = Drawable.createFromStream(
                            new URL(imageUrl).openStream(), "image.png");
                    imageCache.put(imageUrl, new SoftReference<Drawable>(
                            drawable));
                    handler.post(new Runnable() {
                        public void run() {
                            callback.imageLoaded(drawable);
                        }
                    });
                    saveFile(drawable, imageUrl);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return null;
    }

    /**
     * 为了节约手机内存，载入缩略图
     *
     * @param imageUrl
     *            图像url地址
     * @param callback
     *            回调接口
     * @return 返回内存中缓存的图像，第一次加载返回null
     */
    public Drawable loadSmallDrawable(final String imageUrl,
                                      final ImageCallback callback) {
        // 如果缓存过就从缓存中取出数据
        if (smallImageCache.containsKey(imageUrl)) {
            SoftReference<Drawable> softReference = smallImageCache
                    .get(imageUrl);
            if (softReference.get() != null) {
                return softReference.get();
            }
        } else if (useTheSmallImage(imageUrl) != null) {
            return useTheSmallImage(imageUrl);
        }
        // 缓存中没有图像，则从网络上取出数据，并将取出的数据缓存到内存中
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    // final Drawable drawable = Drawable.createFromStream(
                    // new URL(imageUrl).openStream(), "image.png");
                    // TODO: 生成缩略图

                    int width = 180;
                    int height = 124;
                    int options = ThumbnailUtils.OPTIONS_RECYCLE_INPUT;

                    // 通过openRawResource获取一个InputStream对象<br>
                    InputStream input = new URL(imageUrl).openStream();
                    // 通过InputStream创建BitmapDrawable对象<br>
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(input);
                    // 保存大图片文件
                    saveFile(bitmapDrawable, imageUrl);
                    // 通过BitmapDrawable对象获取Bitmap对象<br>
                    Bitmap source = bitmapDrawable.getBitmap();
                    // 利用Bitmpap对象创建缩略图<br>
                    Bitmap smallImage = ThumbnailUtils.extractThumbnail(source,
                            width, height, options);

                    final Drawable drawable = new BitmapDrawable(smallImage);
                    // 保存小图片
                    saveSmallImage(drawable, imageUrl);

                    smallImageCache.put(imageUrl, new SoftReference<Drawable>(
                            drawable));
                    handler.post(new Runnable() {
                        public void run() {
                            callback.imageLoaded(drawable);
                        }
                    });

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return null;
    }

    // 从网络上取数据方法
    public Drawable loadImageFromUrl(String imageUrl) {
        try {

            return Drawable.createFromStream(new URL(imageUrl).openStream(),
                    "image.png");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 对外界开放的回调接口
    public interface ImageCallback {
        // 注意 此方法是用来设置目标对象的图像资源
        public void imageLoaded(Drawable imageDrawable);
    }

    /**
     * @description 引入线程池，并引入内存缓存功能,并对外部调用封装了接口，简化调用过程
     * @param url
     * @param iv
     */
    public void LoadImage(final String url, final ImageView iv) {
        if (iv.getImageMatrix() == null) {
            // 默认的加载图片
            iv.setImageResource(R.drawable.cover);
        }
		/*
		 * 如果缓存过就会从缓存中取出图像，ImageCallback接口中方法也不会被执行
		 */
        Drawable cacheImage = loadDrawable(url,
                new AsyncImageLoader1.ImageCallback() {
                    // 请参见实现：如果第一次加载url时下面方法会执行
                    public synchronized void imageLoaded(Drawable imageDrawable) {
                        iv.setImageDrawable(imageDrawable);
                        // iv.startAnimation(AnimationUtils.loadAnimation(
                        // mContext, R.anim.fade_in));
                    }
                });
        if (cacheImage != null) {
            System.out.print("cacheImage----->"+cacheImage.toString());
            iv.setImageDrawable(cacheImage);
        }
    }

    /**
     * @description 引入线程池，并引入内存缓存功能,并对外部调用封装了接口，简化调用过程
     * @param url
     * @param iv
     */
    public void LoadSmallImage(final String url, final ImageView iv) {
        // TODO: 下载大图片，生成小图片， 显示小图片，保证手机内存
        if (iv.getImageMatrix() == null) {
            // 默认的加载图片
            iv.setImageResource(R.drawable.cover);
        }
        // 如果缓存过就会从缓存中取出图像，ImageCallback接口中方法也不会被执行
        Drawable cacheImage = loadSmallDrawable(url,
                new AsyncImageLoader1.ImageCallback() {
                    // 请参见实现：如果第一次加载url时下面方法会执行
                    public synchronized void imageLoaded(Drawable imageDrawable) {
                        iv.setImageDrawable(imageDrawable);
                    }
                });
        if (cacheImage != null) {
            iv.setImageDrawable(cacheImage);
        }
    }

    /**
     * 保存图片到SD卡上
     *
     * @param dw
     * @param url
     *
     */
    private void saveFile(Drawable dw, String url) {
        String dir = SDcardUtils.CACHE_IAMGE;
        saveFile(dw, url, dir);
    }

    /**
     * 保存图片到缩略图文件夹
     *
     * @param dw
     * @param url
     */
    private void saveSmallImage(Drawable dw, String url) {
        String dir = SDcardUtils.CACHE_SMALL_IMAGE;
        saveFile(dw, url, dir);
    }

    /**
     * 保存图片到SD卡上
     *
     * @param dw
     * @param url
     * @param dir
     *            文件夹名称
     *
     */
    private void saveFile(Drawable dw, String url, String dir) {
        try {
            BitmapDrawable bd = (BitmapDrawable) dw;
            Bitmap bm = bd.getBitmap();
            // //Log.i(TAG, "保存前图片的宽="+bm.getWidth()+"保存前图片的高="+bm.getHeight());
            // 获得文件名字
            final String fileNa = MD5Util.getMD5Encoding(url);
            File file = new File(dir + "/" + fileNa);
            // 创建图片缓存文件夹
            boolean sdCardExist = Environment.getExternalStorageState().equals(
                    android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
            if (sdCardExist) {
                File maiduo = new File(SDcardUtils.CLIENT_PATH);
                File ad = new File(dir);
                // 如果文件夹不存在
                if (!maiduo.exists()) {
                    // 按照指定的路径创建文件夹
                    maiduo.mkdir();
                    // 如果文件夹不存在
                } else if (!ad.exists()) {
                    // 按照指定的路径创建文件夹
                    ad.mkdir();
                }
                // 检查图片是否存在
                if (!file.exists()) {
                    file.createNewFile();
                }
            }

            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(file));
            bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            //
        }
    }

    /**
     * 使用SD卡上的图片
     *
     */
    public Drawable useTheImage(String imageUrl) {
        String dir = "image";
        return useTheImage(imageUrl, dir);
    }

    public Bitmap getTheImage(String imageUrl, int type) {
        String dir = SDcardUtils.CACHE_IAMGE;
        if (type == 0) {
            dir = SDcardUtils.CACHE_SMALL_IMAGE;
        }
        Bitmap bmpDefaultPic = null;

        // 获得文件路径
        String imageSDCardPath = dir + "/" + MD5Util.getMD5Encoding(imageUrl);
        File file = new File(imageSDCardPath);
        // 检查图片是否存在
        if (!file.exists()) {
            return null;
        }
        bmpDefaultPic = BitmapFactory.decodeFile(imageSDCardPath, null);
        if (bmpDefaultPic == null) {
            return null;
        }
        return bmpDefaultPic;
    }

    public String getTheImage(String imageUrl) {
        String dir = SDcardUtils.CACHE_IAMGE;

        // 获得文件路径
        String imageSDCardPath = dir + "/" + MD5Util.getMD5Encoding(imageUrl);
        File file = new File(imageSDCardPath);
        // 检查图片是否存在
        if (!file.exists()) {
            return null;
        }
        return imageSDCardPath;
    }

    /**
     * 使用SD卡上的缩略图片
     *
     */
    public Drawable useTheSmallImage(String imageUrl) {
        String dir = SDcardUtils.CACHE_SMALL_IMAGE;
        return useTheImage(imageUrl, dir);
    }

    private Drawable useTheImage(String imageUrl, String dir) {
        Bitmap bmpDefaultPic = null;

        // 获得文件路径
        String imageSDCardPath = dir + "/" + MD5Util.getMD5Encoding(imageUrl);
        File file = new File(imageSDCardPath);
        // 检查图片是否存在
        if (!file.exists()) {
            return null;
        }
        bmpDefaultPic = BitmapFactory.decodeFile(imageSDCardPath, null);
        if (bmpDefaultPic == null) {
            return null;
        }
        Drawable drawable = new BitmapDrawable(bmpDefaultPic);
        return drawable;
    }
}


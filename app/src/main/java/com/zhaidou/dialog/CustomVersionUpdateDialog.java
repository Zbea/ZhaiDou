package com.zhaidou.dialog;

        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.net.HttpURLConnection;
        import java.net.MalformedURLException;
        import java.net.URL;
        import android.app.AlertDialog;
        import android.app.Dialog;
        import android.app.AlertDialog.Builder;
        import android.content.Context;
        import android.content.Intent;
        import android.net.Uri;
        import android.os.Handler;
        import android.os.Message;
        import android.text.Html;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.Window;
        import android.view.animation.Animation;
        import android.view.animation.AnimationUtils;
        import android.widget.LinearLayout;
        import android.widget.ProgressBar;
        import android.widget.RelativeLayout;
        import android.widget.TextView;

        import com.zhaidou.R;
        import com.zhaidou.ZhaiDou;
        import com.zhaidou.view.CircleProgressBar;

/**
 * Created by roy on 15/8/12.
 */
public class CustomVersionUpdateDialog
{

    private Context mContext;

    private Dialog noticeDialog;

    private static final String savePath = "/sdcard/zhaidou/";

    private static final String saveFileName = savePath+"zhaidouUpdateApk.apk";


    private CircleProgressBar mProgress;
    private TextView uInfo_tv;
    private TextView max_tv;
    private TextView current_tv;
    private TextView ratio_tv;
    private TextView info_tv;
    private int count = 0;
    private int length;
    private String serviceInfo;
    private String serviceUrl;
    private LinearLayout managerLine;
    private RelativeLayout updateline;

    private static final int DOWN_UPDATE = 1;

    private static final int DOWN_OVER = 2;

    private int progress = 0;

    private Thread downLoadThread;
    private boolean isRun=true;

    private boolean interceptFlag = false;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            max_tv.setText("" + length + " kb ");
            switch (msg.what)
            {
                case DOWN_UPDATE:
                    current_tv.setText("" + count + " kb ");
                    ratio_tv.setText("" + progress + "%");
                    mProgress.setProgress(progress);
                    break;
                case DOWN_OVER:
                    progress = progress + 1;
                    ratio_tv.setText("" + progress + "%");
                    noticeDialog.dismiss();
                    installApk();
                    break;
                default:
                    break;
            }
        };
    };

    public CustomVersionUpdateDialog(Context context, String info,String url)
    {
        this.mContext = context;
        this.serviceInfo = info;
        this.serviceUrl = url;
    }

    public void checkUpdateInfo()
    {
        showNoticeDialog();
    }

    private void showNoticeDialog()
    {
        noticeDialog=new Dialog(mContext,R.style.custom_dialog);
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_custom_update_manager, null);
        noticeDialog.setContentView(view);
        noticeDialog.show();

        managerLine=(LinearLayout)view.findViewById(R.id.updateManagerLine);
        updateline=(RelativeLayout)view.findViewById(R.id.updateProgressLine);

        mProgress = (CircleProgressBar) view.findViewById(R.id.progress);
        mProgress.setMaxProgress(100);
        max_tv = (TextView) view.findViewById(R.id.update_max);
        current_tv = (TextView) view.findViewById(R.id.update_current);
        ratio_tv = (TextView) view.findViewById(R.id.update_ratio);
        TextView cancels_btn = (TextView)view.findViewById(R.id.cancels_btn);
        cancels_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                interceptFlag=true;
                noticeDialog.dismiss();
            }
        });

        uInfo_tv = (TextView) view.findViewById(R.id.update_info);
        uInfo_tv.setText(serviceInfo);
        TextView download_btn = (TextView) view.findViewById(R.id.download_btn);
        download_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Animation animation= AnimationUtils.loadAnimation(mContext,R.anim.page_scale);
                animation.start();
                updateline.setAnimation(animation);
                managerLine.setVisibility(View.GONE);
                updateline.setVisibility(View.VISIBLE);
                downloadApk();
            }
        });

        TextView cancel_btn = (TextView) view.findViewById(R.id.cancel_btn);
        cancel_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                noticeDialog.dismiss();
            }
        });
    }

    private Runnable mdownApkRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                URL url = new URL(serviceUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                length = conn.getContentLength();
                InputStream is = conn.getInputStream();
                File file = new File(savePath);
                if (!file.exists())
                {
                    file.mkdir();
                }
                String apkFile = saveFileName;
                File ApkFile = new File(apkFile);
                FileOutputStream fos = new FileOutputStream(ApkFile);
                byte buf[] = new byte[1024];
                do
                {
                    int numread = is.read(buf);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0)
                    {
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf, 0, numread);
                }
                while (!interceptFlag);// 结束标志

                fos.close();
                is.close();
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    };

    /**
     * 开始下载apk
     */
    private void downloadApk()
    {
        downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }

    private void installApk()
    {
        File apkfile = new File(saveFileName);
        if (!apkfile.exists())
        {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }

    private class customThread extends Thread
    {

        public void customThread(Boolean run)
        {
            isRun=run;
        }

        @Override
        public void run()
        {
            while (isRun)
            {
                super.run();
            }
        }
    }


}

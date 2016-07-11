package com.zhaidou.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.utils.DeviceUtils;

import java.io.File;

/**
 * Created by roy on 16/7/11.
 */
public class CustomPhotoMenuDialog
{
    Context mContext;
    String pathUrl;
    Dialog dialog;
    public final static int MENU_CAMERA_SELECTED = 0;
    public final static int MENU_PHOTO_SELECTED = 1;

    public CustomPhotoMenuDialog(Context context, String path)
    {
        super();
        mContext=context;
        pathUrl=path;
    }


    public Dialog showPhotoMenuDialog()
    {
        View view= LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_photo_menu,null);
        dialog=new Dialog(mContext, R.style.custom_dialog);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.show();

        Window window=dialog.getWindow();
        WindowManager.LayoutParams layoutParams=window.getAttributes();
        layoutParams.width= DeviceUtils.getScreenWidth(mContext);
        window.setAttributes(layoutParams);
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.pop_anim_style);

        TextView tv_camera=(TextView)view.findViewById(R.id.tv_camera);
        tv_camera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Uri imageUri = Uri.fromFile(new File(pathUrl));
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                ((MainActivity)mContext).startActivityForResult(intent, MENU_CAMERA_SELECTED);
                dialog.dismiss();
            }
        });

        TextView tv_photo=(TextView)view.findViewById(R.id.tv_photo);
        tv_photo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                ((MainActivity)mContext).startActivityForResult(intent1, MENU_PHOTO_SELECTED);
                dialog.dismiss();
            }
        });

        TextView tv_cancel=(TextView)view.findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        return dialog;
    }

}

package com.zhaidou.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.utils.ToolUtils;

public class PhotoViewFragment extends BaseFragment {
    private static final String ARG_POSITION = "position";
    private static final String ARG_URL = "url";

    private int mPosition;
    private String mUrl;
    private ImageView mImageView;

    private PhotoListener photoListener;

    public static PhotoViewFragment newInstance(int position, String url) {
        PhotoViewFragment fragment = new PhotoViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }
    public PhotoViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPosition = getArguments().getInt(ARG_POSITION);
            mUrl = getArguments().getString(ARG_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_photo_view, container, false);
        view.findViewById(R.id.iv_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).popToStack(PhotoViewFragment.this);
                photoListener.onPhotoDelete(mPosition,mUrl);
            }
        });
        ImageView photo=(ImageView)view.findViewById(R.id.iv_pic);
        ToolUtils.setImageCacheUrl("file://"+mUrl,photo);
        return view;
    }

    public void setPhotoListener(PhotoListener photoListener) {
        this.photoListener = photoListener;
    }

    public interface PhotoListener{
        public void onPhotoDelete(int position,String url);
    }
}

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PhotoViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PhotoViewFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_POSITION = "position";
    private static final String ARG_URL = "url";

    // TODO: Rename and change types of parameters
    private int mPosition;
    private String mUrl;

    private PhotoListener photoListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param position Parameter 1.
     * @param url Parameter 2.
     * @return A new instance of fragment PhotoViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PhotoViewFragment newInstance(int position, String url) {
        PhotoViewFragment fragment = new PhotoViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }
    public PhotoViewFragment() {
        // Required empty public constructor
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

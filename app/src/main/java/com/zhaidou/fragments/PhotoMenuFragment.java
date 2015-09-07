package com.zhaidou.fragments;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;

public class PhotoMenuFragment extends BaseFragment implements View.OnClickListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private TextView tv_camera;
    private TextView tv_photo;
    private TextView tv_cancel;

    private MenuSelectListener menuSelectListener;

    public static PhotoMenuFragment newInstance(String param1, String param2) {
        PhotoMenuFragment fragment = new PhotoMenuFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public PhotoMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_photo_menu, container, false);
        tv_camera=(TextView)view.findViewById(R.id.tv_camera);
        tv_cancel=(TextView)view.findViewById(R.id.tv_cancel);
        tv_photo=(TextView)view.findViewById(R.id.tv_photo);

        tv_photo.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
        tv_camera.setOnClickListener(this);
        view.findViewById(R.id.ll_menu_close).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_camera:
                menuSelectListener.onMenuSelect(0,"拍照");
                break;
            case R.id.tv_photo:
                menuSelectListener.onMenuSelect(1,"相册");
                break;
            case R.id.tv_cancel:
                if (getParentFragment()!=null&&getParentFragment() instanceof ProfileFragment){
                    ((ProfileFragment)getParentFragment()).toggleMenu();
                }else if (getParentFragment()!=null&&getParentFragment() instanceof OrderAfterSaleFragment){
                    ((OrderAfterSaleFragment)getParentFragment()).toggleMenu();
                }
                break;
            case R.id.ll_menu_close:
                if (getParentFragment()!=null&&getParentFragment() instanceof ProfileFragment){
                    ((ProfileFragment)getParentFragment()).toggleMenu();
                }else if (getParentFragment()!=null&&getParentFragment() instanceof OrderAfterSaleFragment){
                    ((OrderAfterSaleFragment)getParentFragment()).toggleMenu();
                }
                break;
            default:
                break;
        }
    }

    public void setMenuSelectListener(MenuSelectListener menuSelectListener) {
        this.menuSelectListener = menuSelectListener;
    }

    public interface MenuSelectListener{
        public void onMenuSelect(int position,String tag);
    }

}

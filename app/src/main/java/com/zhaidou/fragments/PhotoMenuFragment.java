package com.zhaidou.fragments;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaidou.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PhotoMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PhotoMenuFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView tv_camera;
    private TextView tv_photo;
    private TextView tv_cancel;

    private MenuSelectListener menuSelectListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PhotoMenuFragment.
     */
    // TODO: Rename and change types and number of parameters
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
                ((ProfileFragment)getParentFragment()).toggleMenu();
                break;
            case R.id.ll_menu_close:
                Log.i("fff","ll_menu_close");
                Toast.makeText(getActivity(),"ll_menu_close",1).show();
                ((ProfileFragment)getParentFragment()).toggleMenu();
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

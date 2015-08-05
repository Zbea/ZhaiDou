package com.zhaidou.fragments;



import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SaleServiceFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SaleServiceFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SaleServiceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SaleServiceFragment newInstance(String param1, String param2) {
        SaleServiceFragment fragment = new SaleServiceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public SaleServiceFragment() {
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
        View view=inflater.inflate(R.layout.goods_details_aftersale_page, container, false);
        view.findViewById(R.id.rl_qq_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url="mqqwpa://im/chat?chat_type=wpa&uin=11300";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });
        return view;
    }


}

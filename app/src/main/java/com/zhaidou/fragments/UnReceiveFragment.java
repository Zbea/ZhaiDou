package com.zhaidou.fragments;



import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UnReceiveFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class UnReceiveFragment extends BaseFragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView mLogisticsButton,mReceivedButton;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UnReceiveFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UnReceiveFragment newInstance(String param1, String param2) {
        UnReceiveFragment fragment = new UnReceiveFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public UnReceiveFragment() {
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
        View view=inflater.inflate(R.layout.fragment_un_receive, container, false);
        mLogisticsButton=(TextView)view.findViewById(R.id.bt_logistics);
        mReceivedButton=(TextView)view.findViewById(R.id.bt_received);
        mLogisticsButton.setOnClickListener(this);
        mReceivedButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_logistics:
                LogisticsMsgFragment logisticsMsgFragment=LogisticsMsgFragment.newInstance("","");
                ((MainActivity)getActivity()).navigationToFragment(logisticsMsgFragment);
                break;
            case R.id.bt_received:
                final Dialog dialog=new Dialog(getActivity(), R.style.custom_dialog);

                View view1= LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_receiced,null);
                TextView cancelTv=(TextView)view1.findViewById(R.id.cancelTv);
                cancelTv.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        dialog.dismiss();
                    }
                });

                TextView okTv=(TextView)view1.findViewById(R.id.okTv);
                okTv.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        dialog.dismiss();
                    }
                });
                dialog.setCanceledOnTouchOutside(true);
                dialog.setCancelable(true);
                dialog.addContentView(view1,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                dialog.show();
                break;
        }
    }
}

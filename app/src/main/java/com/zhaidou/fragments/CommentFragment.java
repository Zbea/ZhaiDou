package com.zhaidou.fragments;


import android.support.v4.app.Fragment;

/**
 * Author Scoield
 * Created at 15/9/28 15:24
 * Description:评论列表Fragment
 * FIXME
 */
public class CommentFragment extends Fragment {
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    private String mParam1;
//    private String mParam2;
//    private Context mContext;
//    private ListView mListView;
//    private TextView tv_test;
//    private CommentAdapter commentAdapter;
//    private CommentDialog mDialog;
//    InputMethodManager imm;
//
//    public static CommentFragment newInstance(String param1, String param2) {
//        CommentFragment fragment = new CommentFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    public CommentFragment() {
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        final View view = inflater.inflate(R.layout.fragment_comment, container, false);
//        mContext = getActivity();
//        imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
//        mDialog = new CommentDialog();
//        TextView mTitleView = (TextView) view.findViewById(R.id.title_tv);
//        mTitleView.setText(mContext.getResources().getString(R.string.title_comment_list));
//        tv_test = (TextView) view.findViewById(R.id.tv_test);
//        imm.showSoftInput(tv_test,InputMethodManager.SHOW_IMPLICIT);
//        tv_test.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mDialog.show(getChildFragmentManager(), "dialog");
//            }
//        });
//        view.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                if (inputMethodManager.isActive())
//                    inputMethodManager.hideSoftInputFromWindow(getActivity().getWindow().peekDecorView().getApplicationWindowToken(), 0);
//
//            }
//        });
//        mListView = (ListView) view.findViewById(R.id.lv_comment);
//        commentAdapter = new CommentAdapter(mContext, new ArrayList<Comment>());
//        commentAdapter.add(null);
//        commentAdapter.add(null);
//        commentAdapter.add(null);
//        commentAdapter.add(null);
//        commentAdapter.add(null);
//        commentAdapter.add(null);
//        commentAdapter.add(null);
//        commentAdapter.add(null);
//
//        mListView.setAdapter(commentAdapter);
//        return view;
//    }
//
//    public void onSend(String comment) {
//        System.out.println("CommentFragment.onSend");
//        System.out.println("comment = [" + comment + "]");
//    }
//
//    public class CommentAdapter extends BaseListAdapter<Comment> {
//        public CommentAdapter(Context context, List<Comment> list) {
//            super(context, list);
//        }
//
//        @Override
//        public View bindView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null)
//                convertView = mInflater.inflate(R.layout.item_comment_list, null);
//            return convertView;
//        }
//    }
//
//    private class CommentDialog extends DialogFragment {
//        private EditText et_comment;
//        private View dialogView;
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//            System.out.println("CommentDialog.onCreateView");
//            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
//            dialogView = inflater.inflate(R.layout.dialog_comment, container);
//            TextView tv_cancel = (TextView) dialogView.findViewById(R.id.tv_cancel);
//            TextView tv_send = (TextView) dialogView.findViewById(R.id.tv_send);
//            et_comment = (EditText) dialogView.findViewById(R.id.et_comment);
//            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//            tv_cancel.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    getDialog().dismiss();
//                }
//            });
//            tv_send.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String comment = et_comment.getText().toString().trim();
//                    if (TextUtils.isEmpty(comment)) {
//                        Toast.makeText(mContext, "评论不能为空哦!", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    CommentFragment commentFragment = (CommentFragment) getParentFragment();
//                    commentFragment.onSend(comment);
//                }
//            });
//            return dialogView;
//        }
//
//        @Override
//        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//            et_comment.requestFocus();
//            imm.showSoftInput(et_comment,InputMethodManager.SHOW_FORCED);
//        }
//
//        @Override
//        public void onStart() {
//            super.onStart();
//            DisplayMetrics dm = new DisplayMetrics();
//            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
//            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0xff000000));
//            getDialog().getWindow().setLayout(dm.widthPixels, getDialog().getWindow().getAttributes().height);
//            WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
//            layoutParams.width = dm.widthPixels;
//            layoutParams.gravity = Gravity.BOTTOM;
//            getDialog().getWindow().setAttributes(layoutParams);
//        }
//
//        @Override
//        public void onDismiss(DialogInterface dialog) {
//            imm.hideSoftInputFromWindow(getActivity().getWindow().peekDecorView().getApplicationWindowToken(), 0);
//            super.onDismiss(dialog);
//        }
//
//    }

}

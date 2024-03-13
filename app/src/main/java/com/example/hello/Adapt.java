package com.example.hello;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class Adapt extends BaseAdapter {

    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    ArrayList<SampleData> sample;

    public Adapt(Context context, ArrayList<SampleData> data) {
        mContext = context;
        sample = data;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return sample.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public SampleData getItem(int position) {
        return sample.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.listview_custom, null);

        TextView userView = view.findViewById(R.id.user_id);
        TextView contentView = view.findViewById(R.id.receipt_review_content);
        TextView dateView = view.findViewById(R.id.receipt_date);

        TextView recdate = view.findViewById(R.id.recdate);



        userView.setText(sample.get(position).getUser_id());
        contentView.setText(sample.get(position).getReceipt_review_content());
        dateView.setText(sample.get(position).getReceipt_date());


        return view;
    }



}

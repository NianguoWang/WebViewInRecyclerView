package com.wangng.webviewinrecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by wng on 2017/7/25.
 */

public class TextViewHolder extends RecyclerView.ViewHolder {
    private final TextView textView;

    public TextViewHolder(View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(R.id.textview);
    }

    public void setTitle(String title) {
        textView.setText(title);
    }
}

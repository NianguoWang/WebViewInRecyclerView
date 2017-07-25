package com.wangng.webviewinrecyclerview.slideconflict;

import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.wangng.webviewinrecyclerview.R;
import com.wangng.webviewinrecyclerview.TextViewHolder;
import com.wangng.webviewinrecyclerview.WebViewHolder;

/**
 * Created by wng on 2017/7/25.
 */

public class SlideConfictActivity extends AppCompatActivity {

    private static final String TAG = "SlideConfictActivity";
    private static final int ITEM_TYPE_TEXT_VIEW = 1;
    private static final int ITEM_TYPE_WEB_VIEW = 2;
    private static final int ITEM_VIEW_COUNT = 2;
    private RecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerview);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mAdapter = new RecyclerViewAdapter();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setOnTouchListener(new RecyclerViewOnTouchListener());
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter {

        private WebViewHolder webViewHolder;

        public WebViewHolder getWebViewHolder() {
            return webViewHolder;
        }
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if(viewType == ITEM_TYPE_TEXT_VIEW) {
                return new TextViewHolder(inflater.inflate(R.layout.item_textview, parent, false));
            }
            if(webViewHolder == null) {
                webViewHolder = new WebViewHolder(inflater.inflate(R.layout.item_webview, parent, false));
            }
            return webViewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(holder instanceof TextViewHolder) {
                ((TextViewHolder) holder).setTitle("解决方案二（解决冲突）");
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return ITEM_TYPE_TEXT_VIEW;
            }
            return ITEM_TYPE_WEB_VIEW;
        }

        @Override
        public int getItemCount() {
            return ITEM_VIEW_COUNT;
        }
    }

    private class RecyclerViewOnTouchListener implements View.OnTouchListener {

        private int mLastY;
        private int mCurrentY;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            WebViewHolder webViewHolder = mAdapter.getWebViewHolder();
            if(webViewHolder == null) {
                return false;
            }
            //获取WebView对象，以便将事件传递给他
            WebView webView = (WebView) webViewHolder.itemView.findViewById(R.id.web_view);
            //获取WebView所在item的顶部相对于其父控件（即RecyclerView的父控件）的距离
            int itemViewTop = webViewHolder.itemView.getTop();
            if(itemViewTop > 0) {
                return false;
            }
            if(itemViewTop < 0) {
                webViewHolder.itemView.scrollTo(0, 0);
                return false;
            }

            //计算dy，用来判断滑动方向。dy<0-->向上滑动；dy>0-->向下滑动。
            int dy = 0;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mLastY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mCurrentY = (int) event.getY();
                    dy = mCurrentY - mLastY;
                    mLastY = mCurrentY;
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    dy = (int) (event.getY() - mLastY);
                    mLastY = 0;
                    mCurrentY = 0;
                    break;
            }
            Log.d(TAG, "dy = " + dy);
            Log.d(TAG, "itemViewTop = " + itemViewTop);

            //如果WebView顶部距离其父控件距离未0，即WebView顶部滑动到RecyclerView父控件顶部重合时，
            // 此时需要拦截滑动事件交给WebView处理。
            if(itemViewTop == 0) {
                if(shouldIntercept(webView, dy)) {
                    webView.onTouchEvent(event);
                    return true;
                }
            }
            return false;
        }

        /**
         * 是否拦截滑动事件，判断的逻辑是：<br/>
         * 1,如果是向上滑动，并且webview能够向上滑动，则拦截事件；<br/>
         * 2,如果是向下滑动，并且webview能够向下滑动，则拦截事件。
         * @param view 判断能够滑动的view
         * @param dy 滑动间距
         * @return true拦截，false不拦截。
         */
        private boolean shouldIntercept(View view, int dy) {
            //canScrollVertically方法的第二个参数direction，传1时返回是否能够向上滑动，传-1时返回能否向下滑动。
            //dy<0-->向上滑动；dy>0-->向下滑动。
            boolean scrollUp = dy < 0 && ViewCompat.canScrollVertically(view, 1);
            boolean scrollDown = dy > 0 && ViewCompat.canScrollVertically(view, -1);
            return scrollUp || scrollDown || dy == 0;
        }
    }
}

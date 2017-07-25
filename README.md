### 前言

众所周知，RecyclerView是可以上下滑动的（当然根据对LayoutManager设置的不同也可以左右滑动），
而WebView也是可以上下左右滑动的。如果RecyclerView和WebView相互嵌套（即RecyclerView的一个条目为WebView），
就会产生滑动冲突。具体表现就是只有RecyclerView能滑动，WebView的滑动事件被拦截了。
原因也很好理解，如果你是RecyclerView的设计者，你也不会默认把滑动事件交给itemView去处理，因为这样很容易乱套，会出现很多奇奇怪怪的bug。
本项目提供了两种简单的解决方案。

### 解决方案一
方案一其实很简单，在布局里面设置WebView的高度为“wrap_content”。至于为什么这样就可以，我们先来复习一下wrap_content和match_parent
*  **wrap_content**
     wrap content翻译成汉语就是“包裹内容”，WebView的内容就是网页的内容，如果WebView的高度设置为“wrap_content”，那WebView的高度就是网页内容的高度。
* **match_parent**
     match parent即匹配父窗体，父控件有多高，高度设置成match_parent的View就有多高。
***
我们假设RecyclerView有两个条目（其中一个是WebView）。此时对WebView的高度设成wrap_content和match_parent时，对比如下：

![match_parent和wrap_content.png](http://upload-images.jianshu.io/upload_images/2989188-fe4a2793d2b3b4b9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

当WebView高度设置成wrap_content时，WebView加载的网页的内容在WebView里全部展现了，只需要滑动RecyclerView，就可以查看到未显示的内容了。
如果设置成match_parent，网页的内容并没有全部展示在WebView当中，需要滑动WebView来展示没有展现的剩下的内容；而此时WebView并不会获得滑动事件，所以剩下的内容永远也没有展现的机会了。

既然wrap_content能完美解决，又如此简单，就用这种方案好了，为什么还会有方案二呢？wrap_content会有些问题，就我发现的：
  1，如果网页会有弹窗，弹窗会显示在网页的正中间，也就是WebView的正中间，对照上图（1），正常情况下不会显示在屏幕范围内，需要向上滑动一段才能看见弹窗，这样对用户是不友好的；
  2，会造成部分JS代码执行错误。
如果设置成match_parent就没有这些问题；下面剩下的问题就是解决滑动冲突，在合适的时候将RecyclerView的事件传递给WebView。即下面的解决方案二。

### 解决方案二
其实思路很简单，重写RecyclerView的onTouchEvent方法，在合适的时候将事件传递给WebView。但是这样做需要写个自定义的RecyclerView然后覆盖onTouchEvent方法，比较麻烦。
View对外提供有setOnTouchListener的接口，只需要传一个OnTouchListener的对象，实现onTouch方法，对事件进行处理即可。
OnTouchListener的优先级比onTouchEvent的优先级要高，可以参见View的源码的dispatchTouchEvent方法：
```
public boolean dispatchTouchEvent(MotionEvent event) {
        //代码省略
        //noinspection SimplifiableIfStatement
        ListenerInfo li = mListenerInfo;
        //优先调用 li.mOnTouchListener.onTouch(this, event)，如果返回true，就不会调用onTouchEvent了
        if (li != null && li.mOnTouchListener != null
                && (mViewFlags & ENABLED_MASK) == ENABLED
                && li.mOnTouchListener.onTouch(this, event)) {
            result = true;
        }
        //
        if (!result && onTouchEvent(event)) {
            result = true;
        }
        //省略代码
        return result;
}
```
接下来的难点就是在**合适的时候**将事件传递给WebView了。直接看代码注释吧：
```
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
```
接下来把该OnTouchListener设置给RecyclerView就可以了。
```
recyclerView.setOnTouchListener(new RecyclerViewOnTouchListener());
```
具体逻辑代码注释已经写的很清楚了，这里就不再啰嗦了。

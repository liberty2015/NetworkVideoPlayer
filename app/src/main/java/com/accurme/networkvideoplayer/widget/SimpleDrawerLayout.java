package com.accurme.networkvideoplayer.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.accurme.networkvideoplayer.R;

/**
 * Created by Administrator on 2/12/2017.
 */

public class SimpleDrawerLayout extends ViewGroup {

    private Scroller scroller;

    private View drawer;

    private View content;

    private boolean hasDrawer=false;

    private boolean firstLayout=true;

    private int draweIndex;

    private int drawerWidth;


    public SimpleDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        scroller=new Scroller(getContext());
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new SimpleLayoutParam(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new SimpleLayoutParam(getContext(),attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new SimpleLayoutParam(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);
        int width=0;
        int height=0;

        int vCount=getChildCount();

        SimpleLayoutParam param=null;

        for (int i=0;i<vCount;i++){
            View childView=getChildAt(i);
            param= (SimpleLayoutParam) childView.getLayoutParams();
            width+=childView.getMeasuredWidth()+param.leftMargin+param.rightMargin;
            if (childView.getMeasuredHeight()>height){
                height=childView.getMeasuredHeight()+param.topMargin+param.bottomMargin;
            }
        }
        int widthFinal=(width>widthSize)?width:widthSize;
        setMeasuredDimension((widthMode==MeasureSpec.EXACTLY)?widthFinal:width,
                (heightMode==MeasureSpec.EXACTLY)?heightSize:height);
        for (int i=0;i<vCount;i++){

        }
//        measureChildren(widthMeasureSpec,heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int vCount=getChildCount();
        if (vCount>2){
            throw new IllegalStateException("the SimpleDrawerLayout's childView count can't be bigger than 2");
        }
        SimpleLayoutParam layoutParams=null;

        for (int i=0;i<vCount;i++){
            View childView = getChildAt(i);
            layoutParams= (SimpleLayoutParam) childView.getLayoutParams();
            boolean isDrawer=layoutParams.isDrawer;
            if (isDrawer&&firstLayout){
                if (!hasDrawer){
                    draweIndex=i;
                    hasDrawer=true;
                }else {
                    throw new IllegalStateException("the SimpleDrawerLayout's Drawer count can't be bigger than 1");
                }
                firstLayout=false;
                drawer=childView;
            }
            int left,top,right,bottom;
            int viewWidth;
            viewWidth=childView.getMeasuredWidth();
            top=0;
            bottom=childView.getMeasuredHeight();
            left=0;
            right=viewWidth;
            switch (i){
                case 0:{
                    if (isDrawer){
                        left=-viewWidth;
                        right=0;
                    }else {
                        left=0;
                        right=viewWidth;
                    }
                }
                break;
                case 1:{
                    if (isDrawer){
                        left=getMeasuredWidth();
                        right=getMeasuredWidth()+viewWidth;
                    }else {
                        if (hasDrawer){
                            left=0;
                            right=viewWidth;
                        }else {
                            int firstWidth=getChildAt(0).getMeasuredWidth();
                            left=firstWidth;
                            right=firstWidth+viewWidth;
                        }
                    }
                }
                break;
            }
            childView.layout(left,top,right,bottom);
        }
    }

    public void openDrawer(){
        switch (draweIndex){
            case 0:{
                scroller.startScroll(drawer.getLeft(),0,drawer.getWidth(),0);
            }
            break;
            case 1:{
                scroller.startScroll(drawer.getLeft(),0,-drawer.getWidth(),0);
            }
            break;
        }
    }

    public void closeDrawer(){
//        scroller.startScroll();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()){
            drawer.setX(scroller.getCurrX());
        }
    }



    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public class SimpleLayoutParam extends MarginLayoutParams{

        public boolean isDrawer;

        public SimpleLayoutParam(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray array = c.obtainStyledAttributes(attrs, R.styleable.SimpleDrawerLayout_Layout);
            isDrawer=array.getBoolean(R.styleable.SimpleDrawerLayout_Layout_isDrawer,false);
            array.recycle();
        }

        public SimpleLayoutParam(int width, int height) {
            super(width, height);
        }

        public SimpleLayoutParam(MarginLayoutParams source) {
            super(source);
        }

        public SimpleLayoutParam(LayoutParams source) {
            super(source);
        }
    }
}

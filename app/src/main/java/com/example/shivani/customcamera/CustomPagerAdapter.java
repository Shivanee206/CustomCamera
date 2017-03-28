package com.example.shivani.customcamera;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by Kanishk on 1/16/2017.
 */
public class CustomPagerAdapter extends PagerAdapter  {

    Context mContext;
    LayoutInflater mLayoutInflater;

    public CustomPagerAdapter(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return array_image.length;
    }
    private int[]array_image = new int[]{
    R.drawable.aa, R.drawable.bb};
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.custom_adapter, container, false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
       // imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageResource(array_image[position]);
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

}

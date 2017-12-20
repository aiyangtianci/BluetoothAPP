package com.example.buletoothdemo.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by 重新测量高度 on 2017/9/5.
 */

public class ListViewHeightMesure {



    public static void setAdapterHeight(ListView listView){

        android.widget.ListAdapter listAdapter = listView.getAdapter();//1、获取adapter
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0,j =listAdapter.getCount(); i < j ; i++) {  //2、算出没一个item高度总和
            View listItem = listAdapter.getView(i , null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params=listView.getLayoutParams();
        params.height = totalHeight + listView.getPaddingBottom()   //3、加上listview自身每行间距属性
                + listView.getPaddingTop()
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));


        listView.setLayoutParams(params);//4、重新设置高
    }
}

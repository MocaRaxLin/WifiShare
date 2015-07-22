package com.sharerececive.wifishare;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Justin on 2015/1/16.
 */
public class Data {
    Activity activity;
    ListView dataList;
    BaseAdapter adapter;
    int icon[] = {R.drawable.id_card, R.drawable.data_wifi_point};
    String dataName[] = {"User: ", "Wifi Point: "};
    String data[] = {"Name", "99999"};
    private static Data dataInstance = null;

    List<View> inList = new ArrayList();
    int index;

    public static Data getInstance() {
        if(dataInstance==null)
            dataInstance = new Data();
        return dataInstance;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        dataList = (ListView) activity.findViewById(R.id.lisvData);
        update();
    }

    public void set(String name,String point){
        inList.clear();
        data[0] = name;
        data[1] = point;
        update();
    }
    public void update(){
        setView();
        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return dataName.length;
            }

            @Override
            public Object getItem(int arg0) {
                return null;
            }

            @Override
            public long getItemId(int arg0) {
                return 0;
            }

            @Override
            public View getView(final int arg0, View arg1, ViewGroup arg2) {
                index = arg0;
                return inList.get(arg0);
            }
        };
        dataList.setAdapter(adapter);
    }

    public void setView() {
        for (int i = 0; i < dataName.length; i++) {
            View view = activity.getLayoutInflater().inflate(R.layout.data_list_style, null);

            ImageView imv = (ImageView) view.findViewById(R.id.imageView1);
            imv.setImageResource(icon[i]);

            TextView tv_name1 = (TextView) view.findViewById(R.id.textView1);
            tv_name1.setText(dataName[i]);

            TextView tv_name2 = (TextView) view.findViewById(R.id.textView2);
            tv_name2.setText(data[i]);

            inList.add(view);
        }
    }


//----實作接口---------------------------------------------------------------------------------------

    //可使用的函式
    public void setData(int order, String content){
        if(order < data.length){
            data[order] = content;
        }
    }
}
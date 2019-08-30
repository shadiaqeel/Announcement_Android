package com.pa.announcement;

/**
 * Created by dell on 04/08/2017.
 */


import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomAdapter extends ArrayAdapter<String>  {


    Context context;

    public CustomAdapter(Context context, int resourceId,ArrayList<String> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    /*private view holder class*/
    private class ViewHolder {
       ImageButton image;
        TextView txtTitle;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent) {



        ViewHolder holder = null;
        String Item = getItem(position);


        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);

            holder = new ViewHolder();

            holder.image = (ImageButton) convertView.findViewById(R.id.imageButton);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.seleced_Item);

            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();


            holder.txtTitle.setText(Item);
            holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_delete));

       final int Pos = position;
        holder.image.setOnClickListener(new ImageButton.OnClickListener()
        {


                @Override
                public void onClick (View v)
                {

                    ( (MainActivity)context).Delete(Pos);




                }

        });







        return convertView;
    }




    }

    /*
    <ImageButton
        android:id="@+id/imageButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentTop="false"
        android:layout_marginLeft="16dp"
        android:layout_alignParentRight="true"
        android:src="@drawable/ic_delete"
        android:background="@color/transparent"/>

    */
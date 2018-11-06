package com.leptons.keepmesafe;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class TrackingUserList extends RecyclerView.ViewHolder  implements View.OnClickListener{

    public TextView userName;
    ItemClickListener itemClickListener;

    public TrackingUserList (View itemView) {
        super(itemView);
        userName = (TextView) itemView.findViewById(R.id.txt_user);
    }


    public void setItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public void onClick(View view)
    {
        itemClickListener.onClick(view, getAdapterPosition());
    }

}

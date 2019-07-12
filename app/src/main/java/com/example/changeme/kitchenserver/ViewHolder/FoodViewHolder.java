package com.example.changeme.kitchenserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.changeme.kitchenserver.Common.Common;
import com.example.changeme.kitchenserver.Interface.ItemClickListener;
import com.example.changeme.kitchenserver.R;

/**
 * Created by SHEGZ on 1/4/2018.
 */
public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

    public TextView foodname;
    public ImageView foodimage;
    public ProgressBar progressBar2;

    private ItemClickListener itemClickListener;

    public FoodViewHolder(View itemView) {
        super(itemView);
        progressBar2 = itemView.findViewById(R.id.progress_load_photo1);
        foodname = (TextView) itemView.findViewById(R.id.food_name);
        foodimage = (ImageView) itemView.findViewById(R.id.food_image);
        itemView.setOnCreateContextMenuListener(this);
        itemView.setOnClickListener(this);


    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);

    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select the action");
        contextMenu.add(0, 0, getAdapterPosition(), Common.UPDATE);

        contextMenu.add(0, 0, getAdapterPosition(), Common.DELETE);
        contextMenu.add(0, 0, getAdapterPosition(), Common.ADDTOBANNER);
    }


}


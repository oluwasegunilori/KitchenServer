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
 * Created by SHEGZ on 1/3/2018.
 */
public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

    public ProgressBar progressBar;
    public TextView textmenuName;
    public ImageView imageView;

    private ItemClickListener itemClickListener;

    public MenuViewHolder(View itemView) {
        super(itemView);

        textmenuName = (TextView) itemView.findViewById(R.id.manu_name);
        imageView = (ImageView) itemView.findViewById(R.id.menu_image);
        progressBar = itemView.findViewById(R.id.progress_load_photo);
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
    }
}

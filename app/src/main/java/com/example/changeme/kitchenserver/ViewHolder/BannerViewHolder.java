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

public class BannerViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

    public TextView bannername;
    public ImageView bannerimage;
    public ProgressBar progressBar;

    private ItemClickListener itemClickListener;

    public BannerViewHolder(View itemView) {
        super(itemView);

        bannername = (TextView) itemView.findViewById(R.id.banner_name);
        bannerimage = (ImageView) itemView.findViewById(R.id.banner_image);
        progressBar = itemView.findViewById(R.id.progress_load_photosmall);
        itemView.setOnCreateContextMenuListener(this);

    }


    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select the action");
        contextMenu.add(0, 0, getAdapterPosition(), Common.UPDATE);

        contextMenu.add(0, 0, getAdapterPosition(), Common.DELETE);
    }
}


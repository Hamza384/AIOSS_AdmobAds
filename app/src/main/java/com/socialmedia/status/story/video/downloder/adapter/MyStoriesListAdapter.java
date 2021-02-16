package com.socialmedia.status.story.video.downloder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.socialmedia.status.story.video.downloder.R;
import com.socialmedia.status.story.video.downloder.databinding.ItemsWhatsappViewBinding;
import com.socialmedia.status.story.video.downloder.MyModel.story.ItemModel;

import java.util.ArrayList;

import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.RootDirectoryInsta;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.startDownload;

public class MyStoriesListAdapter extends RecyclerView.Adapter<MyStoriesListAdapter.ViewHolder> {
    private final Context mContext;
    private final ArrayList<ItemModel> storyItemModelList;

    public MyStoriesListAdapter(Context mContext, ArrayList<ItemModel> list) {
        this.mContext = mContext;
        this.storyItemModelList = list;
    }

    @NonNull
    @Override
    public MyStoriesListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        return new ViewHolder(DataBindingUtil.inflate(layoutInflater, R.layout.items_whatsapp_view, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyStoriesListAdapter.ViewHolder viewHolder, int position) {
        ItemModel itemModel = storyItemModelList.get(position);
        try {
            if (itemModel.getMedia_type()==2) {
                viewHolder.itemsWhatsappViewBinding.ivPlay.setVisibility(View.VISIBLE);
            } else {
                viewHolder.itemsWhatsappViewBinding.ivPlay.setVisibility(View.GONE);
            }
            Glide.with(mContext)
                    .load(itemModel.getImage_versions2().getCandidates().get(0).getUrl())
                    .into(viewHolder.itemsWhatsappViewBinding.pcw);

        }catch (Exception ex){
            ex.printStackTrace();
        }

        viewHolder.itemsWhatsappViewBinding.tvDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemModel.getMedia_type()==2) {
                    startDownload(itemModel.getVideo_versions().get(0).getUrl(),
                            RootDirectoryInsta, mContext,"story_"+itemModel.getId()+".mp4" );
                }else {
                    startDownload(itemModel.getImage_versions2().getCandidates().get(0).getUrl(),
                            RootDirectoryInsta, mContext, "story_"+itemModel.getId()+".png");
                }
            }
        });


    }
    @Override
    public int getItemCount() {
        return storyItemModelList == null ? 0 : storyItemModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
         ItemsWhatsappViewBinding itemsWhatsappViewBinding;
        public ViewHolder(ItemsWhatsappViewBinding mbinding) {
            super(mbinding.getRoot());
            this.itemsWhatsappViewBinding = mbinding;
        }
    }
}
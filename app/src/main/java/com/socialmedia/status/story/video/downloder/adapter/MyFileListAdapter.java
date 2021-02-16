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
import com.socialmedia.status.story.video.downloder.databinding.ItemsFileViewBinding;
import com.socialmedia.status.story.video.downloder.MyInterfaces.FileListClickInterface;
import java.io.File;
import java.util.ArrayList;


public class MyFileListAdapter extends RecyclerView.Adapter<MyFileListAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<File> fileArrayList;
    private LayoutInflater layoutInflater;
    private FileListClickInterface fileListClickInterface;

    public MyFileListAdapter(Context mContext, ArrayList<File> files, FileListClickInterface fileListClickInterface) {
        this.mContext = mContext;
        this.fileArrayList = files;
        this.fileListClickInterface=  fileListClickInterface;
    }

    @NonNull
    @Override
    public MyFileListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(viewGroup.getContext());
        }
        return new ViewHolder(DataBindingUtil.inflate(layoutInflater, R.layout.items_file_view, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyFileListAdapter.ViewHolder viewHolder, int i) {
        File fileItem = fileArrayList.get(i);

        try {
            String fileExtension = fileItem.getName().substring(fileItem.getName().lastIndexOf("."));
            if (fileExtension.equals(".mp4")) {
                viewHolder.mbinding.ivPlay.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mbinding.ivPlay.setVisibility(View.GONE);
            }
            Glide.with(mContext)
                    .load(fileItem.getPath())
                    .into(viewHolder.mbinding.pc);
        }catch (Exception ex){
        }

        viewHolder.mbinding.rlMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileListClickInterface.getPosition(i,fileItem);
            }
        });
    }


    @Override
    public int getItemCount() {
        return fileArrayList == null ? 0 : fileArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemsFileViewBinding mbinding;

        public ViewHolder(ItemsFileViewBinding mbinding) {
            super(mbinding.getRoot());
            this.mbinding = mbinding;
        }
    }
}
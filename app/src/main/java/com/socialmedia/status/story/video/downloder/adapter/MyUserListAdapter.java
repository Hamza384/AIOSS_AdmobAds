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
import com.socialmedia.status.story.video.downloder.databinding.ItemUserListBinding;
import com.socialmedia.status.story.video.downloder.MyInterfaces.UserListInterface;
import com.socialmedia.status.story.video.downloder.MyModel.story.MyTrayModel;

import java.util.ArrayList;

public class MyUserListAdapter extends RecyclerView.Adapter<MyUserListAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<MyTrayModel> myTrayModelArrayList;
    private UserListInterface userListInterface;

    public MyUserListAdapter(Context mContext, ArrayList<MyTrayModel> list, UserListInterface listInterface) {
        this.mContext = mContext;
        this.myTrayModelArrayList = list;
        this.userListInterface = listInterface;
    }

    @NonNull
    @Override
    public MyUserListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        return new ViewHolder(DataBindingUtil.inflate(layoutInflater, R.layout.item_user_list, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyUserListAdapter.ViewHolder viewHolder, int position) {

        viewHolder.itemUserListBinding.realName.setText(myTrayModelArrayList.get(position).getUser().getFull_name());
        Glide.with(mContext).load(myTrayModelArrayList.get(position).getUser().getProfile_pic_url())
                .thumbnail(0.2f).into(viewHolder.itemUserListBinding.storyPc);

        viewHolder.itemUserListBinding.RLStoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userListInterface.userListClick(position, myTrayModelArrayList.get(position));
            }
        });

    }
    @Override
    public int getItemCount() {
        return myTrayModelArrayList == null ? 0 : myTrayModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
         ItemUserListBinding itemUserListBinding;
        public ViewHolder(ItemUserListBinding mbinding) {
            super(mbinding.getRoot());
            this.itemUserListBinding = mbinding;
        }
    }
}
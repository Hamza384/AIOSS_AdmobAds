package com.socialmedia.status.story.video.downloder.MyFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.socialmedia.status.story.video.downloder.R;
import com.socialmedia.status.story.video.downloder.MyActivity.MyFullViewActivity;
import com.socialmedia.status.story.video.downloder.adapter.MyFileListAdapter;
import com.socialmedia.status.story.video.downloder.databinding.FragmentHistoryBinding;
import com.socialmedia.status.story.video.downloder.MyInterfaces.FileListClickInterface;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static androidx.databinding.DataBindingUtil.inflate;

public class MyAllinOneGalleryFragment extends Fragment implements FileListClickInterface {
    private FragmentHistoryBinding fragmentHistoryBinding;
    MyFileListAdapter myFileListAdapter;
    private ArrayList<File> fileArrayList;
    private Activity activity;
    File mediaPath;
    RecyclerView.LayoutManager mLayoutManager;
    public MyAllinOneGalleryFragment(File filesOfMedia){
        mediaPath=filesOfMedia;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        activity = getActivity();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString("m");
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        activity = getActivity();
        getAllFiles();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentHistoryBinding = inflate(inflater, R.layout.fragment_history, container, false);
        initViews();
        return fragmentHistoryBinding.getRoot();
    }
    private void initViews(){
        fragmentHistoryBinding.swiperefresh.setOnRefreshListener(() -> {
            getAllFiles();
            fragmentHistoryBinding.swiperefresh.setRefreshing(false);
        });
    }

    private void getAllFiles(){
        fileArrayList = new ArrayList<>();
            mLayoutManager = new GridLayoutManager(activity,3);
        fragmentHistoryBinding.rvFileList.setLayoutManager(mLayoutManager);

        File[] files = mediaPath.listFiles();
        if (files!=null) {
            fileArrayList.addAll(Arrays.asList(files));
            myFileListAdapter = new MyFileListAdapter(activity, fileArrayList, MyAllinOneGalleryFragment.this);
            fragmentHistoryBinding.rvFileList.setAdapter(myFileListAdapter);
            if (files.length>0){
                fragmentHistoryBinding.tvNoResult.setVisibility(View.GONE);
                fragmentHistoryBinding.swiperefresh.setVisibility(View.VISIBLE);
            }else {
                fragmentHistoryBinding.swiperefresh.setVisibility(View.GONE);
                fragmentHistoryBinding.tvNoResult.setVisibility(View.VISIBLE);
            }
        }else {
            fragmentHistoryBinding.swiperefresh.setVisibility(View.GONE);
            fragmentHistoryBinding.tvNoResult.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void getPosition(int position, File file) {
        Intent inNext = new Intent(activity, MyFullViewActivity.class);
        inNext.putExtra("ImageDataFile", fileArrayList);
        inNext.putExtra("Position", position);
        activity.startActivity(inNext);
    }
}

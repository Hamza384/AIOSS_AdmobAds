package com.socialmedia.status.story.video.downloder.MyFragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.socialmedia.status.story.video.downloder.R;
import com.socialmedia.status.story.video.downloder.MyActivity.MyFullViewActivity;
import com.socialmedia.status.story.video.downloder.MyActivity.MyGalleryActivity;
import com.socialmedia.status.story.video.downloder.adapter.MyFileListAdapter;
import com.socialmedia.status.story.video.downloder.databinding.FragmentHistoryBinding;
import com.socialmedia.status.story.video.downloder.MyInterfaces.FileListClickInterface;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.ArrayList;

import static androidx.databinding.DataBindingUtil.inflate;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.RootDirectoryWhatsappShow;

public class MyWhatsAppDowndlededFragment extends Fragment implements FileListClickInterface {
    private FragmentHistoryBinding fragmentHistoryBinding;
    private MyFileListAdapter myFileListAdapter;
    private ArrayList<File> fileArrayList;
    private MyGalleryActivity myGalleryActivity;
    public static MyWhatsAppDowndlededFragment newInstance(String param1) {
        MyWhatsAppDowndlededFragment fragment = new MyWhatsAppDowndlededFragment();
        Bundle args = new Bundle();
        args.putString("m", param1);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onAttach(@NotNull Context _context) {
        super.onAttach(_context);
        myGalleryActivity = (MyGalleryActivity) _context;
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
        myGalleryActivity = (MyGalleryActivity) getActivity();
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
        File[] files = RootDirectoryWhatsappShow.listFiles();
        if (files!=null) {
            for (File file : files) {
                fileArrayList.add(file);
            }
            myFileListAdapter = new MyFileListAdapter(myGalleryActivity, fileArrayList, MyWhatsAppDowndlededFragment.this);
            fragmentHistoryBinding.rvFileList.setAdapter(myFileListAdapter);
        }
    }
    @Override
    public void getPosition(int position, File file) {
        Intent inNext = new Intent(myGalleryActivity, MyFullViewActivity.class);
        inNext.putExtra("ImageDataFile", fileArrayList);
        inNext.putExtra("Position", position);
        myGalleryActivity.startActivity(inNext);
    }
}

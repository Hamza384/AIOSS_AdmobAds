package com.socialmedia.status.story.video.downloder.MyFragment;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.socialmedia.status.story.video.downloder.R;
import com.socialmedia.status.story.video.downloder.adapter.WhatsappStatusAdapter;
import com.socialmedia.status.story.video.downloder.databinding.FragmentWhatsappImageBinding;
import com.socialmedia.status.story.video.downloder.MyModel.MyWhatsappStatusModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import static androidx.databinding.DataBindingUtil.inflate;

public class MyWhatsappVideoFragment extends Fragment {
    FragmentWhatsappImageBinding fragmentWhatsappImageBinding;

    private File[] allfiles;
    ArrayList<MyWhatsappStatusModel> statusModelArrayList;
    private WhatsappStatusAdapter whatsappStatusAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentWhatsappImageBinding = inflate(inflater, R.layout.fragment_whatsapp_image, container, false);
        initViews();
        return fragmentWhatsappImageBinding.getRoot();
    }

    private void initViews() {
        statusModelArrayList = new ArrayList<>();
        getData();
        fragmentWhatsappImageBinding.swiperefresh.setOnRefreshListener(() -> {
            statusModelArrayList = new ArrayList<>();
            getData();
            fragmentWhatsappImageBinding.swiperefresh.setRefreshing(false);
        });

    }

    private void getData() {
        MyWhatsappStatusModel myWhatsappStatusModel;
        String targetPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/.Statuses";
        File targetDirector = new File(targetPath);
        allfiles = targetDirector.listFiles();

        String targetPathBusiness = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp Business/Media/.Statuses";
        File targetDirectorBusiness = new File(targetPathBusiness);
        File[] allfilesBusiness = targetDirectorBusiness.listFiles();


        try {
            Arrays.sort(allfiles, (Comparator) (o1, o2) -> {
                if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                    return -1;
                } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                    return +1;
                } else {
                    return 0;
                }
            });

            for (int i = 0; i < allfiles.length; i++) {
                File file = allfiles[i];
                if (Uri.fromFile(file).toString().endsWith(".mp4")) {
                    myWhatsappStatusModel = new MyWhatsappStatusModel("WhatsStatus: " + (i + 1),
                            Uri.fromFile(file),
                            allfiles[i].getAbsolutePath(),
                            file.getName());
                    statusModelArrayList.add(myWhatsappStatusModel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Arrays.sort(allfilesBusiness, (Comparator) (o1, o2) -> {
                if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                    return -1;
                } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                    return +1;
                } else {
                    return 0;
                }
            });

            for (int i = 0; i < allfilesBusiness.length; i++) {
                File file = allfilesBusiness[i];
                if (Uri.fromFile(file).toString().endsWith(".mp4")) {
                    myWhatsappStatusModel = new MyWhatsappStatusModel("WhatsStatusB: " + (i + 1),
                            Uri.fromFile(file),
                            allfilesBusiness[i].getAbsolutePath(),
                            file.getName());
                    statusModelArrayList.add(myWhatsappStatusModel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (statusModelArrayList.size()!=0) {
            fragmentWhatsappImageBinding.tvNoResult.setVisibility(View.GONE);
        } else {
            fragmentWhatsappImageBinding.tvNoResult.setVisibility(View.VISIBLE);
        }
        whatsappStatusAdapter = new WhatsappStatusAdapter(getActivity(), statusModelArrayList);
        fragmentWhatsappImageBinding.rvFileList.setAdapter(whatsappStatusAdapter);

    }
}

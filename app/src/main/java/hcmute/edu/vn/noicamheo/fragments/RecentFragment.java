package hcmute.edu.vn.noicamheo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.adapter.RecentAdapter;
import hcmute.edu.vn.noicamheo.entity.ERecentCallType;
import hcmute.edu.vn.noicamheo.entity.Recent;

public class RecentFragment extends Fragment {
    RecyclerView recyclerViewRecent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recent, container, false);

        List<Object> recents = new ArrayList<>();
        recents.add("Saturday, March 9");
        recents.add(new Recent("Bien Xuan Huy", "0987654321", ERecentCallType.TYPE_MADE, true));
        recents.add(new Recent("Bien Xuan Huy", "0987654321", ERecentCallType.TYPE_MISSED_OUTGOING, true));
        recents.add("Saturday, March 8");
        recents.add(new Recent("Chi Shipper xinh dep", "0246813579", ERecentCallType.TYPE_RECEIVED, true));
        recents.add(new Recent("02868855087", "02868855087", ERecentCallType.TYPE_MISSED, false));

        recyclerViewRecent = view.findViewById(R.id.recyclerViewRecentHolder);
        recyclerViewRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRecent.setAdapter(new RecentAdapter(getContext(), recents));

        return view;
    }
}
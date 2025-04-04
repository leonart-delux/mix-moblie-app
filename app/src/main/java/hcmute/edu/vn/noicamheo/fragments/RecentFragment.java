package hcmute.edu.vn.noicamheo.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.adapter.RecentAdapter;
import hcmute.edu.vn.noicamheo.entity.ERecentCallType;
import hcmute.edu.vn.noicamheo.entity.Recent;

public class RecentFragment extends Fragment {
    RecyclerView recyclerViewRecent;
    private static final int REQUEST_CODE_CONTACT = 102;        // Code to request permission
    private List<Object> recents = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recent, container, false);

        // Load component on UI
        recyclerViewRecent = view.findViewById(R.id.recyclerViewRecentHolder);

        // Check for permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_CALL_LOG}, REQUEST_CODE_CONTACT);
        }

        // Load data
        recents = loadCallHistory();

        // Append data with recycler
        recyclerViewRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRecent.setAdapter(new RecentAdapter(getContext(), recents));

        return view;
    }

    private List<Object> loadCallHistory() {
        List <Object> recents = new ArrayList<>();

        // Load call history order by date
        Cursor cursor = requireActivity().getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                CallLog.Calls.DATE + " DESC"
        );

        // Resolve data from cursor
        if (cursor != null) {
            // First get index of each column
            int nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
            int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
            int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);
            int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);

            // Map each record into a recent object
            while (cursor.moveToNext()) {
                // Load basic information
                String name = cursor.getString(nameIndex);
                String number = cursor.getString(numberIndex);
                long dateMillis = cursor.getLong(dateIndex);
                int duration = cursor.getInt(durationIndex);
                int type = cursor.getInt(typeIndex);

                ERecentCallType callType;
                switch (type) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        callType = ERecentCallType.TYPE_MADE;
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        callType = ERecentCallType.TYPE_MISSED;
                        break;
                    default:
                        callType = ERecentCallType.TYPE_RECEIVED;
                        break;
                }

                // If name null --> not exist in contact
                boolean isInContact =  true;
                if (name == null) {
                    name = number;
                    isInContact = false;
                }

                // Add recent object to list
                recents.add(new Recent(name, number, callType, isInContact, new Date(dateMillis), duration));
            }
            cursor.close();
        }

        return recents;
    }
}
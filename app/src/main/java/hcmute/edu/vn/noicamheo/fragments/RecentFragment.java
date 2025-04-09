package hcmute.edu.vn.noicamheo.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.adapter.RecentAdapter;
import hcmute.edu.vn.noicamheo.entity.Contact;
import hcmute.edu.vn.noicamheo.entity.ERecentCallType;
import hcmute.edu.vn.noicamheo.entity.Recent;

public class RecentFragment extends Fragment {
    RecyclerView recyclerViewRecent;
    SearchView searchView;
    RecentAdapter recentAdapter;
    private static final int REQUEST_CODE_CONTACT = 102;        // Code to request permission
    private List<Object> recents = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recent, container, false);

        // Load component on UI
        recyclerViewRecent = view.findViewById(R.id.recyclerViewRecentHolder);
        searchView = view.findViewById(R.id.searchViewRecent);

        // Check for permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_CALL_LOG}, REQUEST_CODE_CONTACT);
        }

        // Load data
        recents = loadCallHistory();
        addContactListHeader(recents);

        // Append data with recycler
        recyclerViewRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        recentAdapter = new RecentAdapter(getContext(), recents);
        recyclerViewRecent.setAdapter(recentAdapter);

        // Set event for search bar
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return false;
            }
        });

        return view;
    }

    private void filterList(String newText) {
        List<Object> filteredList = new ArrayList<>();
        for (Object object: recents) {
            if (object instanceof Recent && (
                    ((Recent) object).getFullName().toLowerCase().contains(newText.toLowerCase()) ||
                            ((Recent) object).getPhoneNumber().contains(newText)
            )) {
                filteredList.add(object);
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "No result found!", Toast.LENGTH_SHORT).show();
        } else {
            addContactListHeader(filteredList);
            recentAdapter.setFilteredList(filteredList);
        }
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
                if (name == null || name.isEmpty()) {
                    name = number;
                    isInContact = false;
                }

                // Convert date from millisecond to Calender
                Calendar date = Calendar.getInstance();
                date.setTimeInMillis(dateMillis);

                // Add recent object to list
                recents.add(new Recent(name, number, callType, isInContact, date, duration));
            }
            cursor.close();
        }

        return recents;
    }

    private void addContactListHeader(List<Object> recentList) {
        ListIterator<Object> iterator = recentList.listIterator();
        Calendar lastDate = null;

        while (iterator.hasNext()) {
            Object recentElement = iterator.next();

            if (!(recentElement instanceof Recent)) {
                continue;
            }

            // Current item date
            Calendar currentDate = ((Recent) recentElement).getDate();

            if (lastDate == null || !isSameDay(currentDate, lastDate)) {
                lastDate = (Calendar) currentDate.clone();

                // Format date header
                String dateString = android.text.format.DateFormat.format("EEE, dd/MM/yyyy", currentDate).toString();

                iterator.previous();
                iterator.add(dateString);
                iterator.next();
            }
        }
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
                && c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
    }
}
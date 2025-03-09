package hcmute.edu.vn.noicamheo.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import hcmute.edu.vn.noicamheo.fragments.ContactFragment;
import hcmute.edu.vn.noicamheo.fragments.DialFragment;
import hcmute.edu.vn.noicamheo.fragments.RecentFragment;

public class ContactViewPagerAdapter extends FragmentStateAdapter {
    public ContactViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new RecentFragment();
            case 2:
                return new ContactFragment();
            default:
                return new DialFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

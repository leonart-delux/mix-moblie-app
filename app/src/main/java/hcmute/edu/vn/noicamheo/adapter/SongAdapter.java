package hcmute.edu.vn.noicamheo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import hcmute.edu.vn.noicamheo.R;
import hcmute.edu.vn.noicamheo.entity.Song;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Song song);
    }

    public SongAdapter(Context context, List<Song> songList, OnItemClickListener listener) {
        this.context = context;
        this.songList = songList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.tvSongName.setText(song.getTitle());
        holder.tvArtistName.setText(song.getArtist());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(song));
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvSongName, tvArtistName;
        ImageView imgSongThumb;
        ImageButton btnMore;

        public SongViewHolder(View itemView) {
            super(itemView);
            tvSongName = itemView.findViewById(R.id.tv_song_name);
            tvArtistName = itemView.findViewById(R.id.tv_artist_name);
            imgSongThumb = itemView.findViewById(R.id.img_song_thumb);
            btnMore = itemView.findViewById(R.id.btn_more);
        }
    }
}
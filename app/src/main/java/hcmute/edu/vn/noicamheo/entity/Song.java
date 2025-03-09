package hcmute.edu.vn.noicamheo.entity;

public class Song {
    private String title;
    private String artist;
    private String path;
    public Song(String title, String artist, String path) {
        this.title = title;
        this.artist = artist;
        this.path = path;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getPath() { return path; }
}
package hcmute.edu.vn.noicamheo.entity;

public class Item {
    String date;
    int day;
    String month;
    String title;
    String time;
    String description;

    public Item( int day, String date, String month, String title, String time, String description) {
        this.date = date;
        this.day = day;
        this.month = month;
        this.title = title;
        this.time = time;
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {  // Sửa tên phương thức đúng theo quy tắc getter/setter
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}


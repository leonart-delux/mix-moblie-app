package hcmute.edu.vn.noicamheo.entity;

import java.util.Date;

public class Recent {
    private String fullName;
    private String phoneNumber;
    private ERecentCallType eRecentCallType;
    private boolean isInContact;
    private Date date;

    private int duration;

    public Recent(String fullName, String phoneNumber, ERecentCallType eRecentCallType, boolean isInContact, Date date, int duration) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.eRecentCallType = eRecentCallType;
        this.isInContact = isInContact;
        this.date = date;
        this.duration = duration;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public ERecentCallType geteRecentCallType() {
        return eRecentCallType;
    }

    public boolean isInContact() {
        return isInContact;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void seteRecentCallType(ERecentCallType eRecentCallType) {
        this.eRecentCallType = eRecentCallType;
    }

    public void setInContact(boolean inContact) {
        isInContact = inContact;
    }


    public void setDate(Date date) {
        this.date = date;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Date getDate() {
        return date;
    }

    public int getDuration() {
        return duration;
    }
}

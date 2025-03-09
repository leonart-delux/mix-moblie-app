package hcmute.edu.vn.noicamheo.entity;

public class Recent {
    private String fullName;
    private String phoneNumber;
    private ERecentCallType eRecentCallType;
    private boolean isInContact;

    public Recent(String fullName, String phoneNumber, ERecentCallType eRecentCallType, boolean isInContact) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.eRecentCallType = eRecentCallType;
        this.isInContact = isInContact;
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
}

package kr.dogfoot.webserver.loader.resourcesetting;

public class NotServicedFile {
    private String namePatten;
    private boolean inheritable;

    public NotServicedFile() {
    }


    public NotServicedFile(String namePatten, boolean inheritable) {
        this.namePatten = namePatten;
        this.inheritable = inheritable;
    }

    public String namePatten() {
        return namePatten;
    }

    public void namePatten(String namePatten) {
        this.namePatten = namePatten;
    }

    public boolean isInheritable() {
        return inheritable;
    }

    public void setInheritable(boolean inheritable) {
        this.inheritable = inheritable;
    }
}

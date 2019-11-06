package kr.dogfoot.webserver.loader.resourcesetting;

public class VirtualDirectorySetting extends DirectorySetting {
    private String sourcePath;

    public VirtualDirectorySetting() {
    }

    @Override
    public SettingType type() {
        return SettingType.VirtualDirectory;
    }

    public String sourcePath() {
        return sourcePath;
    }

    public void sourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }
}

package ScanHub.BE;

public class FileSettings {

    private int fileSettingsId;
    private double hue;
    private double brightness;
    private double contrast;
    private double saturation;
    private int globalRotation;

    public FileSettings(int fileSettingsId, double hue, double brightness, double contrast, double saturation, int globalRotation) {
        this.fileSettingsId = fileSettingsId;
        this.hue = hue;
        this.brightness = brightness;
        this.contrast = contrast;
        this.saturation = saturation;
        this.globalRotation = globalRotation;
    }

    public FileSettings(double hue, double brightness, double contrast, double saturation, int globalRotation) {
        this.hue = hue;
        this.brightness = brightness;
        this.contrast = contrast;
        this.saturation = saturation;
        this.globalRotation = globalRotation;
    }

    public int getFileSettingsId() {
        return fileSettingsId;
    }
    public double getHue() {
        return hue;
    }
    public double getBrightness() {
        return brightness;
    }
    public double getContrast() {
        return contrast;
    }
    public double getSaturation() {
        return saturation;
    }
    public int getGlobalRotation() {return globalRotation;}

    public void setHue(double hue) {
        this.hue = hue;
    }
    public void setBrightness(double brightness) {
        this.brightness = brightness;
    }
    public void setContrast(double contrast) {
        this.contrast = contrast;
    }
    public void setSaturation(double saturation) {
        this.saturation = saturation;
    }
    public void setGlobalRotation(int globalRotation) {this.globalRotation = globalRotation;}
}

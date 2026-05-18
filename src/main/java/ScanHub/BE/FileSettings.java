package ScanHub.BE;

public class FileSettings {

    private int fileSettingsId;
    private int rotation;
    private double hue;
    private double brightness;
    private double contrast;
    private double saturation;

    public FileSettings() {}

    public FileSettings(int rotation, int fileSettingsId, double hue, double brightness, double contrast, double saturation) {
        this.fileSettingsId = fileSettingsId;
        this.hue = hue;
        this.brightness = brightness;
        this.contrast = contrast;
        this.saturation = saturation;
        this.rotation = rotation;
    }

    public FileSettings( int rotation, double hue, double brightness, double contrast, double saturation) {
        this.rotation = rotation;
        this.hue = hue;
        this.brightness = brightness;
        this.contrast = contrast;
        this.saturation = saturation;
    }

    public int getFileSettingsId() { return fileSettingsId; }
    public int getRotation()       { return rotation; }
    public double getHue()         { return hue; }
    public double getBrightness()  { return brightness; }
    public double getContrast()    { return contrast; }
    public double getSaturation()  { return saturation; }

    public void setRotation(int rotation)        { this.rotation = rotation; }
    public void setHue(double hue)               { this.hue = hue; }
    public void setBrightness(double brightness) { this.brightness = brightness; }
    public void setContrast(double contrast)     { this.contrast = contrast; }
    public void setSaturation(double saturation) { this.saturation = saturation; }
}

package ScanHub.BE;

public class FileAdjustmentSettings {

    private int rotation;
    private double hue;
    private double brightness;
    private double contrast;
    private double saturation;

    public FileAdjustmentSettings() {}

    public FileAdjustmentSettings(int rotation, double hue, double brightness, double contrast, double saturation) {
        setRotation(rotation);
        setHue(hue);
        setBrightness(brightness);
        setContrast(contrast);
        setSaturation(saturation);
    }

    public FileAdjustmentSettings(FileAdjustmentSettings source) {
        if (source == null) return;
        this.rotation = source.rotation;
        this.hue = source.hue;
        this.brightness = source.brightness;
        this.contrast = source.contrast;
        this.saturation = source.saturation;
    }

    public static FileAdjustmentSettings copyOf(FileAdjustmentSettings source) {
        return new FileAdjustmentSettings(source);
    }

    public int getRotation()       { return rotation; }
    public double getHue()         { return hue; }
    public double getBrightness()  { return brightness; }
    public double getContrast()    { return contrast; }
    public double getSaturation()  { return saturation; }

    public void setRotation(int rotation)        { this.rotation = normaliseRotation(rotation); }
    public void setHue(double hue)               { this.hue = hue; }
    public void setBrightness(double brightness) { this.brightness = brightness; }
    public void setContrast(double contrast)     { this.contrast = contrast; }
    public void setSaturation(double saturation) { this.saturation = saturation; }

    private int normaliseRotation(int rotation) {
        return ((rotation % 360) + 360) % 360;
    }
}

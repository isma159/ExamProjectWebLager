package ScanHub.BE;

public class FileAdjustmentSettings {

    private int rotation;
    private double hue;
    private double brightness;
    private double contrast;
    private double saturation;
    private double sharpness;

    public FileAdjustmentSettings() {}

    public FileAdjustmentSettings(int rotation, double hue, double brightness, double contrast, double saturation, double sharpness) {
        setRotation(rotation);
        setHue(hue);
        setBrightness(brightness);
        setContrast(contrast);
        setSaturation(saturation);
        setSharpness(sharpness);
    }

    public FileAdjustmentSettings(FileAdjustmentSettings source) {
        if (source == null) return;
        this.rotation = source.rotation;
        this.hue = source.hue;
        this.brightness = source.brightness;
        this.contrast = source.contrast;
        this.saturation = source.saturation;
        this.sharpness = source.sharpness;
    }

    public static FileAdjustmentSettings copyOf(FileAdjustmentSettings source) {
        return new FileAdjustmentSettings(source);
    }

    public int getRotation()       { return rotation; }
    public double getHue()         { return hue; }
    public double getBrightness()  { return brightness; }
    public double getContrast()    { return contrast; }
    public double getSaturation()  { return saturation; }
    public double getSharpness()   { return sharpness;}

    public void setRotation(int rotation)        { this.rotation = normaliseRotation(rotation); }
    public void setHue(double hue)               { this.hue = hue; }
    public void setBrightness(double brightness) { this.brightness = brightness; }
    public void setContrast(double contrast)     { this.contrast = contrast; }
    public void setSaturation(double saturation) { this.saturation = saturation; }
    public void setSharpness(double sharpness)   { this.sharpness = sharpness;}

    private int normaliseRotation(int rotation) {
        return ((rotation % 360) + 360) % 360;
    }
}

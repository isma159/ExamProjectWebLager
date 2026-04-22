package ScanHub.BE;

public class Profile {

    private int id;
    private String name;
    private String splitBehaviour;

    public Profile(int id, String name, String splitBehaviour) {
        this.id = id;
        this.name = name;
        this.splitBehaviour = splitBehaviour;
    }

    public Profile(String name, String splitBehaviour) {
        this.name = name;
        this.splitBehaviour = splitBehaviour;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getSplitBehaviour() {
        return splitBehaviour;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setSplitBehaviour(String splitBehaviour) {
        this.splitBehaviour = splitBehaviour;
    }


}

public class Material {
    private String matId;
    private String matName;
    private double matCost;
    private int matQuantity;

    public Material(String matId, String matName, double matCost, int matQuantity) {
        this.matId = matId; this.matName = matName;
        this.matCost = matCost; this.matQuantity = matQuantity;
    }

    // Getters and Setters
    public String getMatId() { return matId; }
    public String getMatName() { return matName; }
    public double getMatCost() { return matCost; }
    public int getMatQuantity() { return matQuantity; }
}
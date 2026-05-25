import java.util.*;

public class Service {
    private String serviceId;
    private double serviceCost;
    private String serviceName;
    private String description;
    private String projName;
    private List<Material> materials = new ArrayList<>(); // Composition Relationship

    public Service(String serviceId, String serviceName, double serviceCost) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.serviceCost = serviceCost;
    }

    // Composition method
    public void addMaterial(Material m) { materials.add(m); }

    //GETTERS and SETTERS
    public String getServiceId() { return serviceId; }
    public String getServiceName() { return serviceName; }
    public double getServiceCost() { return serviceCost; }
    public List<Material> getMaterials() { return materials; }
    public String getDescription() { return description; }
    public String getProjName() { return projName; }
    public void setDescription(String description) { this.description = description; }
    public void setProjName(String projName) { this.projName = projName; }

    // Diagram Methods
    public double showServicesCost() {
        double matTotal = materials.stream().mapToDouble(m -> m.getMatCost() * m.getMatQuantity()).sum();
        return serviceCost + matTotal;
    }
}
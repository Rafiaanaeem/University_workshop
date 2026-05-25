public class Worker {
    private String workId;
    private String workFname;
    private String workLname;
    private String serviceIncharge;
    private boolean availability;
    private String assignedProject;

    public Worker(String id, String fn, String ln, String si, boolean av, String ap) {
        this.workId = id; this.workFname = fn; this.workLname = ln;
        this.serviceIncharge = si; this.availability = av; this.assignedProject = ap;
    }

    // Getters and Setters
    public String getWorkId() { return workId; }
    public String getFullName() { return workFname + " " + workLname; }
    public String getServiceIncharge() { return serviceIncharge; }
    public boolean isAvailable() { return availability; }
}
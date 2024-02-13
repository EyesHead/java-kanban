public class Subtask extends Task {
    private final int relatedEpicID;

    public Subtask(String name, String description, int relatedEpicID, Status status) {
        super(name, description, status);
        this.relatedEpicID = relatedEpicID;
    }
    public Subtask(String name, String description, int relatedEpicID) {
        super(name, description);
        this.relatedEpicID = relatedEpicID;
        setStatus(Status.NEW);
    }

    public int getRelatedEpicID() {
        return relatedEpicID;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "relatedEpicID=" + relatedEpicID +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }
}

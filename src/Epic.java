import java.util.ArrayList;
import java.util.HashMap;

public class Epic extends Task {
    private ArrayList<Integer> relatedSubtaskIDs;

    public Epic(String name, String description) {
        super(name, description);
        setStatus(Status.NEW);
        ArrayList<Integer> relatedSubtaskID = new ArrayList<>();
    }
    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }


    public ArrayList<Integer> getRelatedSubtaskIDs() {
        return relatedSubtaskIDs;
    }
    public void setRelatedSubtaskIDs(ArrayList<Integer> relatedSubtaskIDs) {
        this.relatedSubtaskIDs = relatedSubtaskIDs;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }
}

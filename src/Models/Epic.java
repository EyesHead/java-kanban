package Models;

import Statuses.Status;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Integer> subtaskIds;

    public Epic(String name, String description) {
        super(name, description);
        ArrayList<Integer> relatedSubtaskID = new ArrayList<>();
    }
    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }


    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }
    public void setSubtaskIds(ArrayList<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        Epic epic = (Epic) object;
        return Objects.equals(subtaskIds, epic.subtaskIds) &&
                id == epic.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIds);
    }

    @Override
    public String toString() {
        String result = "Models.Epic{" +
                        "name='" + name + '\'' +
                        ", description='" + description + '\'' +
                        ", id='" + id + '\'' +
                        ", status='" + status + '\'';
        if (subtaskIds == null) {
            result += ", subtaskIds=null" + '}';
        } else {
            result += ", subtaskIds.size()='" + subtaskIds.size()+ '\'' + '}';
        }
        return result;
    }
}

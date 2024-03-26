package taskModels;

import java.util.ArrayList;
import java.util.Objects;

import static taskModels.Status.NEW;

public class Epic extends Task {
    private ArrayList<Integer> subtaskIds;

    public Epic(String name, String description, Status status) {
        super(name, description, status);
        if (status.equals(NEW)) this.subtaskIds = new ArrayList<>();
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
        if (subtaskIds.isEmpty()) {
            result += ", subtaskIds is Empty" + '}';
        } else {
            result += ", subtaskIds.size()='" + subtaskIds.size()+ '\'' + '}';
        }

        return result;
    }
}

import java.util.Objects;

public class Task {
    protected String name;
    protected String description;
    protected int id;
    protected Status status;


    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
    }
    public Task(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }



    public int getID(){
        return id;
    }
    public void setID(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }

}

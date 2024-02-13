import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private static int id = 0;
    HashMap<Integer, Task> tasks; // All tasks IDs
    HashMap<Integer, Epic> epics; // All epic IDs
    HashMap<Integer, Subtask> subtasks;

    public void createTask(Task task) {
        task.setID(generateID());
        tasks.put(task.getID(), task);
    }
    public void createEpic(Epic epic) {
        epic.setID(generateID());
        epics.put(epic.getID(), epic);
    }
    public void createSubtask(Subtask subtask) {
        subtask.setID(generateID());
        int subtaskID = subtask.getID();

        for (Integer epicID : epics.keySet()) {
            if (epicID.equals(subtask.getRelatedEpicID())) {

                Epic currentEpic = epics.get(epicID); // There we add new subtask ID in list of related epic
                ArrayList<Integer> relatedSubtaskIDs = currentEpic.getRelatedSubtaskIDs();
                relatedSubtaskIDs.add(subtaskID);
                currentEpic.setRelatedSubtaskIDs(relatedSubtaskIDs);

                currentEpic.setStatus(Status.IN_PROGRESS);// after creating NEW subtask -> epic status is IN_PROGRESS

                epics.put(currentEpic.getID(), currentEpic); //update epics HashMap;
                return;
            }
        }// Adding subtask id to related epic id with creating
    }

    public void updateTask(Task task){
        tasks.put(task.getID(), task);
    }
    public void updateEpic(Epic epic){
        epics.put(epic.getID(), epic);

        if (epic.getStatus() == Status.DONE) {
            int epicID = epic.getID();
            for (Subtask subtaskNotDONE : subtasks.values()) {
                if(subtaskNotDONE.getRelatedEpicID() == epicID) {
                    Subtask subtaskDone = subtaskNotDONE;
                    int subtaskDoneID = subtaskNotDONE.getID();

                    subtasks.put(subtaskDoneID, subtaskDone);
                }
            }
        }// Epic status changes to DONE => all related subtasks statuses changes to DONE;
    }
    public void updateSubtask(Subtask subtask){
        subtasks.put(subtask.getID(), subtask);

        int subtaskID = subtask.getID();
        int relatedEpicID = subtask.getRelatedEpicID();
        Epic relatedEpic = getEpicByID(relatedEpicID);
        ArrayList<Integer> relatedSubtaskIDs = relatedEpic.getRelatedSubtaskIDs();
        boolean isAllRelatedSubtasksDONE = false;

        if (subtask.status == Status.DONE) {
            for (Integer relatedSubtaskID : relatedSubtaskIDs) {
                for (Integer subtaskIdFromMap : subtasks.keySet()) {
                    Subtask subtask1 = subtasks.get(subtaskIdFromMap);
                    if (relatedSubtaskID.equals(subtaskIdFromMap) && subtask.status == Status.DONE) {
                        isAllRelatedSubtasksDONE = true;
                    }
                }
            }

        } else {
            relatedEpic.setStatus(Status.IN_PROGRESS);
            epics.put(relatedEpicID, relatedEpic);
        }

        if (isAllRelatedSubtasksDONE) {
            relatedEpic.setStatus(Status.DONE);
            epics.put(relatedEpicID, relatedEpic);
        }

    } //status of related epic changes to DONE if all subtasks statuses are DONE


    public void clearAllTasks() {
        tasks.clear();
    }
    public void clearAllEpics() {
        epics.clear();
    }
    public void clearAllSubtasks(int epicID) {

    }

    public Task getTaskByID(int id){
        for (Integer taskID : tasks.keySet()){
            if (taskID == id) {
                return tasks.get(taskID);
            } else return null;
        }
        return null;
    }
    public Epic getEpicByID(int id){
        for (Integer epicID : epics.keySet()){
            if (epicID == id) {
                return epics.get(epicID);
            }
        }
        return null;
    }
    public Subtask getSubtaskByID(int id){
        for (Integer subtaskID : subtasks.keySet()){
            if (subtaskID == id) {
                return subtasks.get(subtaskID);
            }
        }
        return null;
    }

    public ArrayList<Subtask> getSubtaskListByEpicID(int relatedEpicID) {
        ArrayList<Subtask> subtaskListByEpicID = new ArrayList<>();

        Epic relatedEpic = getEpicByID(relatedEpicID);
        for (Integer subtaskIdFromMap : subtasks.keySet()) {
            for (Integer subtaskIdFromRelatedEpic : relatedEpic.getRelatedSubtaskIDs()) {
                if (subtaskIdFromMap.equals(subtaskIdFromRelatedEpic)) {
                    Subtask subtask = subtasks.get(subtaskIdFromMap);
                    subtaskListByEpicID.add(subtask);
                }
            }
        }
        return subtaskListByEpicID;
    }

    private static int generateID() {
        id++;
        return id;
    }
}
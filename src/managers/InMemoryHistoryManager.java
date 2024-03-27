package managers;

import managers.interfaces.HistoryManager;
import models.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> historyOfView = new LinkedList<>();

    @Override
    public void add(Task task) {
        historyOfView.add(task);
        if (historyOfView.size() > 10) {
            historyOfView.removeFirst();
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyOfView;
    }


}

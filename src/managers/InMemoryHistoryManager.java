package managers;

import managerInterfaces.HistoryManager;
import taskModels.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> historyOfView = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (historyOfView.size() == 10) {
            historyOfView.removeFirst();
            historyOfView.add(task);
        }
        historyOfView.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return historyOfView;
    }


}

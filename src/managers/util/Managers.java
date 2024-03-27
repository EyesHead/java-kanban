package managers.util;

import managers.InMemoryHistoryManager;
import managers.InMemoryTaskManager;

public class Managers {
    public InMemoryTaskManager getDefaultTasks() {
        return new InMemoryTaskManager();
    }
    public InMemoryHistoryManager getDefaultHistory(InMemoryTaskManager manager){
        return (InMemoryHistoryManager) manager.getHistoryManager();
    }
}

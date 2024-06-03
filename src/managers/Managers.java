package managers;

import managers.memory_classes.FileBackedTaskManager;
import managers.memory_classes.InMemoryHistoryManager;
import managers.memory_classes.InMemoryTaskManager;

public class Managers {
    public static InMemoryTaskManager getDefault() {
        return new FileBackedTaskManager(getDefaultHistory());
    }

    public static InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}

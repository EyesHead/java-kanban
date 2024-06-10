package managers;

import managers.memory_classes.FileBackedTaskManager;
import managers.memory_classes.InMemoryHistoryManager;

public class Managers {
    public static FileBackedTaskManager getDefault() {
        return new FileBackedTaskManager(getDefaultHistory());
    }

    public static InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}

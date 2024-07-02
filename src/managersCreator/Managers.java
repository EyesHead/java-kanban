package managersCreator;

import taskManager.memory.FileBackedTaskManager;
import taskManager.memory.InMemoryHistoryManager;
import taskManager.memory.InMemoryTaskManager;

public class Managers {
    public static FileBackedTaskManager getDefaultFileManager() {
        return new FileBackedTaskManager(getDefaultHistory());
    }

    public static InMemoryTaskManager getDefaultTaskManager() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

}

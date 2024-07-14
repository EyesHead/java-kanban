package service;

import service.file.FileBackedTaskManager;
import service.history.InMemoryHistoryManager;
import service.inMemory.repository.InMemoryTaskManager;

public class ManagersCreator {
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

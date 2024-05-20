package managers.util;

import managers.InMemoryTaskManager;

public class Managers {
    public InMemoryTaskManager getDefaultTasks() {
        return new InMemoryTaskManager();
    }
}

package managers.interfaces;
import models.*;

import java.util.List;

public interface HistoryManager {
     List<Task> getAll();
     void add(Task task);
     void remove(int id);
}

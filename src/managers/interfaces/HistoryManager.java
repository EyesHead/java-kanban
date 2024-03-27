package managers.interfaces;
import models.*;

import java.util.List;

public interface HistoryManager {
     void add(Task task);
     List<Task> getHistory();

}

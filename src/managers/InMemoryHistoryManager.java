package managers;

import managers.interfaces.HistoryManager;
import models.Task;
import java.util.*;

//InMemoryHistoryManager реализует структуру LinkedHashMap
public class InMemoryHistoryManager implements HistoryManager {

    private static class Node {
        Task item;
        Node next;
        Node prev;
        Node(Node prev, Task item, Node next){
            this.prev = prev;
            this.next = next;
            this.item = item;
        }
    }

    HashMap<Integer, Node> history = new HashMap<>();
    Node head;
    Node tail;


    @Override
    public void add(Task task) {
        Node node = history.get(task.getId());
        removeNode(node);
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        Node node = history.get(id);
        removeNode(node);
    }


    @Override
    public List<Task> getAll() {
        ArrayList<Task> list = new ArrayList<>();
        Node current = head;
        while (current != null) {
            list.add(current.item);
            current = current.next;
        }
        // Обход по связанному списку
        return list;
    }


    void linkLast(Task task) {
        final Node last = tail;
        final Node newNode = new Node(last, task, null);
        tail = newNode;
        if (last == null) {
            head = newNode;
        } else {
            last.next = newNode;
        }

        history.put(task.getId(), newNode);
    }

    //Удаление из связанного списка
    private void removeNode(Node current) {
        if (current == null) {
            return;
        }
        if (current.prev == null) {
            current.next.prev = null;
        }
        if (current.prev != null && current.next != null) {
            current.prev.next = current.next;
            current.next.prev = current.prev;
        }
        if (current.next == null) {
            current.prev.next = null;
        }

        history.remove(current.item.getId());
    }
}

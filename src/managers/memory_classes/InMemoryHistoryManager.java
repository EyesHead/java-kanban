package managers.memory_classes;

import managers.interfaces.HistoryManager;
import models.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    HashMap<Integer, Node> historyMap = new HashMap<>();
    Node head;
    Node tail;


    @Override
    public void add(Task task) {
        if (task == null) return;

        //удаляем задачу, если она есть в map
        if (historyMap.containsKey(task.getId())) {
            Node node = historyMap.get(task.getId());
            removeNode(node);
        }

        linkLast(task);
    }

    @Override
    public void remove(int id) {
        Node node = historyMap.get(id);
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

    //Добавление в связанный список (в конец)
    private void linkLast(Task task) {
        final Node last = tail;
        final Node newNode = new Node(last, task, null);
        tail = newNode;
        if (last == null) {
            head = newNode;
        } else {
            last.next = newNode;
        }

        historyMap.put(task.getId(), newNode);
    }

    //Удаление из связанного списка
    private void removeNode(Node current) {
        if (current == null) return;

        // (head)CURRENT <-> nodeNext <-> ...   |   (head)nodeNext <-> ...
        if (current.prev == null && current.next != null) {
            current.next.prev = null;
            head = current.next;
        }

        // (head)nodePrev <-> CURRENT <-> nodeNext(tail)    |   (head)nodePrev <-> nodeNext(tail)
        if (current.prev != null && current.next != null) {
            current.prev.next = current.next;
            current.next.prev = current.prev;
        }

        // ... <-> nodePrev <-> CURRENT(tail)   |   ... <-> nodePrev(tail)
        if (current.next == null && current.prev != null) {
            current.prev.next = null;
            tail = current.prev;
        }

        // null <-> CURRENT(tail + head) <-> null
        if (current.next == null && current.prev == null) {
            historyMap.remove(current.item.getId());
            tail = null;
            head = null;
            return;
        }

        historyMap.remove(current.item.getId());
    }
}

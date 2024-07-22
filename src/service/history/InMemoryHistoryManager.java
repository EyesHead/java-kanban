package service.history;

import service.HistoryManager;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//InMemoryHistoryManager реализует структуру LinkedHashMap
public class InMemoryHistoryManager implements HistoryManager {

    HashMap<Integer, Node> historyMap = new HashMap<>();
    Node head;
    Node tail;

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

    public int size() {
        return getAll().size();
    }

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
    private void removeNode(Node currentNode) {
        if (currentNode == null) return;

        // (head)CURRENT <-> nodeNext <-> ...   |   (head)nodeNext <-> ...
        if (currentNode.prev == null && currentNode.next != null) {
            currentNode.next.prev = null;
            head = currentNode.next;
        }

        // (head)nodePrev <-> CURRENT <-> nodeNext(tail)    |   (head)nodePrev <-> nodeNext(tail)
        if (currentNode.prev != null && currentNode.next != null) {
            currentNode.prev.next = currentNode.next;
            currentNode.next.prev = currentNode.prev;
        }

        // ... <-> nodePrev <-> CURRENT(tail)   |   ... <-> nodePrev(tail)
        if (currentNode.next == null && currentNode.prev != null) {
            currentNode.prev.next = null;
            tail = currentNode.prev;
        }

        // null <-> CURRENT(tail + head) <-> null
        if (currentNode.next == null && currentNode.prev == null) {
            historyMap.remove(currentNode.item.getId());
            tail = null;
            head = null;
            return;
        }

        historyMap.remove(currentNode.item.getId());
    }
}

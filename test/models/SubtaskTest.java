package models;

import org.junit.jupiter.api.Test;

import static models.Status.NEW;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    public void assertEqualsSubtasks(){
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", epic.getId(), NEW);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", epic.getId(), NEW);
        //id подзадачам присваивается только при добавлении их в коллекцию!
        assertEquals(subtask1, subtask2, "Подзадачи не совпадают");
    }

}
package models;

import org.junit.jupiter.api.Test;

import static models.Status.*;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    public void assertEqualsTasks(){
        Task task1 = new Task("Задача 1", "Описание задачи 1", NEW);
        Task task2 = new Task("Задача 2", "Описание задачи 2", NEW);
        //id задачам присваивается только при добавлении их в коллекцию!
        assertEquals(task1, task2, "Задачи не совпадают");
    }
}
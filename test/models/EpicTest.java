package models;

import org.junit.jupiter.api.Test;

import static models.Status.NEW;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    public void assertEqualsEpics(){
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        //id эпикам присваивается только при добавлении их в коллекцию!
        assertEquals(epic1, epic2, "Эпики не совпадают");
    }
}
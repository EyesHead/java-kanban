package tests.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class LDTRandomizer {
    public static LocalDateTime getRandomLDT() {
        return LocalDateTime.of(getRandomDate(), getRandomTime());
    }

    private static LocalTime getRandomTime() {
        return LocalTime.of((int) (Math.random() * 24), (int) (Math.random() * 60));

    }

    private static LocalDate getRandomDate() {
        return LocalDate.of( (int) (Math.random() * 6) + 2020,
                (int) (Math.random() * 11) + 1,
                (int) (Math.random() * 27) + 1);
    }
}

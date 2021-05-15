package ru.sorin.port.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sorin.port.timetable.TimeTable;

@SpringBootApplication
@RestController
public class ServiceOneController {
    @GetMapping("/timeTable")
    public String getTimeTable() {
        TimeTable table = new TimeTable(400);
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(table);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}

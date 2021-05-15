package ru.sorin.port.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ru.sorin.port.interaction.Interaction;
import ru.sorin.port.timetable.TimeTable;
import ru.sorin.port.unloading.Statistics;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class ServiceTwoController {
    static final String path = "http://localhost:8280/";

    @GetMapping("/jsonTimeTable")
    public String getJsonTimeTable() {
        SpringApplication app = new SpringApplication(ServiceOneController.class);
        Map<String, Object> map = new HashMap<>();
        map.put("server.port", "8280");
        map.put("server.host", "localhost");
        app.setDefaultProperties(map);
        ConfigurableApplicationContext context = app.run();

        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplate restTemplate = builder.build();
        String stringTimeTable = restTemplate.getForObject(path + "timeTable", String.class);
        context.close();

        TimeTable table = new TimeTable();
        Interaction.readString(stringTimeTable, table);
        Interaction.printJson("timetable", table);
        return stringTimeTable;
    }
    @GetMapping("/jsonTimeTableByName")
    public String getJsonTimeTableByName(@RequestParam String filename) {
        TimeTable table = new TimeTable();
        Interaction.readJson(filename, table);
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(table);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
    @PostMapping("/statistics")
    public void sendJsonStatistics(@RequestBody String str) {
        Statistics statistics = new Statistics();
        Interaction.readString(str, statistics);
        Interaction.printJson("stat", statistics);
    }
}
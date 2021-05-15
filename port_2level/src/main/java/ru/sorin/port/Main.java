package ru.sorin.port;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestTemplate;
import ru.sorin.port.controllers.ServiceTwoController;
import ru.sorin.port.interaction.Interaction;
import ru.sorin.port.timetable.TimeTable;
import ru.sorin.port.unloading.Port;
import ru.sorin.port.unloading.Statistics;

import java.util.HashMap;
import java.util.Map;

public class Main {
    static final String path = "http://localhost:8080/";
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ServiceTwoController.class);
        Map<String, Object> map = new HashMap<>();
        map.put("server.port", "8080");
        map.put("server.host", "localhost");
        app.setDefaultProperties(map);
        ConfigurableApplicationContext context = app.run();

        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplate restTemplate = builder.build();
        //String stringTimeTable = restTemplate.getForObject(path + "jsonTimeTable", String.class);
        String stringTimeTable = restTemplate.getForObject(path + "jsonTimeTableByName?filename=timetable", String.class);
        System.out.println(stringTimeTable);
        TimeTable table = new TimeTable();
        Interaction.readString(stringTimeTable, table);

        Port port = new Port(table);
        port.run(6, 6, 6);
        Statistics statistics = port.getBestStatistics();
        statistics.printInformation();
        System.out.println("Best cranes: Liquid " + statistics.countOfLiquidCrane +
                ", Loose " + statistics.countOfLooseCrane +
                ", Container " + statistics.countOfContainerCrane);

        try {
            restTemplate.postForObject(path + "statistics",
                    new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(statistics), String.class );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        context.close();
    }
}

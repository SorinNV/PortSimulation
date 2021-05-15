package ru.sorin.port.interaction;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.sorin.port.timetable.Ship;
import ru.sorin.port.timetable.TimeTable;
import ru.sorin.port.unloading.Statistics;


import java.io.*;
import java.util.LinkedList;

public class Interaction {
    public static void printJson(String fileName, TimeTable timeTable) {
        String filepath = System.getProperty("user.dir") + File.separator + fileName + ".json";
        try {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(filepath), timeTable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void printJson(String fileName, Statistics statistics) {
        String filepath = System.getProperty("user.dir") + File.separator + fileName + ".json";
        try {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(filepath), statistics);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readJson(String fileName, Statistics statistics) {
        String filepath = System.getProperty("user.dir") + File.separator + fileName + ".json";
        try {
            Statistics stat = new ObjectMapper().readValue(new FileInputStream(filepath), Statistics.class);
            statistics.statLiquidList.addAll(stat.statLiquidList);
            statistics.statLooseList.addAll(stat.statLooseList);
            statistics.statContainerList.addAll(stat.statContainerList);
            statistics.unloadCount.set(stat.unloadCount.get());
            statistics.averageQueueSize = stat.averageQueueSize;
            statistics.averageWaitingTime = stat.averageWaitingTime;
            statistics.averageDelayTime = stat.averageDelayTime;
            statistics.maxDelayTime = stat.maxDelayTime;
            statistics.sumCost = stat.sumCost;
            statistics.countOfLiquidCrane = stat.countOfLiquidCrane;
            statistics.countOfLooseCrane = stat.countOfLooseCrane;
            statistics.countOfContainerCrane = stat.countOfContainerCrane;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readJson(String fileName, TimeTable timeTable) {
        String filepath = System.getProperty("user.dir") + File.separator + fileName + ".json";
        try {
            read(new FileInputStream(filepath), timeTable);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void readString(String string, TimeTable timeTable) {
        if (timeTable.getShipList() == null) {
            timeTable.setShipList(new LinkedList<Ship>());
        }
        try {
            timeTable.getShipList().addAll(new ObjectMapper().readValue(string, TimeTable.class).getShipList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readString(String string, Statistics statistics) {
        Statistics stat = null;
        try {
            stat = new ObjectMapper().readValue(string, Statistics.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        statistics.statLiquidList.addAll(stat.statLiquidList);
        statistics.statLooseList.addAll(stat.statLooseList);
        statistics.statContainerList.addAll(stat.statContainerList);
        statistics.unloadCount.set(stat.unloadCount.get());
        statistics.averageQueueSize = stat.averageQueueSize;
        statistics.averageWaitingTime = stat.averageWaitingTime;
        statistics.averageDelayTime = stat.averageDelayTime;
        statistics.maxDelayTime = stat.maxDelayTime;
        statistics.sumCost = stat.sumCost;
        statistics.countOfLiquidCrane = stat.countOfLiquidCrane;
        statistics.countOfLooseCrane = stat.countOfLooseCrane;
        statistics.countOfContainerCrane = stat.countOfContainerCrane;
    }

    public static void readConsole(TimeTable timeTable) {
        read(System.in, timeTable);
    }

    private static void read(InputStream src, TimeTable timeTable) {
        if (timeTable.getShipList() == null) {
            timeTable.setShipList(new LinkedList<Ship>());
        }
        try {
            timeTable.getShipList().addAll(new ObjectMapper().readValue(src, TimeTable.class).getShipList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

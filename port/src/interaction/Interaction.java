package interaction;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import timetable.TimeTable;
import unloading.Statistics;

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
            statistics.UnloadCount = stat.UnloadCount;
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

    public static void readConsole(TimeTable timeTable) {
        read(System.in, timeTable);
    }

    private static void read(InputStream src, TimeTable timeTable) {
        if (timeTable.getShipList() == null) {
            timeTable.setShipList(new LinkedList<>());
        }
        try {
            timeTable.getShipList().addAll(new ObjectMapper().readValue(src, TimeTable.class).getShipList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

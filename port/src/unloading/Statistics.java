package unloading;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import timetable.TimeTable;
import java.util.LinkedList;

public class Statistics {
    public static class ShipStat {
        public String name;
        public int arrivalTime = 0;
        public int waitingTime = 0;
        public int startUnloadTime = 0;
        public int unloadTime = 0;

        public ShipStat() {}
        public ShipStat(ShipStat ss) {
            name = ss.name;
            arrivalTime = ss.arrivalTime;
            waitingTime = ss.waitingTime;
            startUnloadTime = ss.startUnloadTime;
            unloadTime = ss.unloadTime;
        }
    }
    @JsonDeserialize(as = LinkedList.class, contentAs = ShipStat.class)
    public LinkedList<ShipStat> statLiquidList = new LinkedList<>();
    @JsonDeserialize(as = LinkedList.class, contentAs = ShipStat.class)
    public LinkedList<ShipStat> statLooseList = new LinkedList<>();
    @JsonDeserialize(as = LinkedList.class, contentAs = ShipStat.class)
    public LinkedList<ShipStat> statContainerList = new LinkedList<>();
    public int UnloadCount = 0;
    public int averageQueueSize = 0;
    public int averageWaitingTime = 0;
    public int averageDelayTime = 0;
    public int maxDelayTime;
    public int sumCost = 0;
    public int countOfLiquidCrane = 0;
    public int countOfLooseCrane = 0;
    public int countOfContainerCrane = 0;

    public Statistics() {}
    public Statistics(Statistics statistics) {
        statLiquidList = new LinkedList<>();
        statLooseList = new LinkedList<>();
        statContainerList = new LinkedList<>();

        for (ShipStat ss: statistics.statLiquidList) {
            statLiquidList.addLast(new ShipStat(ss));
        }
        for (ShipStat ss: statistics.statLooseList) {
            statLooseList.addLast(new ShipStat(ss));
        }
        for (ShipStat ss: statistics.statContainerList) {
            statContainerList.addLast(new ShipStat(ss));
        }
        UnloadCount = statistics.UnloadCount;
        averageQueueSize = statistics.averageQueueSize;
        averageWaitingTime = statistics.averageWaitingTime;
        averageDelayTime = statistics.averageDelayTime;
        maxDelayTime = statistics.maxDelayTime;
        sumCost = statistics.sumCost;
        countOfLiquidCrane = statistics.countOfLiquidCrane;
        countOfLooseCrane = statistics.countOfLooseCrane;
        countOfContainerCrane = statistics.countOfContainerCrane;
    }
    public void printInformation() {
        int separatorsCount = 109;
        for (ShipStat ss: statLiquidList) {
            printStat(ss);
            averageWaitingTime += ss.waitingTime;
        }
        for (int i = 0; i < separatorsCount; i++) {
            System.out.print("-");
        }
        System.out.println();
        for (ShipStat ss: statLooseList) {
            printStat(ss);
            averageWaitingTime += ss.waitingTime;
        }
        for (int i = 0; i < separatorsCount; i++) {
            System.out.print("-");
        }
        System.out.println();
        for (ShipStat ss: statContainerList) {
            printStat(ss);
            averageWaitingTime += ss.waitingTime;
        }
        averageWaitingTime /= (statLiquidList.size() + statLooseList.size() + statContainerList.size());
        for (int i = 0; i < separatorsCount; i++) {
            System.out.print("-");
        }
        System.out.println();
        System.out.println("Unload count: " + (UnloadCount));
        System.out.println("Sum cost: " + sumCost);
        System.out.println("Count of liquid crane: " + countOfLiquidCrane);
        System.out.println("Count of loose crane: " + countOfLooseCrane);
        System.out.println("Count of container crane: " + countOfContainerCrane);
        System.out.println("Average waiting time: " + timeToString(averageWaitingTime));
        System.out.println("Average delay time: " + timeToString(averageDelayTime));
        System.out.println("Max delay time: " + timeToString(maxDelayTime));
        System.out.println("Average queue size: " + averageQueueSize);
    }
    public ShipStat get(String shipName) {
        for (ShipStat stat: statLiquidList) {
            if (stat.name == shipName) {
                return stat;
            }
        }
        for (ShipStat stat: statLooseList) {
            if (stat.name == shipName) {
                return stat;
            }
        }
        for (ShipStat stat: statContainerList) {
            if (stat.name == shipName) {
                return stat;
            }
        }
        return null;
    }
    private void printStat(ShipStat ss) {
        System.out.println("Name: " + String.format("%7s", ss.name) +
                " Arrival time: " + timeToString(ss.arrivalTime) +
                " Waiting time: " + timeToString(ss.waitingTime) +
                " Start unload time: " + timeToString(ss.startUnloadTime) +
                " Unload time: " + timeToString(ss.unloadTime));
    }
    private String timeToString(int time) {
        return String.format("%02d", time / TimeTable.minutesPerDay) +
                "." + String.format("%02d", (time / TimeTable.minutesPerHour) % TimeTable.hoursPerDay) +
                "." + String.format("%02d", time % TimeTable.minutesPerHour);
    }
}

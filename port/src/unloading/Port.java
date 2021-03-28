package unloading;

import timetable.Ship;
import timetable.TimeTable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Port {
    private final ArrayList<Ship> liquidShipListToSave;
    private final ArrayList<Ship> looseShipListToSave;
    private final ArrayList<Ship> containerShipListToSave;
    private ArrayList<Ship> liquidShipList;
    private ArrayList<Ship> looseShipList;
    private ArrayList<Ship> containerShipList;

    private final ArrayList<Statistics> statisticsList;

    private final static double craneLooseSpeed = 75.0;
    private final static double craneLiquidSpeed = 55.0;
    private final static double craneContainerSpeed = 25.0;
    private final static int unloadDelayTime = TimeTable.minutesPerDay;
    private final static int arrivalDelayTime = 7 * TimeTable.minutesPerDay;
    private final static int craneCost = 30000;
    private final static int waitingCostPerHour = 100;

    public Port(TimeTable table) {
        setLiquidShipList(new ArrayList<>());
        setLooseShipList(new ArrayList<>());
        setContainerShipList(new ArrayList<>());
        liquidShipListToSave = new ArrayList<>();
        looseShipListToSave = new ArrayList<>();
        containerShipListToSave = new ArrayList<>();
        statisticsList = new ArrayList<>();

        for (Ship ship: table.getShipList()) {
            Random rand = new Random();
            switch (ship.getCargoType()) {
                case LOOSE -> {
                    int t = (int) (ship.getCargoWeight() / craneLooseSpeed) + rand.nextInt(unloadDelayTime);
                    ship.setUnloadTime(t);
                }
                case LIQUID -> {
                    int t = (int) (ship.getCargoWeight() / craneLiquidSpeed) + rand.nextInt(unloadDelayTime);
                    ship.setUnloadTime(t);
                }
                case CONTAINER -> {
                    int t = (int) (ship.getCargoWeight() / craneContainerSpeed) + rand.nextInt(unloadDelayTime);
                    ship.setUnloadTime(t);
                }
            }
            int arrivalDelay = ship.getDay() * TimeTable.minutesPerDay +
                    ship.getTime() +
                    rand.nextInt(arrivalDelayTime * 2) - arrivalDelayTime;
            ship.setDay(arrivalDelay / TimeTable.minutesPerDay);
            if (ship.getDay() < 0) {
                ship.setDay(0);
            } else if (ship.getDay() >= TimeTable.daysPerMonth) {
                ship.setDay(TimeTable.daysPerMonth - 1);
            }
            ship.setTime(arrivalDelay % TimeTable.minutesPerDay);
            if (ship.getTime() < 0) {
                ship.setTime(0);
            }
        }
        table.getShipList().sort(new Ship.ShipComparator());
        System.out.println("-------------------------------------");
        System.out.println("After change timetable:");
        for (Ship ship: table.getShipList()) {
            ship.printInformation();
            switch (ship.getCargoType()) {
                case LOOSE -> looseShipList.add(looseShipList.size(), ship);
                case LIQUID -> liquidShipList.add(liquidShipList.size(), ship);
                case CONTAINER -> containerShipList.add(containerShipList.size(), ship);
            }
        }

        for (int i = 0; i < liquidShipList.size(); i++) {
            liquidShipListToSave.add(i, new Ship(liquidShipList.get(i)));
        }
        for (int i = 0; i < looseShipList.size(); i++) {
            looseShipListToSave.add(i, new Ship(looseShipList.get(i)));
        }
        for (int i = 0; i < containerShipList.size(); i++) {
            containerShipListToSave.add(i, new Ship(containerShipList.get(i)));
        }
    }
    static class UnloadTask implements Runnable {
        private final ArrayList<Ship> shipList;
        private final int countCranes;
        private final Statistics statistics;
        private final LinkedList<Statistics.ShipStat> statShipList;

        public UnloadTask(ArrayList<Ship> shipList, int countCranes, Statistics statistics,
                          LinkedList<Statistics.ShipStat> statShipList) {
            this.shipList = shipList;
            this.countCranes = countCranes;
            this.statistics = statistics;
            this.statShipList = statShipList;
        }

        @Override
        public void run() {
            for (Ship ship : shipList) {
                statShipList.addLast(new Statistics.ShipStat());
                statShipList.getLast().name = ship.getName();
                statShipList.getLast().arrivalTime = ship.getDay() * TimeTable.minutesPerDay + ship.getTime();
            } //statistic
            for (int time = 0; time < TimeTable.daysPerMonth * TimeTable.minutesPerDay; time++) {
                int countFreeCranes = countCranes;
                for (int shipNumber = 0; shipNumber < shipList.size(); shipNumber++) {
                    int currentShipTime = shipList.get(shipNumber).getDay() * TimeTable.minutesPerDay +
                            shipList.get(shipNumber).getTime();
                    if (currentShipTime < time) {
                        if (shipList.get(shipNumber).getUnloadTime() == 0) {
                            int delayTime = (time - currentShipTime) -
                                    (int) (shipList.get(shipNumber).getCargoWeight() / craneLiquidSpeed);
                            statistics.averageDelayTime += delayTime; // statistics
                            statistics.maxDelayTime = Math.max(statistics.maxDelayTime, delayTime); // statistics
                            if (delayTime > 0) {
                                statistics.sumCost += delayTime / TimeTable.minutesPerHour * waitingCostPerHour;
                            }
                            statistics.get(shipList.get(shipNumber).getName()).unloadTime =
                                    (time - statistics.get(
                                            shipList.get(shipNumber).getName()).startUnloadTime); //statistic
                            int countInQueue = 0;
                            for (Ship ship : shipList) {
                                if (statistics.get(ship.getName()).arrivalTime < time &&
                                        statistics.get(ship.getName()).startUnloadTime == 0) {
                                    countInQueue++;
                                }
                            } //statistics
                            statistics.averageQueueSize += countInQueue; //statistics
                            shipList.remove(shipNumber);
                            statistics.UnloadCount++;
                            break;
                        }
                        if (countFreeCranes == 0) {
                            break;
                        } else if (countFreeCranes == 1) {
                            if (statistics.get(shipList.get(shipNumber).getName()).startUnloadTime == 0) {
                                statistics.get(shipList.get(shipNumber).getName()).startUnloadTime = time;
                                statistics.get(shipList.get(shipNumber).getName()).waitingTime =
                                        time - statistics.get(shipList.get(shipNumber).getName()).arrivalTime;
                            } //statistic
                            if (shipList.get(shipNumber).getUnloadTime() > 0) {
                                countFreeCranes--;
                                shipList.get(shipNumber).setUnloadTime(
                                        shipList.get(shipNumber).getUnloadTime() - 1);
                            }
                        } else {
                            if (statistics.get(shipList.get(shipNumber).getName()).startUnloadTime == 0) {
                                statistics.get(shipList.get(shipNumber).getName()).startUnloadTime = time;
                                statistics.get(shipList.get(shipNumber).getName()).waitingTime =
                                        time - statistics.get(shipList.get(shipNumber).getName()).arrivalTime;
                            } //statistic
                            if (shipList.get(shipNumber).getUnloadTime() > 1) {
                                countFreeCranes -= 2;
                                shipList.get(shipNumber).setUnloadTime(
                                        shipList.get(shipNumber).getUnloadTime() - 2);
                            } else if (shipList.get(shipNumber).getUnloadTime() == 1) {
                                countFreeCranes--;
                                shipList.get(shipNumber).setUnloadTime(
                                        shipList.get(shipNumber).getUnloadTime() - 1);
                            }
                        }
                    }
                }
            }
        }
    }
    public void run(int maxCountOfLiquidCrane, int maxCountOfLooseCrane, int maxCountOfContainerCrane) {
        for (int a = 1; a < maxCountOfLiquidCrane + 1; a++) {
            for (int b = 1; b < maxCountOfLooseCrane + 1; b++) {
                for (int c = 1; c < maxCountOfContainerCrane + 1; c++) {
                    Statistics statistics = new Statistics();
                    statistics.sumCost = craneCost * (a + b + c);
                    statistics.countOfLiquidCrane = a;
                    statistics.countOfLooseCrane = b;
                    statistics.countOfContainerCrane = c;
                    Thread thread1 = new Thread(new UnloadTask(liquidShipList, a, statistics, statistics.statLiquidList));
                    Thread thread2 = new Thread(new UnloadTask(looseShipList, b, statistics, statistics.statLooseList));
                    Thread thread3 = new Thread(new UnloadTask(containerShipList, c, statistics, statistics.statContainerList));
                    thread1.start();
                    thread2.start();
                    thread3.start();
                    try {
                        thread1.join();
                        thread2.join();
                        thread3.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    statistics.averageDelayTime /= (statistics.UnloadCount);
                    statistics.averageQueueSize /= (statistics.UnloadCount);
                    statistics.printInformation();
                    statisticsList.add(statisticsList.size(), statistics);

                    liquidShipList.clear();
                    looseShipList.clear();
                    containerShipList.clear();
                    for (int i = 0; i < liquidShipListToSave.size(); i++) {
                        liquidShipList.add(i, new Ship(liquidShipListToSave.get(i)));
                    }
                    for (int i = 0; i < looseShipListToSave.size(); i++) {
                        looseShipList.add(i, new Ship(looseShipListToSave.get(i)));
                    }
                    for (int i = 0; i < containerShipListToSave.size(); i++) {
                        containerShipList.add(i, new Ship(containerShipListToSave.get(i)));
                    }
                }
            }
        }
    }
    public Statistics getBestStatistics() {
        int min = statisticsList.get(0).sumCost;
        int minIndex = 0;
        for (int i = 0; i < statisticsList.size(); i++) {
            if (statisticsList.get(i).sumCost < min) {
                min = statisticsList.get(i).sumCost;
                minIndex = i;
            }
        }
        return statisticsList.get(minIndex);
    }

    public ArrayList<Ship> getLiquidShipList() {
        return liquidShipList;
    }
    public void setLiquidShipList(ArrayList<Ship> liquidShipList) {
        this.liquidShipList = liquidShipList;
    }
    public ArrayList<Ship> getLooseShipList() {
        return looseShipList;
    }
    public void setLooseShipList(ArrayList<Ship> looseShipList) {
        this.looseShipList = looseShipList;
    }
    public ArrayList<Ship> getContainerShipList() {
        return containerShipList;
    }
    public void setContainerShipList(ArrayList<Ship> containerShipList) {
        this.containerShipList = containerShipList;
    }
}

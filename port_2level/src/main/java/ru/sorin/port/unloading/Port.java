package ru.sorin.port.unloading;



import ru.sorin.port.timetable.Ship;
import ru.sorin.port.timetable.TimeTable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Port {
    private final CopyOnWriteArrayList<Ship> liquidShipListToSave;
    private final CopyOnWriteArrayList<Ship> looseShipListToSave;
    private final CopyOnWriteArrayList<Ship> containerShipListToSave;
    private CopyOnWriteArrayList<Ship> liquidShipList;
    private CopyOnWriteArrayList<Ship> looseShipList;
    private CopyOnWriteArrayList<Ship> containerShipList;

    private final ArrayList<Statistics> statisticsList;

    private final static double craneLooseSpeed = 75.0;
    private final static double craneLiquidSpeed = 55.0;
    private final static double craneContainerSpeed = 25.0;
    private final static int unloadDelayTime = TimeTable.minutesPerDay;
    private final static int arrivalDelayTime = 7 * TimeTable.minutesPerDay;
    private final static int craneCost = 30000;
    private final static int waitingCostPerHour = 100;

    public Port(TimeTable table) {
        setLiquidShipList(new CopyOnWriteArrayList<Ship>());
        setLooseShipList(new CopyOnWriteArrayList<Ship>());
        setContainerShipList(new CopyOnWriteArrayList<Ship>());
        liquidShipListToSave = new CopyOnWriteArrayList<>();
        looseShipListToSave = new CopyOnWriteArrayList<>();
        containerShipListToSave = new CopyOnWriteArrayList<>();
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
    static class UnloadTask {
        private final CopyOnWriteArrayList<Ship> shipList;
        private final int countCranes;
        private final Statistics statistics;
        private final LinkedList<Statistics.ShipStat> statShipList;

        public UnloadTask(CopyOnWriteArrayList<Ship> shipList, int countCranes, Statistics statistics,
                          LinkedList<Statistics.ShipStat> statShipList) {
            this.shipList = shipList;
            this.countCranes = countCranes;
            this.statistics = statistics;
            this.statShipList = statShipList;
        }
        static class TimeSynchronizer {
            public TimeSynchronizer(int numberOfCranes, int numberOfShips) {
                this.numberOfCranes = numberOfCranes;
                time = 0;
                endTime = TimeTable.daysPerMonth * TimeTable.minutesPerDay;
                freeWorkspace = new CopyOnWriteArrayList<>();
                for (int i = 0; i < numberOfShips; i++) {
                    freeWorkspace.add(i, 2);
                }
            }
            public volatile int time;
            private volatile int numberOfCompletedCranes;
            private volatile int numberOfCranes;
            private volatile int endTime;
            private volatile CopyOnWriteArrayList<Integer> freeWorkspace;
            public synchronized void complete() {
                numberOfCompletedCranes += 1;
            }
            public void update() {
                while (numberOfCompletedCranes != numberOfCranes) {
                }
                numberOfCompletedCranes = 0;
                for (int i = 0; i < freeWorkspace.size(); i++) {
                    if (freeWorkspace.get(i) >= 0) {
                        freeWorkspace.set(i, 2);
                    }
                }
                time++;
            }
            public boolean endWork() {
                return time >= endTime;
            }
        }
        static class Crane implements Runnable {
            private final CopyOnWriteArrayList<Ship> shipList;
            private final Statistics statistics;
            private final TimeSynchronizer timer;
            Crane(CopyOnWriteArrayList<Ship> shipList, Statistics statistics, TimeSynchronizer timer) {
                this.shipList = shipList;
                this.statistics = statistics;
                this.timer = timer;
            }

            @Override
            public void run() {
                int time = -1;
                while (!timer.endWork()) {
                    while (time == timer.time) {
                    }
                    time = timer.time;
                    int currentNumber = -1;
                    for (int shipNumber = 0; shipNumber < shipList.size(); shipNumber++) {
                        int currentShipTime = shipList.get(shipNumber).getDay() * TimeTable.minutesPerDay +
                                shipList.get(shipNumber).getTime();
                        if (currentShipTime < time && timer.freeWorkspace.get(shipNumber) > 0) {
                            if (shipList.get(shipNumber).getUnloadTime() <= 1) {
                                timer.freeWorkspace.set(shipNumber, 0);
                            } else {
                                timer.freeWorkspace.set(shipNumber, timer.freeWorkspace.get(shipNumber) - 1);
                            }
                            currentNumber = shipNumber;
                            break;
                        }
                    }
                    if (currentNumber == -1) {
                        timer.complete();
                        continue;
                    }
                    synchronized (timer.freeWorkspace) {
                        if (shipList.get(currentNumber).getUnloadTime() <= 0) {
                            if (timer.freeWorkspace.get(currentNumber) == -1) {
                                timer.complete();
                                continue;
                            } else {
                                timer.freeWorkspace.set(currentNumber, -1);
                            }
                            int delayTime = (time - shipList.get(currentNumber).getDay() * TimeTable.minutesPerDay +
                                    shipList.get(currentNumber).getTime()) -
                                    (int) (shipList.get(currentNumber).getCargoWeight() / craneLiquidSpeed);
                            statistics.averageDelayTime += delayTime; // statistics
                            statistics.maxDelayTime = Math.max(statistics.maxDelayTime, delayTime); // statistics
                            if (delayTime > 0) {
                                statistics.sumCost += delayTime / TimeTable.minutesPerHour * waitingCostPerHour;
                            }
                            statistics.get(shipList.get(currentNumber).getName()).unloadTime =
                                    (time - statistics.get(
                                            shipList.get(currentNumber).getName()).startUnloadTime); //statistic
                            int countInQueue = 0;
                            for (Ship ship : shipList) {
                                if (statistics.get(ship.getName()).arrivalTime < time &&
                                        statistics.get(ship.getName()).startUnloadTime == 0) {
                                    countInQueue++;
                                }
                            } //statistics
                            statistics.averageQueueSize += countInQueue; //statistics
                            //shipList.remove(currentNumber);
                            //freeWorkspace.remove(currentNumber);
                            statistics.unloadCount.set(statistics.unloadCount.get() + 1);
                            timer.complete();
                            continue;
                        }
                    }
                    if (statistics.get(shipList.get(currentNumber).getName()).startUnloadTime == 0) {
                        statistics.get(shipList.get(currentNumber).getName()).startUnloadTime = time;
                        statistics.get(shipList.get(currentNumber).getName()).waitingTime =
                                time - statistics.get(shipList.get(currentNumber).getName()).arrivalTime;
                    } //statistic
                    if (shipList.get(currentNumber).getUnloadTime() > 1) {
                        shipList.get(currentNumber).setUnloadTime(
                                shipList.get(currentNumber).getUnloadTime() - 1);
                    } else if (shipList.get(currentNumber).getUnloadTime() == 1) {
                        shipList.get(currentNumber).setUnloadTime(0);
                    }
                    //freeWorkspace.set(currentNumber, freeWorkspace.get(currentNumber) + 1);
                    timer.complete();
                }
                /*for (int time = 0; time < TimeTable.daysPerMonth * TimeTable.minutesPerDay; time++) {
                    int currentNumber = -1;
                    for (int shipNumber = 0; shipNumber < shipList.size(); shipNumber++) {
                        int currentShipTime = shipList.get(shipNumber).getDay() * TimeTable.minutesPerDay +
                                shipList.get(shipNumber).getTime();
                        if (currentShipTime < time && freeWorkspace.get(shipNumber) > 0) {
                            currentNumber = shipNumber;
                            freeWorkspace.set(currentNumber, freeWorkspace.get(currentNumber) - 1);
                            break;
                        }
                    }
                    if (currentNumber == -1) {
                        continue;
                    }
                    if (shipList.get(currentNumber).getUnloadTime() == 0) {
                        int delayTime = (time - shipList.get(currentNumber).getDay() * TimeTable.minutesPerDay +
                                shipList.get(currentNumber).getTime()) -
                                (int) (shipList.get(currentNumber).getCargoWeight() / craneLiquidSpeed);
                        statistics.averageDelayTime += delayTime; // statistics
                        statistics.maxDelayTime = Math.max(statistics.maxDelayTime, delayTime); // statistics
                        if (delayTime > 0) {
                            statistics.sumCost += delayTime / TimeTable.minutesPerHour * waitingCostPerHour;
                        }
                        statistics.get(shipList.get(currentNumber).getName()).unloadTime =
                                (time - statistics.get(
                                        shipList.get(currentNumber).getName()).startUnloadTime); //statistic
                        int countInQueue = 0;
                        for (Ship ship : shipList) {
                            if (statistics.get(ship.getName()).arrivalTime < time &&
                                    statistics.get(ship.getName()).startUnloadTime == 0) {
                                countInQueue++;
                            }
                        } //statistics
                        statistics.averageQueueSize += countInQueue; //statistics
                        shipList.remove(currentNumber);
                        freeWorkspace.remove(currentNumber);
                        statistics.UnloadCount++;
                        continue;
                    }
                    if (statistics.get(shipList.get(currentNumber).getName()).startUnloadTime == 0) {
                        statistics.get(shipList.get(currentNumber).getName()).startUnloadTime = time;
                        statistics.get(shipList.get(currentNumber).getName()).waitingTime =
                                time - statistics.get(shipList.get(currentNumber).getName()).arrivalTime;
                    } //statistic
                    if (shipList.get(currentNumber).getUnloadTime() > 0) {
                        shipList.get(currentNumber).setUnloadTime(
                                shipList.get(currentNumber).getUnloadTime() - 1);
                    }
                    freeWorkspace.set(currentNumber, freeWorkspace.get(currentNumber) + 1);
                }*/
            }
        }

        public void run() {
            for (Ship ship : shipList) {
                statShipList.addLast(new Statistics.ShipStat());
                statShipList.getLast().name = ship.getName();
                statShipList.getLast().arrivalTime = ship.getDay() * TimeTable.minutesPerDay + ship.getTime();
            } //statistic
            CopyOnWriteArrayList<Thread> threads = new CopyOnWriteArrayList<>();
            TimeSynchronizer timer = new TimeSynchronizer(countCranes, shipList.size());
            for (int i = 0; i < countCranes; i++) {
                threads.add(new Thread(new Crane(shipList, statistics, timer)));
            }
            for (int i = 0; i < countCranes; i++) {
                threads.get(i).start();
            }
            while (!timer.endWork()) {
                timer.update();
                //System.out.println(timer.time);
            }
            for (int i = 0; i < countCranes; i++) {
                try {
                    threads.get(i).join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
                    /////поточность по типам убрать, вижул вм вроде для просмотра многопоточности, типы очереди нормальные для потоков
                    UnloadTask unloadTask1 = new UnloadTask(liquidShipList, a, statistics, statistics.statLiquidList);
                    UnloadTask unloadTask2 = new UnloadTask(looseShipList, b, statistics, statistics.statLooseList);
                    UnloadTask unloadTask3 = new UnloadTask(containerShipList, c, statistics, statistics.statContainerList);

                    unloadTask1.run();
                    unloadTask2.run();
                    unloadTask3.run();

                    statistics.averageDelayTime /= (statistics.unloadCount.get());
                    statistics.averageQueueSize /= (statistics.unloadCount.get());
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

    public CopyOnWriteArrayList<Ship> getLiquidShipList() {
        return liquidShipList;
    }
    public void setLiquidShipList(CopyOnWriteArrayList<Ship> liquidShipList) {
        this.liquidShipList = liquidShipList;
    }
    public CopyOnWriteArrayList<Ship> getLooseShipList() {
        return looseShipList;
    }
    public void setLooseShipList(CopyOnWriteArrayList<Ship> looseShipList) {
        this.looseShipList = looseShipList;
    }
    public CopyOnWriteArrayList<Ship> getContainerShipList() {
        return containerShipList;
    }
    public void setContainerShipList(CopyOnWriteArrayList<Ship> containerShipList) {
        this.containerShipList = containerShipList;
    }
}

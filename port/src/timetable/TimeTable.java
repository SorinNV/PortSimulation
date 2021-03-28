package timetable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.LinkedList;
import java.util.Random;

public class TimeTable {
    @JsonDeserialize(as = LinkedList.class, contentAs = Ship.class)
    private LinkedList<Ship> shipList;
    public static int maxCargoWeight = 100;
    public static int daysPerMonth = 30;
    public static int hoursPerDay = 24;
    public static int minutesPerHour = 60;
    public static int minutesPerDay = hoursPerDay * minutesPerHour;

    public TimeTable() {}
    public TimeTable(int countOfShip) {
        double dTime = daysPerMonth * hoursPerDay * minutesPerHour / (countOfShip - 1.0);
        double timeCounter = 0;
        setShipList(new LinkedList<>());
        for (int i = 0; i < countOfShip; i++) {
            Ship newShip = new Ship("ship" + i,
                    (int) (timeCounter / (minutesPerDay)),
                    (int) (timeCounter % (minutesPerDay)),
                    Ship.CargoType.valueOf(new Random().nextInt(Ship.CargoType.values().length)),
                    new Random().nextInt(maxCargoWeight));
            getShipList().addLast(newShip);
            timeCounter += dTime;
        }
    }
    public TimeTable(TimeTable timeTable) {
        shipList = new LinkedList<>();

        for (Ship ship: timeTable.getShipList()) {
            shipList.addLast(new Ship(ship));
        }
    }

    public LinkedList<Ship> getShipList() {
        return shipList;
    }

    public void setShipList(LinkedList<Ship> shipList) {
        this.shipList = shipList;
    }
}

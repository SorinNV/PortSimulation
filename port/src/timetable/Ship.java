package timetable;

import java.util.Comparator;

public class Ship {
    private String name;
    private CargoType type;
    private int cargoWeight;
    private int day;
    private int time;
    private int unloadTime;
    public enum CargoType{
        LOOSE,
        LIQUID,
        CONTAINER;
        public static CargoType valueOf(int index) {
            return CargoType.values()[index];
        }
    }
    public Ship() {}
    public Ship(String name, int day, int time, CargoType cargoType, int cargoWeight) {
        setName(name);
        setDay(day);
        setTime(time);
        setCargoType(cargoType);
        setCargoWeight(cargoWeight);
    }
    public Ship(Ship copyShip) {
        setName(copyShip.getName());
        setDay(copyShip.getDay());
        setTime(copyShip.getTime());
        setCargoType(copyShip.getCargoType());
        setCargoWeight(copyShip.getCargoWeight());
        setUnloadTime(copyShip.getUnloadTime());
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getTime() {
        return time;
    }
    public void setTime(int time) {
        this.time = time;
    }

    public int getDay() {
        return day;
    }
    public void setDay(int day) {
        this.day = day;
    }
    public int getCargoWeight() {
        return cargoWeight;
    }
    public void setCargoWeight(int cargoWeight) {
        this.cargoWeight = cargoWeight;
    }
    public CargoType getCargoType() {
        return type;
    }
    public void setCargoType(CargoType type) {
        this.type = type;
    }
    public int getUnloadTime() {
        return unloadTime;
    }
    public void setUnloadTime(int unloadTime) {
        this.unloadTime = unloadTime;
    }
    public void printInformation() {
        System.out.println("Name: " + name);
        System.out.println("Arrival day: " + day);
        System.out.println("Arrival time: {" + time / 60 + ":" + time % 60 + "}");
        System.out.println("Cargo type: " + type.name());
        System.out.println("Cargo weight: " + cargoWeight);
    }
    public static class ShipComparator implements Comparator<Ship> {
        public int compare(Ship a, Ship b) {
            return (a.getDay() * TimeTable.minutesPerDay + a.getTime()) -
                    (b.getDay() * TimeTable.minutesPerDay + b.getTime());
        }
    }
}

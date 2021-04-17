import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import interaction.Interaction;
import timetable.TimeTable;
import unloading.Port;
import unloading.Statistics;

public class Main {
    public static void main(String[] args) {
        TimeTable table = new TimeTable(400);
        try {
            System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(table));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Interaction.printJson("timetable", table);
        TimeTable rTimeTable = new TimeTable();
        Interaction.readJson("timetable", rTimeTable);
        //Interaction.readConsole(rTimeTable);
        Port port = new Port(rTimeTable);
        port.run(6, 6, 6);
        Statistics statistics = port.getBestStatistics();

        Interaction.printJson("stat", statistics);
        Statistics statistics2 = new Statistics();
        Interaction.readJson("stat", statistics2);
        statistics2.printInformation();
        System.out.println("Best cranes: Liquid " + statistics2.countOfLiquidCrane +
                ", Loose " + statistics2.countOfLooseCrane +
                ", Container " + statistics2.countOfContainerCrane);
    }
}

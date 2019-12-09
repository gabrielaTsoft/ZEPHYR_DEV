import ConnectToZephyr.zapiConnect;
import Utils.PropertiesManager;

import static ConnectToZephyr.zapiConnect.*;

public class mainTest {

    public static String keyProject = PropertiesManager.getDatoProperties("KEY_PROJECT");
    public static String nombreVersion = PropertiesManager.getDatoProperties("NOMBRE_VERSION");
    public static String nombreCiclo = PropertiesManager.getDatoProperties("NOMBRE_CICLO");
    public static String idTestCase = PropertiesManager.getDatoProperties("NOMBRE_ID_TEST_CASE");

    public static void main (String[] args) {
        getIDJiraProyect(keyProject);
        getIDVersionJira(getIDJiraProyect(keyProject), false, nombreVersion);
        getIDCycleJira(getIDJiraProyect(keyProject),nombreCiclo, getIDVersionJira(getIDJiraProyect(keyProject), false, nombreVersion));

        //  --> Para obtener datos relacionados con el test_case o Issue de tipo test
        getIDTestCase(idTestCase);
        getListOfTestSteps(idTestCase);
        createNewCycle(
                "chao",
                getIDJiraProyect(keyProject),
                getIDVersionJira(getIDJiraProyect(keyProject), false, nombreVersion),
                "Descripción del proyecto");
    }
}

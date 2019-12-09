import ConnectToZephyr.zapiConnect;
import Utils.PropertiesManager;

public class mainTest {

    public static String keyProject = PropertiesManager.getDatoProperties("KEY_PROJECT");
    public static String nombreRama = PropertiesManager.getDatoProperties("NOMBRE_RAMA");
    public static String nombreCiclo = PropertiesManager.getDatoProperties("NOMBRE_CICLO");
    public static String idTestCase = PropertiesManager.getDatoProperties("NOMBRE_ID_TEST_CASE");

    public static void main (String[] args) {
        zapiConnect.getIDJiraProyect(keyProject);

        zapiConnect.getIDRamaJira(zapiConnect.getIDJiraProyect(keyProject), false, nombreRama);

        zapiConnect.getIDCycleJira(zapiConnect.getIDJiraProyect(keyProject),nombreCiclo,zapiConnect.getIDRamaJira(zapiConnect.getIDJiraProyect(keyProject), false, nombreRama));

        zapiConnect.getIDTestCase(idTestCase);

        zapiConnect.getListOfTestSteps(idTestCase);
    }
}

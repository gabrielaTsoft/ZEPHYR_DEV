import ConnectToZephyr.zapiConnect;
import Utils.PropertiesManager;

public class mainTest {

    public static String keyProject = PropertiesManager.getDatoProperties("KEY_PROJECT");
    public static String nombreRama = PropertiesManager.getDatoProperties("NOMBRE_RAMA");
    public static String nombreCiclo = PropertiesManager.getDatoProperties("NOMBRE_CICLO");

    public static void main (String[] args) {
        zapiConnect.GetIDJiraProyect(keyProject);

        zapiConnect.GetVersionIDJira(zapiConnect.GetIDJiraProyect(keyProject), false, nombreRama);

        zapiConnect.GetIDCycleJira(zapiConnect.GetIDJiraProyect(keyProject),nombreCiclo,zapiConnect.GetVersionIDJira(zapiConnect.GetIDJiraProyect(keyProject), false, nombreRama));
    }
}

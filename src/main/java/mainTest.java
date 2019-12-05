import ConnectToZephyr.zapiConnect;
import Utils.PropertiesManager;

public class mainTest {

    public static String keyProject = PropertiesManager.getDatoProperties("KEY_PROJECT");
    public static String nombreRelease = PropertiesManager.getDatoProperties("NOMBRE_RELEASE");

    public static void main (String[] args) {
        zapiConnect.GetIDJiraProyect(keyProject);

        zapiConnect.GetVersionIDJira(zapiConnect.GetIDJiraProyect(keyProject), false, nombreRelease);
    }
}

import ConnectToZephyr.zapiConnect;
import Utils.PropertiesManager;

public class mainTest {

    public static void main (String[] args) {
        /*zapiConnect.GetIDJiraProyect(PropertiesManager.getDatoProperties("KEY_PROJECT_FIF"));*/

        zapiConnect.GetVersionIDJira(
                zapiConnect.GetIDJiraProyect(
                        PropertiesManager.getDatoProperties("KEY_PROJECT_FIF")), false, PropertiesManager.getDatoProperties("NOMBRE_RELEASE"));
        ;

        /*zapiConnect.GetCycleIDJira(
                zapiConnect.GetIDJiraProyect(PropertiesManager.getDatoProperties("KEY_PROJECT_FIF")), PropertiesManager.getDatoProperties("NOMBRE_CICLO_FIF"),
                zapiConnect.GetVersionIDJira(zapiConnect.GetIDJiraProyect(PropertiesManager.getDatoProperties("KEY_PROJECT_FIF")), false));*/
    }
}

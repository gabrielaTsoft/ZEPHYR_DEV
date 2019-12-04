import ConnectToZephyr.zapiConnect;
import Utils.PropertiesManager;

public class mainTest {

    public static void main (String[] args) {
        zapiConnect.returnIDJiraProyect(PropertiesManager.getDatoProperties("KEY_PROYECT_FIF"));
    }
}

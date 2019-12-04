package ConnectToZephyr;

import Utils.PropertiesManager;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * Clase que permite interactuar con la API de Zephyr(ZAPI) y con la API de JIRA 8.5.0
 * @author Alejandro Contreras
 * @Date 04/12/2019
 * @version 1.0
 */
public class zapiConnect {

    private static final Logger LOGGER = Logger.getLogger(String.valueOf(zapiConnect.class));
    public static String urlBaseJIRA = PropertiesManager.getDatoProperties("URL_JIRA_FIF");
    public static Client clientJIRA;

    /**
     * Permite crear un cliente que se conecta a JIRA
     *
     * @return Objeto del tipo Client, que posee la conexión a JIRA con sus credenciales
     */
    public static Client getClientJIRA() {

        //TODO Actualizar método para que funcione con las últimas librerías de Maven Central
        LOGGER.info("Creando cliente JIRA");

        try {
            if (clientJIRA == null) {
                HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(
                        PropertiesManager.getDatoProperties("JIRA_USER_FIF"),
                        PropertiesManager.getDatoProperties("JIRA_PASS_FIF")
                );

                clientJIRA = ClientBuilder.newClient();
                clientJIRA.register(feature);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return clientJIRA;
    }

    /**
     * Método que utiliza la petición GET /rest/api/2/project/{projectIdOrKey}.
     * Se obtiene la respuesta en formato JSON, luego se descompone el JSon para obtener sólo el ID del Proyecto
     * y almacenarlo en la variable IDProyecto
     * @param claveProyectoEnJira Hay que pasarle por parámetro la Clave del proyecto en JIRA
     * @return Devuelve el ID del proyecto en JIRA (el cual es distinto a la KEY del proyecto)
     */
    public static String returnIDJiraProyect(String claveProyectoEnJira) {

        String idProyecto = "";
        String strJSON = "";
        Response response;

        try {
            response = zapiConnect.getClientJIRA().target(
                    urlBaseJIRA + "/rest/api/2/project/" + claveProyectoEnJira)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get();

            strJSON = response.readEntity(String.class);

            if (response.getStatus() == 200) {
                System.out.println("Respuesta de la petición: -->  " + response.getStatus());
                JSONObject json = new JSONObject(strJSON);
                idProyecto = json.get("id").toString();
                System.out.println("Id del proyecto --> " + idProyecto);
            } else {
                LOGGER.warning("La respuesta del servidor es --> " + response.getStatus());
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

        return idProyecto;
    }

    /**
     * Permite obtener la versión del proyecto (Released o Unreleased, dentro de Unreleased: Unscheduled, versión 2.0, versión 3.0, etc)
     * Utiliza método GET /rest/zapi/latest/util/versionBoard-list?projectId={idProyecto}
     *
     * @param idProyecto Debe recibir como parámetro el ID del Proyecto
     * @return el campo "value" del array "unreleasedVersions"
     */
    public static String returnVersionIDJira(String idProyecto) {

        String versionID = null, strJSON;
        Response response;

        try {
            response = zapiConnect.getClientJIRA().target(
                    urlBaseJIRA + "/rest/zapi/latest/util/versionBoard-list?projectId=" + idProyecto)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get();

            strJSON = response.readEntity(String.class);

            if(response.getStatus() == 200) {
                JSONObject json = new JSONObject(strJSON);
                if(!strJSON.contains("Versión")) {
                    versionID = "-1";
                }else {
                    versionID = json.getJSONArray("unreleasedVersions").getJSONObject(1).get("value").toString();
                    System.out.println(versionID);
                }
            }else {
                System.out.println("No se ha encontrado la versión del proyecto id: " + idProyecto);
            }

        }catch(Exception e) {
            System.out.println(e.getMessage());
            System.out.println(versionID);
        }
        System.out.println(versionID);
        return versionID;
    }

    public static String returnCycleIDJira(String idProyecto, String nomCiclo, String version) {

        String idCiclo = "";
        Response response;

        try {
            response = zapiConnect.getClientJIRA().target(
                    urlBaseJIRA + "/rest/zapi/latest/cycle?projectId=" + idProyecto + "&versionId=" + version)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get();

            String strJSON = response.readEntity(String.class);

            if(response.getStatus() == 200) {

                JSONObject json = new JSONObject(strJSON);
                JSONArray names = json.names();

                ObjectMapper mapper = new ObjectMapper();

                //Realizar un for para recorrer al usuario asignado
                for(int i = 0; i < names.length(); i++) {
                    if(!names.getString(i).equals("recordsCount")) {

                        JsonNode node = mapper.readTree(strJSON).path(names.getString(i));
                        System.out.println(node.get("name").toString());
                        if(node.get("name").toString().replaceAll("\"", "").contains(nomCiclo)) {
                            idCiclo = names.getString(i);
                            break;
                        }
                    }
                }
            }else {
                System.out.println("No se ha encontrado el ciclo: " + nomCiclo);
            }
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return idCiclo;
    }
}

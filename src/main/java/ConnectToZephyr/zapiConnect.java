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

/**
 * Clase que permite interactuar con la API de Zephyr(ZAPI) y con la API de JIRA 8.5.0
 * @author Alejandro Contreras (Adaptación de trabajo realizado por Hector Castillo)
 * @Date 04/12/2019
 * @version 1.0
 */
public class zapiConnect {

    public static Client clientJIRA;

    public static String urlBaseJIRA = PropertiesManager.getDatoProperties("URL_JIRA");
    public static String jiraUSER = PropertiesManager.getDatoProperties("JIRA_USER");
    public static String jiraPass = PropertiesManager.getDatoProperties("JIRA_PASS");


    /**
     * Permite crear un cliente que se conecta a JIRA
     *
     * @return Objeto del tipo Client, que posee la conexión a JIRA con sus credenciales
     */
    public static Client getClientJIRA() {
        //TODO Actualizar método para que funcione con las últimas librerías de Maven Central
        try {
            if (clientJIRA == null) {
                HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(jiraUSER, jiraPass);
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
    public static String GetIDJiraProyect(String claveProyectoEnJira) {

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
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

        return idProyecto;
    }

    /**
     * Permite obtener el Id del release en el cual están hechas las pruebas --> Puede ser release o Unreleased
     * y dentro de Release o Unreleased el nombre de la rama
     *
     * @param idProyecto Recibe el id del proyecto, el cual se puede obtener a través de GET /rest/api/2/project/{projectIdOrKey}
     * @param ISRelease Hay que entregarle por parámetro si pertenece a Release o Unrelease, si es Release --> true, si es Unrelease --> false
     * @param nombreRama Hay que proporcionarle el nombre de la Rama creada ya sea dentro de release (ISRElease-->true), o unreleased  (ISReleased-->false)
     * @return versionID, el cual es el campoo "valor" o ID asociado al nombre de la Rama
     */
    public static String GetVersionIDJira(String idProyecto, boolean ISRelease, String nombreRama) {

        String versionID = "";
        String strJSON = "";
        Response response;

        try {
            response = zapiConnect.getClientJIRA().target(
                    urlBaseJIRA + "/rest/zapi/latest/util/versionBoard-list?projectId=" + idProyecto)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get();

            strJSON = response.readEntity(String.class);

            if(response.getStatus() == 200) {
                JSONObject json = new JSONObject(strJSON);

                if (ISRelease){
                    JSONArray releasedVersions = json.getJSONArray("releasedVersions");
                    //System.out.println(releasedVersions);

                    for (int i = 0; i < releasedVersions.length(); i++){
                        String contenidoUnrelease = releasedVersions.getJSONObject(i).get("label").toString().trim();
                        //System.out.println(contenidoUnrelease);

                        if (contenidoUnrelease.contains(nombreRama)){
                            versionID = releasedVersions.getJSONObject(i).get("value").toString().trim();
                        }
                    }

                } else {
                    JSONArray unreleasedVersions = json.getJSONArray("unreleasedVersions");
                    //System.out.println(unreleasedVersions);

                    for (int i = 0; i < unreleasedVersions.length(); i++){
                        String contenidoUnrelease = unreleasedVersions.getJSONObject(i).get("label").toString().trim();
                        //System.out.println(contenidoUnrelease);

                        if (contenidoUnrelease.contains(nombreRama)){
                            versionID = unreleasedVersions.getJSONObject(i).get("value").toString().trim();
                        }
                    }
                }
            }

        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("La versión es --> "+ versionID);
        return versionID;
    }

    /**
     * Método que permite obtener el Id del Diclo de JIRA que se esta invocando, pasándole por parámetro en nombre del Ciclo que ta está creado en Zephyr
     *
     * @param idProyecto Este se obtiene gracias al método GetIDJiraProyect
     * @param nomCiclo Este se pasa por parámetro como properties, sabiendo de antemano el nombre del ciclo creado,
     *                 pero este ciclo debe estar contenido en "unreleasedVersions", específicamente en (Unscheduled/Sin programar)
     *                 // TODO Este método debe ser refactorizado junto al método GetVersionIDJira, ya que dicho método sólo toma el id -1, el cual corresponde a "unreleasedVersions", específicamente en (Unscheduled/Sin programar)
     * @param version Este se obtiene gracias al método GetVersionIDJira
     * @return el Id del Ciclo ingresado por properties
     */
    public static String GetCycleIDJira(String idProyecto, String nomCiclo, String version) {
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
                JSONArray ArrayConIdsDeCiclo = json.names();
                System.out.println(ArrayConIdsDeCiclo);

                ObjectMapper mapper = new ObjectMapper();

                // Recorro los Ids de Ciclo de los ciclos "unreleasedVersions", ya que el parámetro ingresado fue -1 (Unscheduled / Sin programar)
                // Si le ingresaramos otro valor del version, recorrería los otros ciclos.
                for(int i = 0; i < ArrayConIdsDeCiclo.length(); i++) {
                    if(!ArrayConIdsDeCiclo.getString(i).equals("recordsCount")) {

                        JsonNode node = mapper.readTree(strJSON).path(ArrayConIdsDeCiclo.getString(i));

                        // node.get("name").toString().replaceAll("\"", "")  --> devuelve el nombre del ciclo
                        // Si el nombre del ciclo, coincide con el nombre del ciclo pasado por parámetro, entonces se asigna el id del ciclo que contiene ese nombre en su interior
                        if(node.get("name").toString().replaceAll("\"", "").contains(nomCiclo)) {
                            idCiclo = ArrayConIdsDeCiclo.getString(i);
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
        System.out.println("Id de ciclo es --> "+ idCiclo + ", el cual se corresponde con el ciclo con nombre de ciclo --> " + nomCiclo);
        return idCiclo;
    }
}

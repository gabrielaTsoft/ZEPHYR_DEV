package ConnectToZephyr;

import Utils.PropertiesManager;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
    private static final String CREDENTIALS = PropertiesManager.getDatoProperties("JIRA_USER") + ":"
            + PropertiesManager.getDatoProperties("JIRA_PASS");


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
     * Método que utiliza la petición GET /rest/api/2/project/{projectIdOrKey}  --> desde la API de JJIRA
     * Se obtiene la respuesta en formato JSON, luego se descompone el JSon para obtener sólo el ID del Proyecto
     * y almacenarlo en la variable IDProyecto
     *
     * @param claveProyectoEnJira Hay que pasarle por parámetro la Clave del proyecto en JIRA
     * @return Devuelve el ID del proyecto en JIRA (el cual es distinto a la KEY del proyecto)
     */
    public static String getIDJiraProyect(String claveProyectoEnJira) {

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
     * Permite obtener el ID de la rama, entregándole el id del Proyecto, luego si es release o no y finalmente el nombre de la rama
     *
     * @param idJiraProyect Recibe el id del proyecto, el cual se puede obtener a través de GET /rest/api/2/project/{projectIdOrKey}
     * @param ISRelease Hay que entregarle por parámetro si pertenece a Release o Unrelease, si es Release --> true, si es Unrelease --> false
     * @param nombreRama Hay que proporcionarle el nombre de la Rama creada ya sea dentro de release (ISRElease-->true), o unreleased  (ISReleased-->false)
     * @return versionID, el cual es el campo "valor" o ID asociado al nombre de la Rama
     */
    public static String getIDVersionJira(String idJiraProyect, boolean ISRelease, String nombreRama) {

        String versionID = "";
        String strJSON = "";
        Response response;

        try {
            response = zapiConnect.getClientJIRA().target(
                    urlBaseJIRA + "/rest/zapi/latest/util/versionBoard-list?projectId=" + idJiraProyect)
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
        System.out.println("El id de la rama es --> "+ versionID);
        return versionID;
    }

    /**
     * Método que permite obtener el id del ciclo de prueba que se pasa por parámetro en "nombreCicloJIRA" --> Nombre del ciclo en Jira
     * Realiza petición GET /rest/zapi/latest/cycle?projectId={idJiraProject}"&versionId={IDVersionJira}
     *
     * @param idJiraProject Recibe el id del proyecto, el cual se puede obtener a través de GET /rest/api/2/project/{projectIdOrKey}
     * @param nombreCicloJIRA Hay que entregarle por parámetro el NombreDelCiclo, el cual se puede ver o crear en JIRA
     * @param IDVersionJira Recibe el versiónID, a través del método GET /rest/zapi/latest/util/versionBoard-list?projectId={IdDelProyecto}
     * @return el IDCiclo, el cual es el ID asignado al ciclo de pruebas en JIRA que se consulta
     */
    public static String getIDCycleJira(String idJiraProject, String nombreCicloJIRA, String IDVersionJira) {
        String idCiclo = "";
        Response response;

        try {
            response = zapiConnect.getClientJIRA().target(
                    urlBaseJIRA + "/rest/zapi/latest/cycle?projectId=" + idJiraProject + "&versionId=" + IDVersionJira)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get();

            String strJSON = response.readEntity(String.class);

            if(response.getStatus() == 200) {
                JSONObject json = new JSONObject(strJSON);
                JSONArray ArrayConIdsDeCiclo = json.names();

                ObjectMapper mapper = new ObjectMapper();

                for(int i = 0; i < ArrayConIdsDeCiclo.length(); i++) {
                    if(!ArrayConIdsDeCiclo.getString(i).equals("recordsCount")) {
                        JsonNode node = mapper.readTree(strJSON).path(ArrayConIdsDeCiclo.getString(i));

                        // node.get("name").toString().replaceAll("\"", "")  --> devuelve el nombre del ciclo
                        // Si el nombre del ciclo, coincide con el nombre del ciclo pasado por parámetro, entonces se asigna el id del ciclo que contiene ese nombre en su interior
                        if(node.get("name").toString().replaceAll("\"", "").contains(nombreCicloJIRA)) {
                            idCiclo = ArrayConIdsDeCiclo.getString(i);
                            break;
                        }
                    }
                }
            }else {
                System.out.println("No se ha encontrado el ciclo: " + nombreCicloJIRA);
            }
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Id de ciclo es --> "+ idCiclo + ", el cual se corresponde con el ciclo con nombre de ciclo --> " + nombreCicloJIRA);
        return idCiclo;
    }

    /**
     * Método que permite obtener el ID del Issue, entendiendo que un Issue en JIRA es cualquier incidencia,
     * en nuestro caso la incidencia sería el TEST_CASE
     * Este método utiliza la petición GET /rest/api/2/issue/{issueIdOrKey} de la JIRA API 8.5.0
     *
     * @param claveTestCase Debemos pasar por parámetro el ID del Test-case que nos proporciona JIRA
     * @return Retorna el id de dicho test-case
     */
    public static String getIDTestCase(String claveTestCase){
        String idIssue = "";
        String strJSON = "";
        Response response;

        try {
            response = zapiConnect.getClientJIRA().target(
                    urlBaseJIRA + "/rest/api/2/issue/" + claveTestCase)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get();

            strJSON = response.readEntity(String.class);

            if (response.getStatus() == 200) {
                JSONObject json = new JSONObject(strJSON);
                idIssue = json.get("id").toString();
                System.out.println("Id del TestCase o Issue --> " + idIssue);
            }
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
        return idIssue;
    }

    /**
     * Método que permite obtener un ArrayList de tipo String con los pasos que debee poseer nuestro caso de prueba
     * Usa petición GET /rest/zapi/latest/teststep/{IDTestCase}?offset=0&limit=50
     *
     * @param IDTestCase Recibe como parámetro el Id del test case o Id del Issue (ambos son lo mismo)
     * @return Devuelve un ArrayList<String> el cual posee el Listado de los Pasos del Caso de Prueba
     */
    public static ArrayList<String>getListOfTestSteps(String IDTestCase){

        ArrayList<String> listOfTestSteps = new ArrayList<String>();
        String strJSON = "";
        Response response;

        try {
            response = zapiConnect.getClientJIRA().target(
                    urlBaseJIRA + "/rest/zapi/latest/teststep/" + zapiConnect.getIDTestCase(IDTestCase) + "?offset=0&limit=50")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get();

            strJSON = response.readEntity(String.class);

            if (response.getStatus() == 200) {
                JSONObject json = new JSONObject(strJSON);
                JSONArray collectionOFTestSteps = json.getJSONArray("stepBeanCollection");

                for (int i = 0; i < collectionOFTestSteps.length(); i++){
                    listOfTestSteps.add(collectionOFTestSteps.getJSONObject(i).get("step").toString().trim());
                }

            }
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
        System.out.println(listOfTestSteps);
        return listOfTestSteps;
    }

    /**
     * Método POST que permite generar una nuevo Ciclo en JIRA
     * LLama al metodo POST de ZAPI --> Create New Cycle --> /rest/zapi/latest/cycle
     *
     * @param nombreDelCiclo Le damos el nombre que queremos que tenga el ciclo
     * @param idJiraProyect Pasamos por parámetro el ID del proyecto en JIJRA
     * @param IDVersionJira Pasamos por parámetro el ID de la versión en la cual queremos que se cree nuestro ciclo
     * @param description Le damos la descripción que deseemos a nuestro nuevo ciclo.
     */
    public static void createNewCycle(String nombreDelCiclo, String idJiraProyect, String IDVersionJira, String description) {
        Entity<?> payload;
        Response response;

        try {
            payload = Entity.json(
                    "{  " +
                            "\"clonedCycleId\": \"\",  " +
                            "\"name\": \"" + nombreDelCiclo + "\", " +
                            "\"build\": \"\",  " +
                            "\"environment\": \"\",  " +
                            "\"description\": \"" + description + "\",  " +
                            "\"startDate\": \"4/Dec/19\", " +
                            "\"endDate\": \"30/Dec/19\",  " +
                            "\"projectId\": \""+ idJiraProyect +"\",  " +
                            "\"versionId\": \""+ IDVersionJira +"\",  " +
                            "\"sprintId\": 1}");

            response = zapiConnect.getClientJIRA().target(
                    urlBaseJIRA + "/rest/zapi/latest/cycle")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(payload);

        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Metodo PUT que permite actualizar un ciclo creado en JIRA
     * LLama al metodo PUT de ZAPI --> Update Cycle Information --> /rest/zapi/latest/cycle
     *
     * @param IDCycle Paso por parametro el Id del Ciclo que quiero actualizar, este lo obtengo con metodo getIDCycleJira
     * @param nuevoNombreDelCiclo Le doy por ejemplo un nuevo nombre, pero puedo parametrizar cualquier otro campo
     * @param IDVersionJIRA Debo darle el ID de la version en JIRA
     */
    public static void updateCycle(String IDCycle, String nuevoNombreDelCiclo, String IDVersionJIRA) {
        Entity<?> payload;
        Response response;

        try {
            payload = Entity.json(
                    "{  " +
                            "\"id\": \"" + IDCycle + "\", " +
                            "\"name\": \"" + nuevoNombreDelCiclo + "\", " +
                            "\"build\": \"Update Build\",  " +
                            "\"environment\": \"Update Environment\",  " +
                            "\"description\": \"Actualizacion de la descripcion\",  " +
                            "\"startDate\": \"4/Dec/19\", " +
                            "\"endDate\": \"30/Dec/19\",  " +
                            "\"versionId\": \""+ IDVersionJIRA +"\",  " +
                            "\"folderId\": \"\"}");

            response = zapiConnect.getClientJIRA().target(
                    urlBaseJIRA + "/rest/zapi/latest/cycle")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .put(payload);

        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Move Executions to Cycle  --> OJO con este método, al parecer se ejecuta la prueba y luego se mueve hacia el ciclo de ejecución
    // Copy Executions to Cycle  --> Método relacionado con el anterior
    // Get the list of folder for a cycle   --> También hay métodos que piden el folder bajo un ciclo.
    // Get list of Step Result --> Get List of Step Result by Execution Id
    // Get StepResult Information --> Get Single Step Result Information by StepResult Id

    // MÉTODOS DE UTILRESOURCE son importantes, ya que nos dan información respecto a tablas que clasifican los datos
    // Get Cycle Criteria Info  --> Da información importantísima respecto a executionStatuses, issueStatuses, priorities

    // Para obtener datos --> Get Execution Statuses, Priorities, Components, Labels

    /**
     * Permite agregar un attachment, ya sea a un step result o a un execution
     * Método POST --> /rest/zapi/latest/attachment?entityId={entityID}&entityType={entityType}
     *
     * @param fileToUpload Le pasamos el archivo que deseamos subir
     * @param entityType Le decimos que tipo de entidad queremos subir  ---> Puede ser stepresult o execution
     * @param entityId De acuerdo al tipo de entidad, le damos el id ya sea del execution o del stepresult
     * @throws RuntimeException En caso de error en RunTime
     * @throws IOException En caso de error con el archivo
     */
    public static void addAttachment(File fileToUpload, String entityType, String entityId) throws RuntimeException, IOException {
        // set up proxy for http client
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.useSystemProperties();
        CloseableHttpClient httpClient = clientBuilder.build();

        HttpPost httpPost = new HttpPost(
                urlBaseJIRA + "/rest/zapi/latest/attachment?entityId=" + entityId + "&entityType=" +  entityType);
        httpPost.setHeader(
                "X-Atlassian-Token", "nocheck");

        if (!CREDENTIALS.isEmpty()) {
            String encoding = new Base64().encodeToString(CREDENTIALS.getBytes());
            httpPost.setHeader("Authorization", "Basic " + encoding);
        }

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", fileToUpload, ContentType.APPLICATION_OCTET_STREAM, fileToUpload.getName());
        HttpEntity multipart = builder.build();
        httpPost.setEntity(multipart);

        CloseableHttpResponse response = httpClient.execute(httpPost);
        HttpEntity responseEntity = response.getEntity();
        if (responseEntity != null) {
            EntityUtils.consume(responseEntity);
        }

        // ensure file was uploaded correctly
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Error al subir el archivo");
        }
    }
}

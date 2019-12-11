**FALABELLA SAT - CORE AUTOMATION**
----

Proyecto que interactúa con la API de JIRA y Zephyr:

----
* API de ZEPHYR
    * https://getzephyr.docs.apiary.io
* API DE JIRA
    * https://docs.atlassian.com/software/jira/docs/api/REST/8.5.0/
---   
El objetivo del proyecto es proveer de métodos base para la automatización en el proyecto SAT de falabella.
A través de la clase zapiConnect se puede acceder a JIRA y el plugin para gestión de pruebas Zephyr.

Listado de métodos:

    public static Client getClientJIRA()
    public static String getIDJiraProyect(String claveProyectoEnJira)
    public static String getIDVersionJira(String idJiraProyect, boolean ISRelease, String nombreRama)
    public static String getIDCycleJira(String idJiraProject, String nombreCicloJIRA, String IDVersionJira)
    public static String getIDTestCase(String claveTestCase)
    public static ArrayList<String> getListOfTestSteps(String IDTestCase)
    public static void createNewCycle(String nombreDelCiclo, String idJiraProyect, String IDVersionJira, String description) {
    public static void updateCycle(String IDCycle, String nuevoNombreDelCiclo, String IDVersionJIRA)
    public static void addAttachment(File fileToUpload, String entityType, String entityId)



    

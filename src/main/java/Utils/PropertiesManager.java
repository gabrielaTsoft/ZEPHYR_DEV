package Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class PropertiesManager {

    public static final String ARCHIVO_PROPIEDADES = "datosSAT.properties";
    private static Properties properties;

    static {

        InputStream input;

        try {
            properties = new Properties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            input = loader.getResourceAsStream(ARCHIVO_PROPIEDADES);
            properties.load(input);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Excepcion EMT- Archivo " + ARCHIVO_PROPIEDADES + " no encontrado.", e);
        } catch (IOException e) {
            throw new RuntimeException("Excepcion EMT- Archivo " + ARCHIVO_PROPIEDADES + " no pudo ser cargado.", e);
        }
    }

    public static String getDatoProperties(String value) {
        return properties.getProperty(value);
    }

    public static Integer getDatoPropertiesInt(String value) {
        return Integer.parseInt(properties.getProperty(value));
    }
}
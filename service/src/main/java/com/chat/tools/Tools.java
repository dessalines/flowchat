package com.chat.tools;

import com.chat.DataSources;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.DBException;
import org.postgresql.jdbc.PgArray;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static javafx.scene.input.KeyCode.T;

/**
 * Created by tyler on 5/24/16.
 */
public class Tools {
    public static ObjectMapper JACKSON = new ObjectMapper();

    public static final void dbInit() {

        try {
            new DB("default").open("org.postgresql.Driver",
                    "jdbc:postgresql://127.0.0.1/test",
                    "tyler",
                    "");
        } catch (DBException e) {
            e.printStackTrace();
            dbClose();
            dbInit();
        }

    }

    public static final void dbClose() {
        new DB("default").close();
    }


    public static Properties loadProperties(String propertiesFileLocation) {

        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(propertiesFileLocation);

            // load a properties file
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;

    }

    public static <T> List<T> convertArrayToList(Array arr) {
        try {
            T[] larr = (T[]) arr.getArray();

            List<T> list = new ArrayList<>(Arrays.asList(larr));

            return list;
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}

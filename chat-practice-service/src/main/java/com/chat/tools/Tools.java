package com.chat.tools;

import com.chat.DataSources;
import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.DBException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by tyler on 5/24/16.
 */
public class Tools {

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
}

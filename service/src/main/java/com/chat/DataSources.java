package com.chat;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import com.chat.tools.Tools;

/**
 * Created by tyler on 5/24/16.
 */
public class DataSources {

    public static final String CODE_DIR = System.getProperty("user.dir");

//    public static final String DB_PROP_FILE = CODE_DIR + "/db.properties"; TODO add this

//    public static final Properties DB_PROP = Tools.loadProperties(DB_PROP_FILE);

    public static final Boolean SSL = false;

    public static final Integer EXPIRE_SECONDS = 86400 * 7; // stays logged in for 7 days


    public static final String PROPERTIES_FILE = CODE_DIR + "/app.properties";

    public static final Properties PROPERTIES = Tools.loadProperties(PROPERTIES_FILE);

}

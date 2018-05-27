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

    public static Boolean SSL = false;

    public static final Integer EXPIRE_SECONDS = 86400 * 7; // stays logged in for 7 days

    public static final String PROPERTIES_FILE = CODE_DIR + "/flowchat.properties";

    public static Properties PROPERTIES = Tools.loadProperties(PROPERTIES_FILE);

    public static final String CHANGELOG_MASTER = "liquibase/db.changelog-master.xml";

}

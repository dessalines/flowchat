package com.chat.tools;

import ch.qos.logback.classic.Logger;
import com.chat.DataSources;
import com.chat.db.Actions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;
import org.javalite.http.Http;
import org.postgresql.jdbc.PgArray;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpCookie;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.sql.Array;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static javafx.scene.input.KeyCode.T;

/**
 * Created by tyler on 5/24/16.
 */
public class Tools {

    public static Logger log = (Logger) LoggerFactory.getLogger(Tools.class);


    public static ObjectMapper JACKSON = new ObjectMapper();
    public static TypeFactory typeFactory = JACKSON.getTypeFactory();

    public static MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);



    private static final SecureRandom RANDOM = new SecureRandom();

    public static final BasicPasswordEncryptor PASS_ENCRYPT = new BasicPasswordEncryptor();

    public static final void dbInit() {

        try {
            new DB("default").open("org.postgresql.Driver",
                    "jdbc:postgresql://127.0.0.1/test",
                    "tyler",
                    "test");
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

    public static Map<String, String> cookieListToMap(List<HttpCookie> list) {
        return list.stream().collect(Collectors.toMap(
                HttpCookie::getName, HttpCookie::getValue));
    }

    public static String generateSecureRandom() {
        return new BigInteger(256, RANDOM).toString(32);
    }

    public static Timestamp newExpireTimestamp() {
        return new Timestamp(new Date().getTime() + 1000 * DataSources.EXPIRE_SECONDS);
    }

    public static final Map<String, String> createMapFromAjaxPost(String reqBody) {
        log.debug(reqBody);
        Map<String, String> postMap = new HashMap<String, String>();
        String[] split = reqBody.split("&");
        for (int i = 0; i < split.length; i++) {
            String[] keyValue = split[i].split("=");
            try {
                if (keyValue.length > 1) {
                    postMap.put(URLDecoder.decode(keyValue[0], "UTF-8"),URLDecoder.decode(keyValue[1], "UTF-8"));
                }
            } catch (UnsupportedEncodingException |ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                throw new NoSuchElementException(e.getMessage());
            }
        }

//		log.info(GSON2.toJson(postMap));

        return postMap;

    }

    public static final Map<String, String> createMapFromReqBody(String reqBody) {

        Map<String, String> map = new HashMap<>();
        try {
            map = JACKSON.readValue(reqBody, mapType);
        } catch(IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    public static Integer findIndexByIdInLazyList(LazyList<? extends Model> ctv, Long searchId) {
        Integer index = IntStream.range(0, ctv.size()).filter(c -> ctv.get(c).getLongId() == searchId).toArray()[0];
        return index;
    }

    public static String[] pgArrayAggToArray(String text) {
        return text.replaceAll("\\{|\\}", "").split(",");
    }

    public static String convertListToInQuery(Collection<?> col){
        return Arrays.toString(col.toArray()).replaceAll("\\[","(").replaceAll("\\]", ")");
    }
}

package com.chat.tools;

import ch.qos.logback.classic.Logger;
import com.chat.DataSources;
import com.chat.db.Actions;
import com.chat.db.Tables;
import com.chat.types.community.CommunityRole;
import com.chat.types.user.User;
import com.chat.webservice.ConstantsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.javalite.activejdbc.*;
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
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                    DataSources.PROPERTIES.getProperty("jdbc.url"),
                    DataSources.PROPERTIES.getProperty("jdbc.username"),
                    DataSources.PROPERTIES.getProperty("jdbc.password"));
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
        } finally  {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public static String constructQueryString(String query, String columnName) {

        try {
            query = java.net.URLDecoder.decode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String[] splitWords = query.split(" ");
        StringBuilder queryStr = new StringBuilder();

        for(int i = 0;;) {
            String word = splitWords[i++].replaceAll("'", "_");

            String likeQuery = columnName + " ilike '%" + word + "%'";

            queryStr.append(likeQuery);

            if (i < splitWords.length) {
                queryStr.append(" and ");
            } else {
                break;
            }
        }

        return queryStr.toString();

    }

    public static String constructOrderByCustom(String orderBy) {

        String orderByOut;
        if (orderBy.startsWith("time-")) {
            Long timeValue = Long.valueOf(orderBy.split("-")[1]);

            // For the custom sorting based on ranking
            orderByOut = "ranking(created, " + timeValue +
                    ",number_of_votes, " + ConstantsService.INSTANCE.getRankingConstants().getNumberOfVotesWeight() +
                    ",avg_rank, " + ConstantsService.INSTANCE.getRankingConstants().getAvgRankWeight() +
                    ") desc nulls last";

        } else {
            orderByOut = orderBy.replaceAll("__", " ").concat(" nulls last");
        }

        return orderByOut;
    }

    public static String constructOrderByPopularTagsCustom(String orderBy) {

        String orderByOut;
        if (orderBy.startsWith("time-")) {
            Long timeValue = Long.valueOf(orderBy.split("-")[1]);

            orderByOut = "ranking(created, " + timeValue +
                    ",count, " + ConstantsService.INSTANCE.getRankingConstants().getNumberOfVotesWeight() +
                    ") desc";
        } else {
            orderByOut = "created desc";
        }

        return orderByOut;
    }

    public static Set<Long> fetchCommunitiesFromParams(String communityParam, User userObj) {

        log.info("community param = " + communityParam);
        Set<Long> communityIds = new HashSet<>();
        if (communityParam.equals("all")) {
            return null;
        } else if (communityParam.equals("favorites")) {
            // Fetch the user's favorite communities
            LazyList<Tables.CommunityUser> favoriteCommunities =
                    Tables.CommunityUser.where("user_id = ? and community_role_id != ?",
                            userObj.getId(),
                            CommunityRole.BLOCKED.getVal());
            communityIds = favoriteCommunities.collectDistinct("community_id");
        } else {
            communityIds.add(Long.valueOf(communityParam));
        }

        if (communityIds.isEmpty()) {
            communityIds = null;
        }

        return communityIds;
    }

    public static void runLiquibase() {

        Liquibase liquibase = null;
        Connection c = null;
        try {
            c = DriverManager.getConnection(DataSources.PROPERTIES.getProperty("jdbc.url"),
                    DataSources.PROPERTIES.getProperty("jdbc.username"),
                    DataSources.PROPERTIES.getProperty("jdbc.password"));

            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
            log.info(DataSources.CHANGELOG_MASTER);
            liquibase = new Liquibase(DataSources.CHANGELOG_MASTER, new FileSystemResourceAccessor(), database);
            liquibase.update("main");
        } catch (SQLException | LiquibaseException e) {
            e.printStackTrace();
            throw new NoSuchElementException(e.getMessage());
        } finally {
            if (c != null) {
                try {
                    c.rollback();
                    c.close();
                } catch (SQLException e) {
                    //nothing to do
                }
            }
        }
    }

}

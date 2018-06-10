package com.chat.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpCookie;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.chat.DataSources;
import com.chat.db.Tables;
import com.chat.types.community.CommunityRole;
import com.chat.types.user.User;
import com.chat.webservice.ConstantsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.jasypt.util.password.BasicPasswordEncryptor;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import spark.Request;

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
    
    public static final HikariConfig hikariConfig() {
        HikariConfig hc = new HikariConfig();
        DataSources.PROPERTIES = Tools.loadProperties(DataSources.PROPERTIES_FILE);
        hc.setJdbcUrl(DataSources.PROPERTIES.getProperty("jdbc.url"));
        hc.setUsername(DataSources.PROPERTIES.getProperty("jdbc.username"));
        hc.setPassword(DataSources.PROPERTIES.getProperty("jdbc.password"));
        hc.setMaximumPoolSize(10);
        return hc;
    }

    public static final HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig());

    public static final void dbInit() {
        Base.open(hikariDataSource); // get connection from pool
    }

    public static final void dbClose() {
        Base.close();
    }

    public static final Algorithm getJWTAlgorithm() {
        Algorithm JWTAlgorithm = null;
        try {
            JWTAlgorithm = Algorithm.HMAC256(DataSources.PROPERTIES.getProperty("jdbc.password"));
        } catch (UnsupportedEncodingException | JWTCreationException exception) {
        }

        return JWTAlgorithm;
    }

    public static final DecodedJWT decodeJWTToken(String token) {

        DecodedJWT jwt = null;

        try {
            JWTVerifier verifier = JWT.require(getJWTAlgorithm()).withIssuer("flowchat").build(); 
            jwt = verifier.verify(token);
        } catch (JWTVerificationException e) {
        }

        return jwt;
    }

    public static final User getUserFromJWTHeader(Request req) {
        return User.create(req.headers("token"));
    }

    public static Properties loadProperties(String propertiesFileLocation) {

        Properties prop = new Properties();

        Map<String, String> env = System.getenv();
        for (String varName : env.keySet()) {
            switch (varName) {
                case "FLOWCHAT_DB_URL":
                    prop.setProperty("jdbc.url", env.get(varName));
                    break;
                case "FLOWCHAT_DB_USERNAME":
                    prop.setProperty("jdbc.username", env.get(varName));
                    break;
                case "FLOWCHAT_DB_PASSWORD":
                    prop.setProperty("jdbc.password", env.get(varName));
                    break;
                case "SORTING_CREATED_WEIGHT":
                    prop.setProperty("sorting_created_weight", env.get(varName));
                    break;
                case "SORTING_NUMBER_OF_VOTES_WEIGHT":
                    prop.setProperty("sorting_number_of_votes_weight", env.get(varName));
                    break;
                case "SORTING_AVG_RANK_WEIGHT":
                    prop.setProperty("sorting_avg_rank_weight", env.get(varName));
                    break;


            }
        }

        if (prop.getProperty("jdbc.url") != null) {
            return prop;
        }



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

//		log.debug(GSON2.toJson(postMap));

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

    public static String constructOrderByCustom(String orderBy, Boolean singleCommunity) {

        String orderByOut = (singleCommunity) ? "stickied desc, " : "";
        if (orderBy.startsWith("time-")) {
            Long timeValue = Long.valueOf(orderBy.split("-")[1]);

            // For the custom sorting based on ranking
            orderByOut += "ranking(created, " + timeValue +
                    ",number_of_votes, " + ConstantsService.INSTANCE.getRankingConstants().getNumberOfVotesWeight() +
                    ",avg_rank, " + ConstantsService.INSTANCE.getRankingConstants().getAvgRankWeight() +
                    ") desc nulls last";

        } else {
            orderByOut += orderBy.replaceAll("__", " ").concat(" nulls last");
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

        log.debug("community param = " + communityParam);
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
            log.debug(DataSources.CHANGELOG_MASTER);
            liquibase = new Liquibase(DataSources.CHANGELOG_MASTER, new ClassLoaderResourceAccessor(), database);
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

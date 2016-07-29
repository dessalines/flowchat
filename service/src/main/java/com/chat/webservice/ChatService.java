package com.chat.webservice;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.chat.DataSources;
import com.chat.db.Actions;
import com.chat.db.Transformations;
import com.chat.tools.Tools;
import com.chat.types.*;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Paginator;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.chat.db.Tables.*;
import static spark.Spark.*;

public class ChatService {

    static Logger log = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    public static void main(String[] args) {

        log.setLevel(Level.toLevel("verbose"));
        log.getLoggerContext().getLogger("org.eclipse.jetty").setLevel(Level.OFF);
        log.getLoggerContext().getLogger("spark.webserver").setLevel(Level.OFF);

        staticFiles.externalLocation("../ui/dist");
        staticFiles.expireTime(600);

        // Instantiates the ranking constants
        ConstantsService.INSTANCE.getRankingConstants();

        // Set up websocket
        webSocket("/threaded_chat", ThreadedChatWebSocket.class);

        // Set up endpoints
        Endpoints.status();
        Endpoints.user();
        Endpoints.discussion();
        Endpoints.tag();
        Endpoints.reply();
        Endpoints.exceptions();

        init();

    }

}

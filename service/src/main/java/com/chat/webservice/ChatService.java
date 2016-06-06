package com.chat.webservice;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.chat.db.Tables;
import com.chat.db.Transformations;
import com.chat.tools.Tools;
import org.eclipse.jetty.websocket.api.Session;
import org.javalite.activejdbc.LazyList;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.chat.db.Tables.COMMENT_THREADED_VIEW;
import static spark.Spark.*;

public class ChatService {

    static Logger log = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);


	public static void main(String[] args) {

        log.setLevel(Level.toLevel("verbose"));
		log.getLoggerContext().getLogger("org.eclipse.jetty").setLevel(Level.OFF);
		log.getLoggerContext().getLogger("spark.webserver").setLevel(Level.OFF);

        staticFiles.externalLocation("../ui/dist");
//        staticFiles.expireTime(600);


		webSocket("/chat", ChatWebSocket.class);

        webSocket("/threaded_chat", ThreadedChatWebSocket.class);
		
		get("/test", (req, res) -> {
			return "{\"data\": [{\"message\":\"derp\"}]}";
		});

        before((req, res) -> {
            Tools.dbInit();
        });
        after((req, res) -> {
            Tools.dbClose();
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Content-Encoding", "gzip");
        });

//        get("/temp", (req, res) -> {
//            LazyList<Tables.CommentThreadedView> ctv = COMMENT_THREADED_VIEW.where("discussion_id = ?", 1);
//            List<Transformations.CommentObj> cos = Transformations.convertCommentsToEmbeddedObjects(ctv);
//            return Tools.JACKSON.writeValueAsString(cos);
//        });



		init();
	}




}

package com.chat.webservice;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.chat.db.Actions;
import com.chat.db.Tables;
import com.chat.db.Transformations;
import com.chat.tools.Tools;
import org.eclipse.jetty.websocket.api.Session;
import org.javalite.activejdbc.LazyList;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
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
//        staticFiles.expireTime(600);

        webSocket("/threaded_chat", ThreadedChatWebSocket.class);
		
		get("/test", (req, res) -> {
			return "{\"data\": [{\"message\":\"derp\"}]}";
		});

        // Get the user id
        get("get_user", (req, res) -> {

            try {

                UserLoginView uv = Actions.getOrCreateUserFromCookie(req, res);

                return uv.toJson(false);

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return e.getMessage();
            }

        });

        post("/login", (req, res) -> {
            try {

                Map<String, String> vars = Tools.createMapFromAjaxPost(req.body());

                String userOrEmail = vars.get("user_or_email");
                String password = vars.get("password");

                String message = Actions.login(userOrEmail, password, req, res);

                return message;

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return e.getMessage();
            }

        });

        post("/signup", (req, res) -> {
            try {

                Map<String, String> vars = Tools.createMapFromAjaxPost(req.body());

                String userName = vars.get("username");
                String password = vars.get("password");
                String email = vars.get("email");

                String message = Actions.signup(userName, password, email, req, res);

                return message;

            } catch (Exception e) {
                res.status(666);
                e.printStackTrace();
                return e.getMessage();
            }

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

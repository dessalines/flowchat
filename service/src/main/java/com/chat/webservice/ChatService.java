package com.chat.webservice;


import com.chat.db.Tables;
import com.chat.db.Transformations;
import com.chat.tools.Tools;
import org.eclipse.jetty.websocket.api.Session;
import org.javalite.activejdbc.LazyList;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.chat.db.Tables.COMMENT_THREADED_VIEW;
import static spark.Spark.*;

public class ChatService {

    static Map<Session, String> userNameMap = new HashMap<>();
    static Integer nextUserNumber = 1;

	public static void main(String[] args) {

        staticFiles.externalLocation("../ui/dist");
//        staticFiles.expireTime(600);

		webSocket("/chat", ChatWebSocket.class);
		
		get("/test", (req, res) -> {
			return "{\"data\": [{\"message\":\"derp\"}]}";
		});

        before((req, res) -> {
            Tools.dbInit();
        });
        after((req, res) -> {
            Tools.dbClose();
            res.header("Access-Control-Allow-Origin", "*");
        });

        get("/temp", (req, res) -> {
            LazyList<Tables.CommentThreadedView> ctv = COMMENT_THREADED_VIEW.where("discussion_id = ?", 1);
            List<Transformations.CommentObj> cos = Transformations.convertCommentsToEmbeddedObjects(ctv);
            return Tools.JACKSON.writeValueAsString(cos);
        });



		init();
	}

	//Sends a message from one user to all users, along with a list of current usernames
	public static void broadcastMessage(String sender, String message) {
		userNameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
			try {
                FullData fd = new FullData(new Message(sender, message), new ArrayList<>(userNameMap.values()));
                String json = Tools.JACKSON.writeValueAsString(fd);
                System.out.println(json);
				session.getRemote().sendString(json);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

    public static class Message {
        public String sender, message, time;

        public Message(String sender, String message) {
            this.sender = sender;
            this.message = message;
            this.time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        }

    }

    public static class FullData {
        public Message userMessage;
        public List<String> userList;

        public FullData(Message userMessage, List<String> userList) {
            this.userMessage = userMessage;
            this.userList = userList;
        }
    }


}

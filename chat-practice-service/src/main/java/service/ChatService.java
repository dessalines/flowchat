package service;


import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;

import java.text.SimpleDateFormat;
import java.util.*;

import static spark.Spark.get;
import static spark.Spark.init;
import static spark.Spark.webSocket;
import static spark.Spark.staticFiles;

public class ChatService {

    static Gson gson = new Gson();
    static Map<Session, String> userNameMap = new HashMap<>();
    static Integer nextUserNumber = 1;

	public static void main(String[] args) {

        staticFiles.externalLocation("../chat-practice-ui/dist");
//        staticFiles.expireTime(600);

		webSocket("/chat", ChatWebSocket.class);
		
		get("/test", (req, res) -> {
			res.header("Access-Control-Allow-Origin", "*");
			return "{\"data\": [{\"message\":\"derp\"}]}";
		});



		init();
	}

	//Sends a message from one user to all users, along with a list of current usernames
	public static void broadcastMessage(String sender, String message) {
		userNameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
			try {
                FullData fd = new FullData(new Message(sender, message), new ArrayList<>(userNameMap.values()));
                String json = gson.toJson(fd);
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

mvn clean install
pkill -9 -f target/chat-websocket.jar
nohup java -jar target/chat-websocket.jar >> log.out &

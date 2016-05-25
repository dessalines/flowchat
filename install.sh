# Building the front end
cd ui
npm i
ng build -prod
cd ..

# Building the back end
cd service
mvn clean install
pkill -9 -f target/chat-websocket.jar
nohup java -jar target/chat-websocket.jar &> log.out &


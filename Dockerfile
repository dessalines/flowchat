FROM openjdk:8
CMD cd app/service/; sleep 10s; java -jar target/flowchat.jar -docker
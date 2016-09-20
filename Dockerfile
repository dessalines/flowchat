FROM openjdk:8
ADD . app
CMD cd app/service/; sleep 5s; java -jar target/flowchat.jar -docker
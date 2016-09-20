FROM openjdk:8
ADD /service/target/flowchat.jar /app/service/target/flowchat.jar
ADD /service/target/flowchat.jar /app/service/target/flowchat.jar
ADD /service/src/main/resources/liquibase/ /app/service/src/main/resources/liquibase/
ADD /ui/dist /app/ui/dist
CMD cd app/service/; sleep 5s; java -jar target/flowchat.jar -docker
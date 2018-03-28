FROM node:9 as node-builder

ARG UI_PATH=/opt/flowchat/ui

# Hacky workaround for installing @angular/cli
RUN chmod a+w /usr/local/lib/node_modules && chmod a+w /usr/local/bin
USER node
RUN npm i -g @angular/cli@latest
USER root

WORKDIR ${UI_PATH}
COPY ui ${UI_PATH}
RUN yarn
RUN ng build -prod -aot


FROM maven:3.5-jdk-8 as java-builder

COPY service /opt/flowchat/service
COPY --from=node-builder /opt/flowchat/ui/dist /opt/flowchat/service/src/main/resources

WORKDIR /opt/flowchat/service
RUN mvn clean install -DskipTests


FROM openjdk:8-jre-slim

COPY --from=java-builder /opt/flowchat/service/target/flowchat.jar /opt/flowchat.jar

RUN unzip -o /opt/flowchat.jar -d /tmp/.flowchat.tmp
CMD ["java", "-jar", "/opt/flowchat.jar", "-liquibase"]

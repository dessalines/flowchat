
FROM node:9 as node-builder

# Hacky workaround for installing @angular/cli
# RUN chmod a+w /usr/local/lib/node_modules && chmod a+w /usr/local/bin
# USER node
# RUN npm i -g @angular/cli@latest

# Build front end resources
USER root
ARG UI_PATH=/opt/flowchat/ui
COPY ui ${UI_PATH}
WORKDIR ${UI_PATH}

ARG ENDPOINT_NAME=http://localhost:4567
RUN echo "ENDPOINT_NAME is ${ENDPOINT_NAME}"
RUN echo "export const environment = {production: true,endpoint: '${ENDPOINT_NAME}/',websocket: 'ws`echo ${ENDPOINT_NAME}|cut -b 5-999`/poll'};" > src/environments/environment.prod.ts
RUN cat src/environments/environment.prod.ts
RUN yarn
RUN yarn build --prod --aot


FROM maven:3.5-jdk-8 as java-builder

COPY service /opt/flowchat/service
COPY --from=node-builder /opt/flowchat/ui/dist /opt/flowchat/service/src/main/resources

WORKDIR /opt/flowchat/service
RUN mvn clean install -DskipTests -Dliquibase.skip

FROM openjdk:8-jre-slim

COPY --from=java-builder /opt/flowchat/service/target/flowchat.jar /opt/flowchat.jar
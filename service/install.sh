mvn clean install
pkill -9 -f target/flowchat.jar
unzip -q -o target/flowchat.jar -d /tmp/.flowchat.tmp
nohup java -jar target/flowchat.jar $@ >> log.out &

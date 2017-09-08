mvn clean install
pkill -9 -f target/flowchat.jar
echo -e 'jdbc.url=jdbc\:postgresql\://127.0.0.1/flowchat\njdbc.username=flowchat\njdbc.password=asdf\nsorting_created_weight=3600\nsorting_number_of_votes_weight=0.001\nsorting_avg_rank_weight=0.01\nreddit_client_id=\nreddit_client_secret=\nreddit_username=\nreddit_password='>flowchat.properties
nohup java -jar target/flowchat.jar $@ >> log.out &

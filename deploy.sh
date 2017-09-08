export SSHPASS=$DEPLOY_PASS
sshpass -e ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST "rm -f flowchat.jar; \
curl -s https://api.github.com/repos/dessalines/flowchat/releases/latest | grep browser_download_url | cut -d '\"' -f 4 | xargs wget; \
pkill -f flowchat.jar; \
unzip -o flowchat.jar -d /tmp/.flowchat.tmp; \
java -jar flowchat.jar -ssl ~/keystore.jks -reddit_import >> log.out &"
if [ "$TRAVIS_BRANCH" == "master"  ] && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
    export SSHPASS=$DEPLOY_PASS
    sshpass -e ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST "curl -s https://api.github.com/repos/dessalines/flowchat/releases/latest | grep browser_download_url | cut -d '"' -f 4 | xargs wget; java -jar flowchat.jar"

if [ "$TRAVIS_BRANCH" == "master"  ] && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
    export SSHPASS=$DEPLOY_PASS
    sshpass -e ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST "source ~/.nvm/nvm.sh;cd ~/git/flowchat;git checkout master;git pull;./install_prod.sh"
fi

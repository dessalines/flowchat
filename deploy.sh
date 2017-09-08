export SSHPASS=$DEPLOY_PASS
sshpass -e ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST "./restart_flowchat.sh"
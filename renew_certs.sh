sudo service apache2 stop
sudo iptables -t nat -D PREROUTING 1
sudo letsencrypt certonly --email happydooby@gmail.com -d flow-chat.com -d www.flow-chat.com
sudo openssl pkcs12 -export -out keystore.p12 -inkey /etc/letsencrypt/live/flow-chat.com/privkey.pem -in /etc/letsencrypt/live/flow-chat.com/fullchain.pem
keytool -importkeystore -destkeystore keystore.jks -srcstoretype PKCS12 -srckeystore keystore.p12
sudo iptables -t nat -I PREROUTING -p tcp --dport 443 -j REDIRECT --to-ports 4567
sudo service apache2 restart
cd git/flowchat
./install_prod.sh

# sudo iptables -t nat -I PREROUTING -p tcp --dport 443 -j REDIRECT --to-ports 4567
sudo iptables -t nat -I PREROUTING -p tcp --dport 80 -j REDIRECT --to-ports 4567

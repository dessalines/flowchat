# Building the front end
cd ui
npm i
npm install -g angular-cli@latest
npm i angular-cli@latest --save-dev
ng build -prod
cd ..

# Building the back end
cd service
sh install.sh -ssl ~/keystore.jks

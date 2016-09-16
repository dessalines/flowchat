# Building the front end
cd ui
npm i
npm install -g angular-cli@webpack
npm i angular-cli@webpack --save-dev
ng build -prod
cd ..

# Building the back end
cd service
sh install.sh -ssl ~/keystore.jks

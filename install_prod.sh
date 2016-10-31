# Building the front end
cd ui
yarn global add angular-cli@latest
yarn
ng build -prod
cd ..

# Building the back end
cd service
sh install.sh -ssl ~/keystore.jks

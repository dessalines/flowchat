# Building the front end
cd ui
yarn global add angular-cli@latest
yarn
ng build -prod -aot
cd ..

# Building the back end
cd service
sh install.sh -ssl ~/keystore.jks

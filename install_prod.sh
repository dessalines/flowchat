# Building the front end
cd ui
yarn
ng build -prod -aot
cd ..

# Building the back end
cd service
sh install.sh -ssl ~/keystore.jks

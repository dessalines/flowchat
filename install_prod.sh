# Building the front end
cd ui
#npm i -g @angular/cli
yarn
ng build -prod
cd ..

# Building the back end
cd service
sh install.sh -ssl ~/keystore.jks

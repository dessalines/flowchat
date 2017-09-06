# Building the front end
cd ui
#npm i -g @angular/cli
yarn
ng build -prod -aot
cd ..

# Building the back end
cd service
sh install.sh -ssl ~/keystore.jks -reddit_import

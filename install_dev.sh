# Building the front end
cd ui
#npm i -g @angular/cli
yarn
ng build -aot
cp -R dist/ ../service/src/main/resources
cd ..

# Building the back end
cd service
sh install.sh

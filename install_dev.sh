# Building the front end
cd ui
#npm i -g @angular/cli
yarn
ng build -aot
cd ..

# Building the back end
cd service
sh install.sh

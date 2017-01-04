# Building the front end
cd ui
yarn
ng build -aot
cd ..

# Building the back end
cd service
sh install.sh

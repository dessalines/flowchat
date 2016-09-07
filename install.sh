# Building the front end
cd ui
npm i
ng build $@
cd ..

# Building the back end
cd service
sh install.sh

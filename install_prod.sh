# Building the front end
cd ui
npm i -g @angular/cli
yarn
ng build -prod -aot
cp -R dist/ ../service/src/main/resources
cd ..
echo $PWD

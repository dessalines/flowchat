# Building the front end
cd ui
yarn
yarn build --prod --aot
cp -R dist/ ../service/src/main/resources
cd ..
echo $PWD

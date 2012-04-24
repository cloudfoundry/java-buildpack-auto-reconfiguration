cd ../
mvn javadoc:javadoc
cd publish-api
bundle install
bundle package
rm -rf apidocs
mkdir -p apidocs
cp -r ../target/site/apidocs/* apidocs
vmc update

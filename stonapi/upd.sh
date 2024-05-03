curl -o openapi.json 'https://api.ston.fi/api-doc/openapi.json'

openapi-generator generate -i openapi.json -g kotlin --additional-properties packageName=fi.ston

rm -rf openapi.json
rm -rf settings.gradle
rm -rf docs
rm -rf gradle
rm -rf .openapi-generator
rm -rf README.md
rm -rf build.gradle
rm -rf .openapi-generator-ignore
rm -rf gradlew
rm -rf gradlew.bat
rm -rf src/test

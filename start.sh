set -a
source .env
set +a
mvn clean install -DskipTests
java -jar target/vega-java-examples-1.0-SNAPSHOT-jar-with-dependencies.jar

echo "Bygger sykepengesoknad-ikke-sendt-altinnvarsel latest"

./gradlew bootJar

docker build . -t sykepengesoknad-ikke-sendt-altinnvarsel:latest

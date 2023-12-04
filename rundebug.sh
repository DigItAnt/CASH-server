#export KEYCLOAK_AUTH_SERVER_URL=https://lari2.ilc.cnr.it/auth
export KEYCLOAK_AUTH_SERVER_URL=https://digitant.ilc.cnr.it/auth_demo
export KEYCLOAK_REALM=princnr
export KEYCLOAK_RESOURCE=princlient

java -Dserver.port=8080 -DNOSECURITY=true -jar  target/cash-0.0.1-SNAPSHOT.jar

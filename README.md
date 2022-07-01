# API Codegen

API Codegen for Scala + Akka-HTTP. 

## How to run server

* Generate code - `./gradlew guardrail` (code in `build/guardrail-sources`)
* Run server - `./gradlew run`

## Helpful curl commands

```bash

# Get all users
curl -s -XGET "localhost:1234/users" | jq

# Get all users
curl -s -XGET "localhost:1234/users/0001" | jq

# Add user to server
curl -s -XPOST "localhost:1234/users" -H "Content-Type: application/json" --data '{ "id": "0003", "name": "User3" }' | jq

```
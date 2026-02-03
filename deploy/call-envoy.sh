#!/bin/bash

#TOKEN=""
TOKEN=$(./keycloak-tokens.sh)

#curl -H "Authorization: Bearer ${TOKEN}" \
#  -H "X-Request-ID: 1234" \
#  -H "x-client-request-id: 1235" \
#  http://localhost:8080/

curl -H "Authorization: Bearer ${TOKEN}" \
  -H "X-Request-ID: 1234" \
  -H "x-client-request-id: 1235" \
  http://localhost:8080/v1/ad/

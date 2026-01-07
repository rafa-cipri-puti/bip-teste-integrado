#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="docker-compose.yml"
BASE_URL="http://localhost:8080"

require() { command -v "$1" >/dev/null 2>&1 || { echo "Missing $1"; exit 1; }; }
require docker
require curl
require jq

cleanup() {
  docker compose -f "$COMPOSE_FILE" down -v --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT

echo ">> Starting stack"
docker compose -f "$COMPOSE_FILE" up -d --build

echo ">> Waiting for API health"
for i in {1..60}; do
  if curl -fsS "$BASE_URL/actuator/health" | jq -e '.status == "UP"' >/dev/null; then
    echo ">> API is UP"
    break
  fi
  sleep 1
  if [[ $i -eq 60 ]]; then
    echo "API did not become healthy"
    docker compose -f "$COMPOSE_FILE" logs --no-color api
    exit 1
  fi
done

request() {
  local method="$1" url="$2" body="${3:-}"
  if [[ -n "$body" ]]; then
    curl -sS -X "$method" "$BASE_URL$url" \
      -H 'Content-Type: application/json' \
      -d "$body" \
      -w '\n%{http_code}'
  else
    curl -sS -X "$method" "$BASE_URL$url" -w '\n%{http_code}'
  fi
}

assert_status() {
  local got="$1" expected="$2"
  if [[ "$got" != "$expected" ]]; then
    echo "Expected HTTP $expected but got $got"
    exit 1
  fi
}

echo ">> Create beneficio A"
resp=$(request POST "/api/v1/beneficios" '{
  "nome": "A",
  "descricao": "Origem",
  "valor": 100.00,
  "ativo": true
}')
body=$(echo "$resp" | sed '$d')
code=$(echo "$resp" | tail -n 1)
assert_status "$code" "201"
fromId=$(echo "$body" | jq -r '.id')
echo "fromId=$fromId"

echo ">> Create beneficio B"
resp=$(request POST "/api/v1/beneficios" '{
  "nome": "B",
  "descricao": "Destino",
  "valor": 10.00,
  "ativo": true
}')
body=$(echo "$resp" | sed '$d')
code=$(echo "$resp" | tail -n 1)
assert_status "$code" "201"
toId=$(echo "$body" | jq -r '.id')
echo "toId=$toId"

echo ">> Transfer 25.50 from A to B"
resp=$(request POST "/api/v1/beneficios/transfer" "{
  \"fromId\": $fromId,
  \"toId\": $toId,
  \"amount\": 25.50
}")
body=$(echo "$resp" | sed '$d')
code=$(echo "$resp" | tail -n 1)
assert_status "$code" "200"

fromValue=$(echo "$body" | jq -r '.fromBeneficio.valor')
toValue=$(echo "$body" | jq -r '.toBeneficio.valor')

if [[ "$fromValue" != "74.5" && "$fromValue" != "74.50" ]]; then
  echo "Expected fromBeneficio.valor 74.50, got $fromValue"
  echo "$body"
  exit 1
fi
if [[ "$toValue" != "35.5" && "$toValue" != "35.50" ]]; then
  echo "Expected toBeneficio.valor 35.50, got $toValue"
  echo "$body"
  exit 1
fi

echo ">> List beneficios (should include A and B)"
resp=$(request GET "/api/v1/beneficios?page=0&size=20&sort=id,asc")
body=$(echo "$resp" | sed '$d')
code=$(echo "$resp" | tail -n 1)
assert_status "$code" "200"
echo "$body" | jq '.'
echo "$body" | jq -e '.content | length >= 2' >/dev/null


echo ">> Delete both"
resp=$(request DELETE "/api/v1/beneficios/$fromId")
code=$(echo "$resp" | tail -n 1)
assert_status "$code" "204"

resp=$(request DELETE "/api/v1/beneficios/$toId")
code=$(echo "$resp" | tail -n 1)
assert_status "$code" "204"

echo "E2E OK"

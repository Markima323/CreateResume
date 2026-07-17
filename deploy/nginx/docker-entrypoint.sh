#!/bin/sh
set -eu

: "${SERVER_NAME:?SERVER_NAME is required}"
: "${BASIC_AUTH_USER:?BASIC_AUTH_USER is required}"
: "${BASIC_AUTH_PASSWORD:?BASIC_AUTH_PASSWORD is required}"

certificate="/etc/letsencrypt/live/${SERVER_NAME}/fullchain.pem"
private_key="/etc/letsencrypt/live/${SERVER_NAME}/privkey.pem"

if [ ! -s "$certificate" ] || [ ! -s "$private_key" ]; then
  echo "TLS certificate for ${SERVER_NAME} is missing. Run deploy/init-letsencrypt.sh first." >&2
  exit 1
fi

htpasswd -bcB /etc/nginx/.htpasswd "$BASIC_AUTH_USER" "$BASIC_AUTH_PASSWORD" >/dev/null
envsubst '${SERVER_NAME}' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf

# Certbot replaces certificate files during renewal. Reload periodically so
# Nginx picks up renewed certificates without requiring a container restart.
(while sleep 6h; do nginx -s reload; done) &

exec nginx -g 'daemon off;'

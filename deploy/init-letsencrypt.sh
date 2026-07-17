#!/usr/bin/env sh
set -eu

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 <domain> <email>" >&2
  exit 1
fi

domain="$1"
email="$2"

case "$domain" in
  *[!A-Za-z0-9.-]*|'') echo "Invalid domain: $domain" >&2; exit 1 ;;
esac

mkdir -p certbot/conf certbot/www

if [ -s "certbot/conf/live/${domain}/fullchain.pem" ]; then
  echo "A certificate for ${domain} already exists. Nothing to do."
  exit 0
fi

echo "Requesting the first Let's Encrypt certificate for ${domain}..."
echo "The domain must already point to this server and ports 80/443 must be open."

docker run --rm \
  -p 80:80 \
  -v "$(pwd)/certbot/conf:/etc/letsencrypt" \
  -v "$(pwd)/certbot/www:/var/www/certbot" \
  certbot/certbot:latest certonly \
  --standalone \
  --non-interactive \
  --agree-tos \
  --no-eff-email \
  --email "$email" \
  -d "$domain"

echo "Certificate created. You can now start compose.prod.yml."

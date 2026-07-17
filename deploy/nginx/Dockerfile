FROM nginx:1.27-alpine

RUN apk add --no-cache apache2-utils

COPY default.conf.template /etc/nginx/templates/default.conf.template
COPY docker-entrypoint.sh /usr/local/bin/resume-nginx-entrypoint

RUN chmod +x /usr/local/bin/resume-nginx-entrypoint

ENTRYPOINT ["/usr/local/bin/resume-nginx-entrypoint"]

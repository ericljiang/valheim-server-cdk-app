sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config \
  -m ec2 \
  -s \
  -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json
systemctl start docker
/usr/bin/docker create \
  --name valheim \
  --log-driver=awslogs \
  --log-opt awslogs-group="$LOG_GROUP"/application \
  --log-opt awslogs-create-group=true \
  --cap-add=sys_nice \
  --stop-timeout 120 \
  -v /etc/valheim:/config:Z \
  -v /opt/valheim:/opt/valheim:Z \
  -p 2456-2457:2456-2457/udp \
  -p 80:80/tcp \
  --env-file /etc/sysconfig/valheim-server \
  ghcr.io/lloesche/valheim-server@sha256:e3fb5d2812841123f4e3185a64a1055261e23655632413a49fdf2486475fb9f7
systemctl daemon-reload
systemctl enable valheim.service
systemctl start valheim.service

sudo shutdown -P

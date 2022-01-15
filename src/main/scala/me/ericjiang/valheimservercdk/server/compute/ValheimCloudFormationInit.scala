package me.ericjiang.valheimservercdk.server.compute

import software.amazon.awscdk.services.ec2.{CloudFormationInit, InitCommand, InitFile, InitPackage}

object ValheimCloudFormationInit {
  def apply(stageName: String, backupBucketName: String, region: String): CloudFormationInit = CloudFormationInit.fromElements(
    InitPackage.yum("docker"),
    InitPackage.yum("jq"),
    InitFile.fromString("/etc/sysconfig/valheim-server",
      raw"""SERVER_NAME=Yeah
           |SERVER_PORT=2456
           |WORLD_NAME=yeahh
           |SERVER_PASS=yeah1234
           |SERVER_PUBLIC=true
           |STATUS_HTTP=true
           |BACKUPS_CRON=*/30 * * * *
           |DISCORD_WEBHOOK=https://discord.com/api/webhooks/930203489722826813/9r6qTG5_n162Fb2u6yISOvDh9GZ2kVdXKvCWGYUMKHUjTuMfGXOTE58w2gwYYOnhuZGD
           |POST_BOOTSTRAP_HOOK=apt-get update && DEBIAN_FRONTEND=noninteractive apt-get -y install awscli
           |POST_SERVER_LISTENING_HOOK=curl -sfSL -X POST -H "Content-Type: application/json" -d "{\"username\":\"Valheim\",\"content\":\"Valheim server started\"}" "$$DISCORD_WEBHOOK"
           |POST_BACKUP_HOOK=aws s3 cp @BACKUP_FILE@ s3://$backupBucketName/
           |PRE_SERVER_SHUTDOWN_HOOK=supervisorctl signal HUP valheim-backup && sleep 60
           |POST_SERVER_SHUTDOWN_HOOK=aws s3 cp @BACKUP_FILE@ s3://$backupBucketName/
           |""".stripMargin),
    InitFile.fromString("/etc/systemd/system/valheim.service",
      // Modified from https://github.com/lloesche/valheim-server-docker/blob/main/valheim.service
      raw"""[Unit]
           |Description=Valheim Server
           |After=docker.service
           |Requires=docker.service
           |ConditionPathExists=/etc/sysconfig/valheim-server
           |
           |[Service]
           |TimeoutStartSec=0
           |ExecStartPre=-/usr/bin/docker stop %n
           |ExecStartPre=-/usr/bin/docker rm %n
           |ExecStart=/usr/bin/docker run \
           |          --log-driver=awslogs \
           |          --log-opt awslogs-group=$stageName/application \
           |          --log-opt awslogs-create-group=true \
           |          --name %n \
           |          --pull=always \
           |          --rm \
           |          --cap-add=sys_nice \
           |          --stop-timeout 120 \
           |          -v /etc/valheim:/config:Z \
           |          -v /opt/valheim:/opt/valheim:Z \
           |          -p 2456-2457:2456-2457/udp \
           |          -p 80:80/tcp \
           |          --env-file /etc/sysconfig/valheim-server \
           |          ghcr.io/lloesche/valheim-server
           |ExecStop=/usr/bin/docker stop %n
           |Restart=always
           |RestartSec=10s
           |
           |[Install]
           |WantedBy=multi-user.target
           |""".stripMargin),
    InitFile.fromString("/usr/local/bin/put-player-count-metric.sh",
      raw"""aws cloudwatch put-metric-data \
           |  --region $region \
           |  --metric-name PlayerCount \
           |  --namespace ValheimServer \
           |  --dimensions Stage=$stageName \
           |  --value $$(curl -s localhost:80/status.json | jq '.player_count')
           |""".stripMargin),
    InitFile.fromString("/etc/cron.d/put-player-count-metric",
      """*/5 * * * * root /bin/sh /usr/local/bin/put-player-count-metric.sh
        |""".stripMargin),
    InitCommand.shellCommand(
      """systemctl daemon-reload
        |systemctl enable valheim.service
        |systemctl start valheim.service
        |""".stripMargin)
  )
}

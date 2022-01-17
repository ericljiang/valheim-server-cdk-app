#!/bin/sh
aws cloudwatch put-metric-data \
  --region $REGION \
  --metric-name PlayerCount \
  --namespace ValheimServer \
  --dimensions Stage=$STAGE_NAME \
  --value $(curl -s localhost:80/status.json | jq '.player_count')

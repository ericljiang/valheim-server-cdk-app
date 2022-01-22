#!/bin/sh
echo "PlayerCount:$(curl -s localhost:80/status.json | jq '.player_count')|c" | nc -u 127.0.0.1 8125

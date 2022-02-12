#!/bin/sh
echo "Uptime:$(awk '{print $1}' /proc/uptime)|g" | nc -u 127.0.0.1 8125

#!/bin/bash

while :;
do
	java -server -Dfile.encoding=UTF-8 -Xms10G -Xmx10G -XX:+OptimizeFill -XX:+EliminateLocks \
	-XX:AutoBoxCacheMax=65536 -XX:+UseCompressedOops -XX:+AggressiveOpts -XX:+UseLargePages \
	-cp config:./lib/* l2s.gameserver.GameServer > log/stdout.log 2>&1

	[ $? -ne 2 ] && break
	sleep 30;
done


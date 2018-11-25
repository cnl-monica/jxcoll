#!/bin/bash

#PIDFILE=$5
#$1 -jar $2 $3>> $4
$1 -jar $2 $3 $4

#ps aux | grep $2 | head -1 | awk '{print $2}'> $PIDFILE

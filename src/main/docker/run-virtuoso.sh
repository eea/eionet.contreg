#!/bin/bash

CONFIG_FILE='/virtuoso.ini'
VIRT_INI_TPL='/tmp/virtuoso.ini.j2'

j2 "$VIRT_INI_TPL" > $CONFIG_FILE

#if [ ! -z "$VIRTUOSO_DB_PATH" ]; then
#    mkdir $VIRTUOSO_DB_PATH/dumps
#fi

/bin/bash /startup.sh
#!/bin/bash

while inotifywait -q -r -e modify --exclude '#$' ./src; do
    mvn -q install && echo "Ok"
done


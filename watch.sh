#!/bin/bash

while inotifywait -q -r -e modify --exclude '#$' ./src; do
    mvn install
done


#!/usr/bin/env bash
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd ${DIR}
mvn -e versions:set \
    -DgroupId=net.sourceforge.peers \
    -DartifactId=peers \
    -DnewVersion=0.5.2-SNAPSHOT \
    -DgenerateBackupPoms=false
mvn -e  clean compile deploy -DskipTests

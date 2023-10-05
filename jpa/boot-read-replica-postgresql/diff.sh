#!/usr/bin/env bash

./mvnw liquibase:diff \
 -Dliquibase.diffChangeLogFile=diff.sql \



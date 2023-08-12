#!/usr/bin/env bash

mvn liquibase:diff \
 -Dliquibase.diffChangeLogFile=diff.sql \



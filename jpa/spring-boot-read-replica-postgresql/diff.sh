#!/usr/bin/env bash

mvn liquibase:diff \
 -Dliquibase.diffChangeLogFile=diff.sql \
 -Dliquibase.referenceUrl=jdbc:postgresql://localhost:5432/my_database \
 -Dliquibase.referenceUsername=postgres_write \
 -Dliquibase.referencePassword=postgres_write \
 -Dliquibase.referenceDriver=org.postgresql.Driver



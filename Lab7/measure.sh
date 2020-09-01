#!/bin/bash
psql -h 127.0.0.1 mydb < create_tables.sql > /dev/null
sleep 5

echo "Query time without indexes"
cat <(echo '\timing') queries.sql |psql -h 127.0.0.1 mydb | grep Time | awk -F "Time" '{print "Query" FNR $2;}'

psql -h 127.0.0.1 mydb < create_indexes.sql > /dev/null

echo "Query time with indexes"
cat <(echo '\timing') queries.sql |psql -h 127.0.0.1 mydb | grep Time | awk -F "Time" '{print "Query" FNR $2;}'


#!/bin/bash

echo "Creating sequence..."
cat <(echo 'CREATE SEQUENCE part_number_seq  START WITH 50000;')|psql -h 127.0.0.1 mydb

echo "Creating trigger and procedure..."
psql -h 127.0.0.1 mydb < triggers.sql

echo "Testing insert..."
cat <(echo "Insert into part_nyc(supplier, color, on_hand, descr) Values (0, 0, 20, 'Description')")|psql -h 127.0.0.1 mydb
cat <(echo 'SELECT part_number FROM part_nyc WHERE on_hand=20;')|psql -h 127.0.0.1 mydb

#!/usr/bin/env bash

until printf "" 2>>/dev/null >>/dev/tcp/cassandra/9042; do
    sleep 5;
    echo "Waiting for cassandra...";
done

echo "Creating keyspace"
cqlsh cassandra -e "CREATE KEYSPACE IF NOT EXISTS marketplace WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'dc1' : 1 };"
echo "OK"
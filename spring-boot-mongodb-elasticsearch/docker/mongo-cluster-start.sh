#!/bin/bash

docker-compose -f docker-compose-cluster.yml  up -d

sleep 5

docker exec -it mongo1 mongosh --eval "rs.initiate({_id: \"myReplicaSet\",members: [{_id: 0, host: \"mongo1\"},{_id: 1, host: \"mongo2\"},{_id: 2, host: \"mongo3\"}]})"

sleep 15

docker exec -it mongo1 mongosh --eval "rs.status()"

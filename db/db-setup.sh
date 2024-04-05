#!/bin/bash
sleep 10

mongosh --host db:27017 <<EOF
  var cfg = {
    "_id": "myReplicaSet",
    "version": 1,
    "members": [
      {
        "_id": 0,
        "host": "db:27017",
        "priority": 2
      },
      {
        "_id": 1,
        "host": "db-sec1:27017",
        "priority": 1
      },
      {
        "_id": 2,
        "host": "db-sec2:27017",
        "priority": 1
      }
    ]
  };
  rs.initiate(cfg);
  rs.status();
EOF
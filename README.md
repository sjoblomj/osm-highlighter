docker-compose up -d && sleep 5 && docker exec -it postgis bash

root@68cefaa735b6:/# psql -h localhost -U jj -d features

#!/usr/bin/env bash

# In order to be able to access pfs-payments-sit account
echo "  Assuming account pfs-payments-sit"
$(docker-compose run -T --rm cmd-helper stsassume pfs-payments-sit 2> /dev/null)

echo "  Run tests in group: $TESTGROUPS"

docker-compose run -T --rm gradle gradle testGroups -Pgroups=$TESTGROUPS
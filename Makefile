SHELL := /bin/bash -e
TENANT ?= payby

.PHONY: runTests

runTests:
	@echo "run tests group: $$TESTGROUPS"
  @docker-compose run --rm gradle gradle testGroups -Pgroups=$$TESTGROUPS

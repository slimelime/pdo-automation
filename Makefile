.PHONY: build

runTests:
	@echo "run tests group: $$GROUPS"
  @docker-compose run --rm gradle gradle testGroups -Pgroups=$$GROUPS

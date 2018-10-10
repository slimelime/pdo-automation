.PHONY: build ecr-login

app_name := pdo-automation
artifact_bucket := myob-buildkite-paycorp-build-artifacts
export AWS_DEFAULT_REGION ?= ap-southeast-2

ecr-login:
	`aws ecr get-login --no-include-email`

build: ecr-login
	@echo "--- running automation"
	@echo "group=$$TESTGROUPS"
	./scripts/end2end.sh


all:

0.11.1: build_cgc

build_cgc:
	$(info $$MAKECMDGOALS is [$(MAKECMDGOALS)])
	./bin/clone-or-update-cgc
	./bin/version-cgc $(MAKECMDGOALS)
	./bin/build-cgc

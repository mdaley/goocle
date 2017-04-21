all:

0.7.0: build_cgc generate_src build_goocle

0.11.1: build_cgc 

build_cgc:
	$(info $$MAKECMDGOALS is [$(MAKECMDGOALS)])
	./bin/clone-or-update-cgc
	./bin/version-cgc $(MAKECMDGOALS)
	./bin/build-cgc

generate_src:
	./bin/generate-src $(MAKECMDGOALS)

build_goocle:
	./bin/build-goocle

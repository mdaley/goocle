all:

0.7.0: build_cgc

0.9.3: build_cgc

0.9.4: build_cgc

build_cgc:
	$(info $$MAKECMDGOALS is [$(MAKECMDGOALS)])
	./bin/clone-or-update-cgc
	./bin/version-cgc $(MAKECMDGOALS)
	./bin/build-cgc

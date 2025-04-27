SHELL = /bin/bash

# Flags to Java interpreter: check assertions
JFLAGS = -ea

# See comment in ../Makefile
PYTHON = python

RMAKE = "$(MAKE)"

# Modify CLASSPATH to correctly handle spaces in paths
# TESTER = CLASSPATH="$$(pwd)/..:$(CLASSPATH):;$$(pwd)/..;\"C:\\Program Files\\Java\\jdk-21\\lib\""
TESTER = CLASSPATH="$$(pwd)/..:.:$${CLASSPATH}" $(PYTHON) tester.py

TESTER_FLAGS =

TESTS = samples/*.in student_tests/*.in *.in

.PHONY: default check clean std

# First, and therefore default, target.
default:
	$(RMAKE) -C ..
	$(RMAKE) PYTHON=$(PYTHON) check

check:
	@echo "Testing application gitlet.Main..."
	$(TESTER) $(TESTER_FLAGS) $(TESTS)

# 'make clean' will clean up stuff you can reconstruct.
clean:
	$(RM) -r */*~ *~ __pycache__

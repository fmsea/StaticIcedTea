JARFILE=./target/DFA_SMT-1.0-SNAPSHOT.jar
GIT_COMMIT:=$(shell git rev-parse --short=16 HEAD)
.PHONY: all
all: package

$(JARFILE):
	mvn --batch-mode package

.PHONY: package
package: dfa-smt dfa-analysis dfa-z3

.PHONY: dfa-smt
dfa-smt: dfa-smt-$(GIT_COMMIT).tar.gz

.PHONY: dfa-analysis
dfa-analysis: dfa-analysis-$(GIT_COMMIT).tar.gz

.PHONY: dfa-z3
dfa-z3: dfa-z3-$(GIT_COMMIT).tar.gz

dfa-smt-$(GIT_COMMIT).tar.gz: guix/dfa-smt.scm $(JARFILE)
	guix time-machine -C ./channels.scm -- \
		pack --format=tarball \
		--relocatable \
		--relocatable \
		--symlink=/bin=bin \
		--symlink=/artifacts.jar=share/java/artifacts.jar \
		--symlink=/domains=share/PredicateDomains \
		--symlink=/lib=lib \
		--manifest=$< \
		--root=$@

dfa-analysis-$(GIT_COMMIT).tar.gz: guix/dfa-analysis.scm
	guix time-machine -C ./channels.scm -- \
		pack --format=tarball \
		--relocatable \
		--relocatable \
		--symlink=/bin=bin \
		--symlink=/lib=lib \
		--manifest=$< \
		--root=$@

dfa-z3-$(GIT_COMMIT).tar.gz: guix/dfa-z3.scm
	guix time-machine -C ./channels.scm -- \
		pack --format=tarball \
		--relocatable \
		--relocatable \
		--symlink=/bin=bin \
		--symlink=/lib=lib \
		--manifest=$< \
		--root=$@

JARFILE=./target/StaticIcedTea-1.0-SNAPSHOT.jar
GIT_COMMIT:=$(shell git rev-parse --short=16 HEAD)
.PHONY: all
all: package

$(JARFILE):
	mvn --batch-mode verify

.PHONY: package
package: $(JAR_FILE) dfa-smt dfa-analysis dfa-z3 dfa-artifacts

.PHONY: dfa-smt
dfa-smt: dfa-smt-$(GIT_COMMIT).squashfs

.PHONY: dfa-analysis
dfa-analysis: dfa-analysis-$(GIT_COMMIT).squashfs

.PHONY: dfa-z3
dfa-z3: dfa-z3-$(GIT_COMMIT).squashfs

.PHONY: dfa-artifacts
dfa-artifacts: dfa-artifacts-$(GIT_COMMIT).tar.gz

dfa-smt-$(GIT_COMMIT).tar.gz: guix/dfa-smt.scm $(JARFILE)
	guix time-machine -C ./channels.scm -- \
		pack --format=tarball \
		--relocatable \
		--relocatable \
		--symlink=/bin=bin \
		--symlink=/lib=lib \
		--manifest=$< \
		--root=$@

dfa-smt-$(GIT_COMMIT).squashfs: guix/dfa-smt.scm $(JARFILE)
	guix time-machine -C ./channels.scm -- \
		pack --format=squashfs \
		--entry-point=bin/dfa \
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

dfa-analysis-$(GIT_COMMIT).squashfs: guix/dfa-analysis.scm
	guix time-machine -C ./channels.scm -- \
		pack --format=squashfs \
		--manifest=$< \
		--entry-point=bin/python \
		--root=$@

dfa-artifacts-$(GIT_COMMIT).tar.gz: guix/dfa-artifacts.scm
	guix time-machine -C ./channels.scm -- \
		pack --format=tarball \
		--symlink=/artifacts.jar=share/java/artifacts.jar \
		--symlink=/domains=share/PredicateDomains \
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

dfa-z3-$(GIT_COMMIT).squashfs: guix/dfa-z3.scm
	guix time-machine -C ./channels.scm -- \
		pack --format=squashfs \
		--entry-point=bin/z3 \
		--manifest=$< \
		--root=$@

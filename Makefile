JARFILE=./target/DFA_SMT-1.0-SNAPSHOT.jar
.PHONY: all
all: package

$(JARFILE):
	mvn --batch-mode package

.PHONY: package
package: package.scm $(JARFILE)
	guix time-machine -C ./channels.scm -- \
		pack --format=tarball \
		--relocatable \
		--relocatable \
		--symlink=/etc=etc \
		--symlink=/bin=bin \
		--symlink=/artifacts.jar=share/java/artifacts.jar \
		--symlink=/domains=share/PredicateDomains \
		--symlink=/lib=lib \
		--manifest=package.scm \
		--root=dfa-smt.tar.gz

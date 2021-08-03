# DFA SMT #

## Dependencies and Environment ##

* Java 11

  - Artifacts to be analyzed by Soot need to be compiled with Java 1.8.

* Maven

* Z3

### Nix ###

Using [Nix][nix] or [NixOS][nixos], the required dependencies are handled.
Simply enter the shell environment for the project:

```bash
nix-shell
```

A better user experience can be achieved using [direnv][direnv] and
[Lorri][lorri] to automatically enter the project environment when entering the
project.  However, IDE/editor integration requires extra work.

### Non-Nix ###

Download the Z3 sources and build the project for your machine.

```bash
git clone git://github.com/Z3Prover/z3.git
cd z3
git checkout z3-4.8.10
python scripts/mk_make.py --java
cd build
make
make install
```

Adjust the `make install` as necessary for your machine.

Add `Z3_DIR` to environment variables, pointing to the resulting installation
directory of Z3.  Similarly, append `${Z3_DIR}/lib` to your `LD_LIBRARY_PATH`
or `DYLD_LIBRARY_PATH` on MacOS.

```bash
export Z3_DIR=/usr/local/z3
```

Make sure `${Z3_DIR}/lib` contains the `libz3.so`, `libz3java.so` and
`com.microsoft.z3.jar` files.

In the root of the project, execute `mvn clean`.  This installs the z3 jar into
your local repository.  Now, build the project as normal.


## Building ##

After all dependencies are installed and resolved, we can build the project
using maven:

```bash
mvn clean test-compile
```

## Test Suite ##

Run the test suite in the expected way:

```bash
mvn test
```

## IDE/Editor Support ##

Maven can generate project files for IDE's:

* Eclipse:

  ```bash
  mvn eclipse:eclipse
  ```

* IntelliJ

  ```bash
  mvn idea:idea
  ```

## References ##

[nix]: https://nixos.org/

[nixos]: https://nixos.org/

[direnv]: https://direnv.net/

[lorri]: https://github.com/nix-community/lorri

# StaticIcedTea #

## Dependencies and environment ##

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

### Guix ###

Similarly, using [Guix][guix] or [GuixSD][guix], the required dependencies
are handled.  Simply enter a shell environment for the project:

```bash
guix time-machine -C ./channels.scm -- shell -m manifest.scm
```

Using the above ensures that each developers environment is using the same
version of the entire dependency tree on any machine at any time.

Again, adding [direnv][direnv] makes this automatic.  Though, you may need to
patch the `use_guix` function for caching and channel loading.

Currently, this does not extend to the JAR dependencies inside the project.
However, those are handled via [Maven][maven].

### Non-Nix/Guix ###

Download the Z3 sources and build the project for your machine.

```bash
git clone git://github.com/Z3Prover/z3.git
cd z3
git checkout z3-4.8.12
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

### Debug builds and graph exports ###

To get debug or trace level logging, modify the
`src/main/resources/simplelogger.properties` and set the desired logging level.
For example, to enable tracing, use the following:

```
org.slf4j.simpleLogger.defaultLogLevel=trace
```

To enable exports of graph states during analysis, set
`DFA_EXPORT_GRAPH_STATES` environment variable to `true`.

For example,

```bash
DFA_EXPORT_GRAPH_STATES=true java -jar ./target/StaticIcedTea-1.0-SNAPSHOT.jar \
    inczone-numerical \
    --classpath=./analysis/artifacts \
    --output=$(mktemp -d) \
    test.Fibonacci 1
```

In the output folder, there will be a new `test.Fibonacci_1` folder which
contains the GraphVIZ dot files for each flow output state.

To convert these graphs to a series of PNG's, we can use the following parallel
command (in the directory of the output created by the above `mktemp -d`):

```bash
parallel dot -Kcirco -Tpng {} -o{.}.png :::: $(find . -name '*.dot')
```

## Test suite ##

Run the test suite in the expected way:

```bash
mvn test
```

## Packaging ##

Create the JAR (dependencies included) file, use the usual goal:

```bash
mvn package
```

## IDE/editor support ##

Maven can generate project files for IDE's:

* Eclipse:

  ```bash
  mvn eclipse:eclipse
  ```

* IntelliJ

  ```bash
  mvn idea:idea
  ```

Notice: Eclipse and IntelliJ need to be informed of the "environment".  This
can be accomplished either by using the "non-nix/guix" instructions, or by
utilizing plugins which integrate with [direnv][direnv] or similar tools.

## R2/Borah deployments using Guix ##

If using [Guix][guix] as described above, we can create "packs" which can be
uploaded to R2 or Borah and used without needing to setup the dependencies on
either of these clusters.

Create the pack using the following command:

```bash
guix time-machine -C channels.scm -- pack -RR -S /bin=bin -S /etc=etc -m manifest.scm
```

The resulting archive can then be uploaded to R2/Borah via scp.

Finally, unpack the tarball into your home directory and source the profile:

```bash
tar -zxf ${archive}.tar.gz
source ./etc/profile
```

Test that it works by using something like the following:

```bash
./bin/java -version
```

> Unfortunately, this does not work since Borah does not support user
> namespaces.

## Acknowledgments #

This project was developed with support from the U.S. National Science
Foundation under award CCF-19-42044.

### Authors ###

- [Elena Sherman](https://github.com/esherman77)

- [Kenny Ballou](https://github.com/kennyballou)

## License ##

This project and its software--- except where plainly stated otherwise, are
released AS-IS, WITHOUT WARRANTY, as Free and Open Source Software under the
terms and conditions of The GNU Public License 3.0.  You should have received a
copy of the [license text][gpl] with your distribution.  If not, please read
about the GPL: https://www.gnu.org/licenses/gpl-3.0.

## References ##

[nix]: https://nixos.org/

[nixos]: https://nixos.org/

[direnv]: https://direnv.net/

[lorri]: https://github.com/nix-community/lorri

[guix]: https://guix.gnu.org/

[gpl]: https://www.gnu.org/licenses/gpl-3.0

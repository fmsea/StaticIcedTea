{ pkgs ? import ./nix {} }:

let
  jdk = pkgs.openjdk11;
  maven = (pkgs.maven.override {
    jdk = jdk;
  });
  z3 = (pkgs.z3.override {
    javaBindings = true;
    jdk = jdk;
  });
  python = (pkgs.python38.buildEnv.override {
    extraLibs = [
      pkgs.python38Packages.matplotlib
      pkgs.python38Packages.pandas
      pkgs.python38Packages.scipy
      pkgs.python38Packages.numpy
    ];
  });
in pkgs.mkShell {
  nativeBuildInputs = [
    jdk
    maven

    # keep this line if you use bash
    pkgs.bashInteractive
    pkgs.gnuplot
    python
  ];
  buildInputs = [
    jdk
    z3
    z3.lib
  ];

  Z3_DIR="${z3.lib}";
  LD_LIBRARY_PATH="${z3.lib}/lib";
}

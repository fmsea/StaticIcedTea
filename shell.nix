{ pkgs ? import <nixpkgs> {} }:

let
  jdk = pkgs.openjdk8;
  maven = (pkgs.maven.override {
    jdk = jdk;
  });
  z3 = (pkgs.z3.override {
    javaBindings = true;
    jdk = jdk;
  });
  ppl = (pkgs.callPackage ./nix/ppl.nix {
    javaBindings = true;
    jdk = jdk;
  });
in pkgs.mkShell {
  nativeBuildInputs = [
    jdk
    maven

    # keep this line if you use bash
    pkgs.bashInteractive
  ];
  buildInputs = [
    jdk
    ppl
    z3
    z3.lib
  ];

  Z3_DIR="${z3.lib}";
  LD_LIBRARY_PATH="${z3.lib}/lib:${ppl}/lib";
  PPL_DIR="${ppl}";
  PPL_JNI="${ppl}/lib/ppl/libppl_java.so";
}

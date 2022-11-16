(define-module (DFA)
  #:use-module (ice-9 regex)
  #:use-module (guix packages)
  #:use-module (guix gexp)
  #:use-module (guix profiles)
  #:use-module (guix build utils)
  #:use-module (guix build-system ant)
  #:use-module (guix build-system copy)
  #:use-module (guix build-system gnu)
  #:use-module (guix build-system trivial)
  #:use-module (gnu packages)
  #:use-module (gnu packages bash)
  #:use-module ((gnu packages java) #:prefix java:)
  #:use-module (manifest))

(define dfa-smt-jar
  (package
    (name "dfa-smt-jar")
    (version "1.0-SNAPSHOT")
    (source (local-file "./target/DFA_SMT-1.0-SNAPSHOT.jar"))
    (build-system copy-build-system)
    (arguments
     '(#:install-plan '(("DFA_SMT-1.0-SNAPSHOT.jar" "lib/DFA_SMT.jar"))))
    (home-page "https://github.com/BoiseState/DFA_SMT")
    (synopsis "The JAR file for executing analyses.")
    (description "The whole enchilada")
    (license #f)))

(define artifacts
  (let ((select-java-files (lambda (file _)
                             (not (or (string-match ".+/Neq/.+.java" file)
                                      (string-match ".+/newV.java" file))))))
    (package
     (name "artifacts-for-analysis")
     (version "0")
     (source (local-file "./analysis/artifacts"
                         #:recursive? #t
                         #:select? select-java-files))
     (build-system ant-build-system)
     (arguments
      `(#:jar-name "artifacts.jar"
        #:source-dir "./"
        #:tests? #false
        ))
     (home-page "https://github.com/BoiseState/DFA_SMT")
     (synopsis "A set of programs for analysis")
     (description "The set of benchmark programs used for analysis")
     (license #f))))

(define domains
  (package
    (name "predicate-domains")
    (version "0")
    (source (local-file "./analysis/ExperimentData" #:recursive? #t))
    (build-system copy-build-system)
    (arguments
     '(#:install-plan '(("domains" "share/PredicateDomains"))))
    (home-page "https://github.com/BoiseState/DFA_SMT")
    (synopsis "Predefined predicate domains for analysis")
    (description "Set of predefined predicate domains")
    (license #f)))

(define entry-script
  (package
    (name "entry-script")
    (version "0")
    (source #f)
    (build-system trivial-build-system)
    (arguments
     (list
      #:modules '((guix build utils))
      #:builder
      #~(begin
          (use-modules (guix build utils))
          (let ((bash #$(this-package-native-input "bash-minimal"))
                (jdk #$(this-package-native-input "openjdk"))
                (dfa-jar #$(this-package-native-input "dfa-smt-jar"))
                (z3 #$(this-package-native-input "z3"))
                (bin (string-append #$output "/bin/")))
            (mkdir-p bin)
            (with-output-to-file (string-append bin "dfa")
              (lambda _
                (format #t "#!~a/bin/sh
export LD_LIBRARY_PATH=~a/lib/
exec ~a/bin/java -Xms4g \\
     -XX:+UseG1GC \\
     -XX:+UseStringDeduplication \\
     -XX:+UseNUMA \\
     -jar ~a/lib/DFA_SMT.jar \\
     $@" bash z3 jdk dfa-jar)))
            (chmod (string-append bin "dfa") #o755)))))
    (native-inputs
     (list bash-minimal z3-with-java openjdk11 dfa-smt-jar))
    (home-page "https://github.com/BoiseState/DFA_SMT")
    (synopsis "Wrapper script for DFA analysis framework")
    (description "A simple wrapper script for the DFA analysis framework.")
    (license #f)))

(concatenate-manifests
 (list
  %dfa-manifest
  (packages->manifest (list dfa-smt-jar entry-script artifacts domains))))

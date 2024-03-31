(define-module (DFA_SMT guix dfa-artifacts)
  #:use-module (ice-9 regex)
  #:use-module (guix packages)
  #:use-module (guix gexp)
  #:use-module (guix profiles)
  #:use-module (guix build utils)
  #:use-module (guix build-system ant)
  #:use-module (guix build-system copy)
  #:use-module (gnu packages)
  #:use-module (gnu packages bash)
  #:use-module ((gnu packages java) #:prefix java:)
  #:use-module (manifest))

(define artifacts
  (let ((select-java-files (lambda (file _)
                             (not (or (string-match ".+/Neq/.+.java" file)
                                      (string-match ".+/newV.java" file))))))
    (package
     (name "artifacts-for-analysis")
     (version "0")
     (source (local-file "../analysis/artifacts"
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
   (source (local-file "../analysis/ExperimentData" #:recursive? #t))
   (build-system copy-build-system)
   (arguments
    '(#:install-plan '(("domains" "share/PredicateDomains"))))
   (home-page "https://github.com/BoiseState/DFA_SMT")
   (synopsis "Predefined predicate domains for analysis")
   (description "Set of predefined predicate domains")
   (license #f)))

(packages->manifest (list artifacts
                          domains))

(define-module (manifest)
  #:use-module (gnu packages)
  #:use-module (guix)
  #:use-module (guix gexp)
  #:use-module (guix monads)
  #:use-module (guix store)
  #:use-module (guix git-download)
  #:use-module (guix profiles)
  #:use-module (gnu packages base)
  #:use-module (gnu packages bash)
  #:use-module ((gnu packages java) #:prefix java:)
  #:use-module (gnu packages maven)
  #:use-module (gnu packages maths)
  #:use-module (gnu packages machine-learning)
  #:use-module (gnu packages python)
  #:use-module (gnu packages python-science)
  #:use-module (gnu packages python-xyz)
  #:use-module (gnu packages statistics)
  #:export (z3-with-java openjdk11))

(define z3-with-java
  (package
   (inherit z3)
   (name "z3")
   (version "4.8.10")
   (home-page "https://github.com/Z3Prover/z3")
   (source (origin
            (method git-fetch)
            (uri (git-reference (url home-page)
                                (commit (string-append "z3-" version))))
            (file-name (git-file-name name version))
            (sha256
             (base32
              "1w1ym2l0gipvjx322npw7lhclv8rslq58gnj0d9i96masi3gbycf"))))
   (native-inputs
    `(("which" ,which)
      ("python" ,python-wrapper)
      ("jdk" ,java:openjdk11 "jdk")))
   (arguments
    `(#:tests? #f
      #:validate-runpath? #f
      #:phases
      (modify-phases %standard-phases
                     (add-after 'unpack 'set-JDK_HOME
                                (lambda* (#:key inputs #:allow-other-keys)
                                  (setenv "JDK_HOME" (assoc-ref inputs "jdk"))
                                  #t))
                     (replace 'configure
                              (lambda* (#:key inputs outputs #:allow-other-keys)
                                (invoke "python" "scripts/mk_make.py"
                                        "--java"
                                        (string-append "--prefix=" (assoc-ref outputs "out")))))
                     (add-after 'configure 'change-directory
                                (lambda _
                                  (chdir "build")
                                  #t)))))
   (native-search-paths
    (list (search-path-specification
           (variable "LD_LIBRARY_PATH")
           (separator #f)
           (files (list "lib/")))
          (search-path-specification
           (variable "Z3_DIR")
           (separator #f)
           (files (list "")))))))

(define openjdk11
  (package
    (inherit java:openjdk11)
    (native-search-paths
     (list (search-path-specification
            (variable "JAVA_HOME")
            (separator #f)
            (files (list "")))))))

(packages->manifest
 (list glibc
       `(,openjdk11 "jdk")
       maven
       bash
       gnuplot
       python-wrapper
       python-matplotlib
       python-pandas
       python-scikit-learn
       python-scipy
       python-statsmodels
       python-numpy
       z3-with-java))

(define-module (DFA_SMT guix dfa-z3)
  #:use-module (guix profiles)
  #:use-module (gnu packages maths))

(packages->manifest (list z3))

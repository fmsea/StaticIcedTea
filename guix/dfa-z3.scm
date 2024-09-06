(define-module (StaticIcedTea guix dfa-z3)
  #:use-module (guix profiles)
  #:use-module (gnu packages bash)
  #:use-module (gnu packages maths))

(packages->manifest (list bash z3))

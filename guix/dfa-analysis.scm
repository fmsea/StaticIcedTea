(define-module (DFA_SMT guix dfa-analysis)
  #:use-module (guix profiles)
  #:use-module (gnu packages maths)
  #:use-module (gnu packages machine-learning)
  #:use-module (gnu packages parallel)
  #:use-module (gnu packages python)
  #:use-module (gnu packages python-science)
  #:use-module (gnu packages python-xyz)
  #:use-module (gnu packages statistics))

(packages->manifest (list python-wrapper
                          python-matplotlib
                          python-pandas
                          python-scikit-learn
                          python-scipy
                          python-statsmodels
                          python-numpy))

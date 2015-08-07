;; Original work
;; by Joel Boehland http://github.com/jolby/rincanter
;; January 24, 2010

;; Copyright (c) Joel Boehland, 2010. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

;; Modified work by svarcheg https://github.com/svarcheg/rincanter
;; May 5, 2015

(ns rincanter.core
  (:import (org.rosuda.REngine REXP REngineException REXPMismatchException)
           (org.rosuda.REngine.Rserve RConnection))
  (:require [rincanter.convert :refer [to-r from-r]]))

(defn get-r
  "Create a RConnection with args ex: "
  [host port]
  (RConnection. host port))

(defn r-eval-no-catch
  "Eval expression in the R engine. Will not catch any exceptions that
  happen during evaluation"
  [r expression]
  (.parseAndEval r expression))

(defn r-eval-raw
  "Eval expression in the R engine. Just return the raw JRI/R wrapper,
  don't convert to Clojure object"
  [r expression]
      (try
      (.parseAndEval r expression)
      (catch REngineException ex
        (println (format "Caught exception evaluating expression: %s\n: %s" expression ex)))
      (catch REXPMismatchException ex
        (println (format "Caught exception evaluating expression: %s\n: %s" expression ex)))))

(defn r-eval
  "Eval expression in the R engine. Convert the return value from
  JRI/R to Clojure"
  [r expression]
  (from-r (r-eval-raw r expression)))

(defn r-try-parse-eval
  "Eval expression in the R engine, wrapped (on the R side) in
  try/catch. Will catch errors on the R side and convert to Exception
  and throw"
  [r expression]

    (try
      (.assign r ".tmp." expression)
      (let [rexp (.parseAndEval r "try(eval(parse(text=.tmp.)),silent=TRUE)")]
        (if (.inherits rexp "try-error")
          (throw (Exception.
                   (format "Error in R evaluating expression:\n %s.\nR exception: %s"
                           expression (.asString rexp))))
          rexp))
      (catch REngineException ex
        (println (format "Caught exception evaluating expression: %s\n: %s" expression ex)))
      (catch REXPMismatchException ex
        (println (format "Caught exception evaluating expression: %s\n: %s" expression ex)))))

(defmacro with-r-eval-no-catch
  "Evaluate forms that are string using r-eval-no-catch, otherwise, just eval
clojure code normally"
  [r & forms]
  `(do ~@(map #(if (string? %) (list 'r-eval-no-catch r %) %) forms)))

(defmacro with-r-eval-raw
  "Evaluate forms that are string using r-eval-raw, otherwise, just eval
  Clojure code normally"
  [r & forms]
  `(do ~@(map #(if (string? %) (list 'r-eval r %) %) forms)))

(defmacro with-r-eval
  "Evaluate forms that are string using r-eval, otherwise, just eval
  Clojure code normally"
  [r & forms]
  `(do ~@(map #(if (string? %) (list 'r-eval r %) %) forms)))

(defmacro with-r-try-parse-eval
  "Evaluate forms that are string using r-try-parse-eval, otherwise
  just eval Clojure code normally"
  [r & forms]
  `(do ~@(map #(if (string? %) (list 'r-try-parse-eval r %) %) forms)))

(defn r-set!
  "Assign r-name to value within the R engine"
  [r r-name value]
    (try
      (.assign r r-name value)
      (catch REngineException ex
        (println (format "Caught exception assigning R value: %s\n: %s" r-name ex)))))

(defn r-get-raw
  "Retrieve the value with this name in the R engine. Do not convert
  from JRI to Clojure type."
  [r r-name]
  (r-eval-raw r r-name))

(defn r-get
  "Retrieve the value with this name in the R engine"
  [r r-name]
  (r-eval r r-name))

(defn r-inspect
  "Runs str(object) on the R side, capturing console output. Runs
  println on returned Strings"
  [r obj]
  (if (string? obj)
    (dorun (map #'println (r-eval r (format "capture.output(str(%s))" obj))))
    (do
      (r-set! r ".printtmp." (if (instance? REXP obj) obj (to-r obj)))
      (dorun (map #'println (r-eval r "capture.output(str(.printtmp.))"))))))

(defn r-install-CRAN
  "Tries to install the provided package using the optionally provided
repository or the master CRAN repository"
  ([r package]
   (dorun (map #'println
               (r-eval r (format "capture.output(install.packages(\"%s\", repos=\"http://cran.r-project.org\"))" package)))))
  ([r package repo]
   (dorun (map #'println
               (r-eval r (format "capture.output(install.packages(\"%s\", repos=\"%s\"))" package repo))))))

;;
;;Inspection, typechecking and print methods
;;===========================================================================
;;
(defmethod print-method REXP [o w]
  (.write w (str "#=(org.rosuda.REngine.REXP. " (str o) ")")))

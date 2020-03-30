(ns expound.alpha2.test-utils
  (:require [clojure.alpha.spec :as s]
            #_#?(:cljs
                 [clojure.spec.test.alpha :as st]
               ;; FIXME
               ;; orchestra is supposed to work with cljs but
               ;; it isn't working for me right now
                 #_[orchestra-cljs.spec.test :as st]
                 :clj [orchestra.spec.test :as st])
            [expound.alpha :as expound]
            [clojure.test :as ct]
            [com.gfredericks.test.chuck.clojure-test :as chuck]
            [expound.alpha2.util :as util]
            [clojure.test.check.generators :as gen]))

;; test.chuck defines a reporter for the shrunk results, but only for the
;; default reporter (:cljs.test/default). Since karma uses its own reporter,
;; we need to provide an implementation of the report multimethod for
;; the karma reporter and shrunk results

(defmethod ct/report [:jx.reporter.karma/karma ::chuck/shrunk] [m]
  (let [f (get (methods ct/report) [::ct/default ::chuck/shrunk])]
    (f m)))

(defn check-spec-assertions [test-fn]
  (s/check-asserts true)
  (test-fn)
  (s/check-asserts false))

(defn instrument-all [test-fn]
  (binding [s/*explain-out* (expound/custom-printer {:theme :figwheel-theme})]
    ;; TODO: re-enable instrumentation
    #_(st/instrument)
    (test-fn)
    #_(st/unstrument)))

(defn contains-nan? [x]
  (boolean (some util/nan? (tree-seq coll? identity x))))

(def any-printable-wo-nan (gen/such-that (complement contains-nan?)
                                         gen/any-printable))

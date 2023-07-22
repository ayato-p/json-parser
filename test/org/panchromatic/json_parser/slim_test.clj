(ns org.panchromatic.json-parser.slim-test
  (:require [clojure.test :as t]
            [org.panchromatic.json-parser.slim :as json]))

(t/deftest normalize-path-test
  (t/are [expect path] (= expect (json/normalize-path path))
    [] []
    [[1]] [1]
    [[1 3 5]] [[3 5 1]]))

(t/deftest parse-array-test
  (t/are [expect json path] (let [parser (json/make-parser path)]
                              (= expect (parser json)))
    [2] "[1, 2, 3]" [1]
    [2] "[1, 2, 3]" [[1]]
    [1 2] "[1, 2, 3]" [[0 1]]
    [1 3] "[1, 2, 3]" [[0 2]]
    [1 3] "[1, 2, 3]" [[2 0]]))

(t/deftest parse-nested-array-test
  (t/are [expect json path] (= expect (let [parser (json/make-parser path)]
                                        (parser json)))
    [1] "[[1]]" [0 0]
    [[1] [2]] "[[1], [2]]" [[0 1]]
    [[2]] "[[1], [2]]" [1]
    [2] "[[1], [2]]" [1 0]
    [3] "[[1], [2], [3]]" [2 0]
      ;; [1 2] "[[1], [2]]" [[0 1] 0]
    ))

(comment
  (let [p (json/make-parser [1 0])]
    (p "[[1], [2]]"))

  (clojure.walk/macroexpand-all '(json/make-parser [1]))
  ;; => (fn*
  ;;     generated-parser
  ;;     ([src__10587__auto__]
  ;;      (let*
  ;;       [result
  ;;        (clojure.core/atom [])
  ;;        factory
  ;;        (new com.fasterxml.jackson.core.JsonFactory)
  ;;        parser
  ;;        (. factory createJsonParser src__10587__auto__)]
  ;;       (do
  ;;        (org.panchromatic.json-parser.slim/next-token parser)
  ;;        (org.panchromatic.json-parser.slim/next-token parser)
  ;;        (org.panchromatic.json-parser.slim/skip-tokens parser 1)
  ;;        (clojure.core/swap! result clojure.core/conj (org.panchromatic.json-parser.default/parse* parser))
  ;;        (org.panchromatic.json-parser.slim/skip-until-end-array parser))
  ;;       @result)))

  ;; => (fn*
  ;;     generated-parser
  ;;     ([src__10587__auto__]
  ;;      (let*
  ;;       [result
  ;;        (clojure.core/atom [])
  ;;        factory
  ;;        (new com.fasterxml.jackson.core.JsonFactory)
  ;;        parser
  ;;        (. factory createJsonParser src__10587__auto__)]
  ;;       (do
  ;;        (org.panchromatic.json-parser.slim/next-token parser)
  ;;        (org.panchromatic.json-parser.slim/skip-tokens parser 1)
  ;;        (do
  ;;         (org.panchromatic.json-parser.slim/next-token parser)
  ;;         (org.panchromatic.json-parser.slim/next-token parser)
  ;;         (org.panchromatic.json-parser.slim/skip-tokens parser 0)
  ;;         (clojure.core/swap! result clojure.core/conj (org.panchromatic.json-parser.default/parse* parser))
  ;;         (org.panchromatic.json-parser.slim/skip-until-end-array parser))
  ;;        (org.panchromatic.json-parser.slim/skip-until-end-array parser))
  ;;       @result)))


         ;;
  )
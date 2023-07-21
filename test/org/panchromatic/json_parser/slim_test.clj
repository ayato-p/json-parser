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
      ;; [1 2] "[[1], [2]]" [[0 1] 0]
    ))
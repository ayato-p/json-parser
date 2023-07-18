(ns org.panchromatic.json-parser.slim-test
  (:require [clojure.test :as t]
            [org.panchromatic.json-parser.slim :as json]))


(t/deftest parse-array-test
  (t/testing "index access"
    (t/are [expect json path] (= expect (json/parse json path))
      ;; Can not parse bellow
      ;; [] "[1, 2, 3]" [3] 
      [2] "[1, 2, 3]" [1]
      [2] "[1, 2, 3]" [[1]]
      [1 2] "[1, 2, 3]" [[0 1]]
      [1 3] "[1, 2, 3]" [[0 2]]))

  (t/testing "index access with nested array"
    (t/are [expect json path] (= expect (json/parse json path))
      [1] "[[1]]" [0 0])))
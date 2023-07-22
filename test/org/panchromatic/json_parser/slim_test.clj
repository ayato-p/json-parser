(ns org.panchromatic.json-parser.slim-test
  (:require [clojure.test :as t]
            [org.panchromatic.json-parser.slim :as json]))

(t/deftest normalize-path-test
  (t/are [expect path] (= expect (json/normalize-path path))
    [] []
    ;; for arrays
    [[1]] [1]
    [[1 3 5]] [[3 5 1]]
    [[3] [5] [1]] [3 5 1]

    ;; for objects
    [#{"foo"}] [:foo]
    [#{"foo"}] ["foo"]
    [#{"foo" "bar"}] [[:foo "bar"]]
    [#{"foo"} #{"bar"}] [#{:foo} #{"bar"}]

    ;; combination
    [[1] #{"foo"} #{"bar" "baz"} [1 3]] [1 :foo [:bar :baz] [3 1]]))

(t/deftest parse-array-test
  (t/are [expect json path] (let [parser (json/make-parser path)]
                              (= expect (parser json)))
    [2] "[1, 2, 3]" [1]
    [2] "[1, 2, 3]" [[1]]
    [1 2] "[1, 2, 3]" [[0 1]]
    [1 3] "[1, 2, 3]" [[0 2]]
    [1 3] "[1, 2, 3]" [[2 0]]
    [[1]] "[[1]]" [0]
    [[1] [3]] "[[1], [2], [3]]" [[0 2]]))

(t/deftest parse-nested-array-test
  (t/are [expect json path] (= expect (let [parser (json/make-parser path)]
                                        (parser json)))
    [1] "[[1]]" [0 0]
    [1 2] "[[1], [2]]" [[0 1] 0]
    [2] "[[1], [2]]" [1 0]
    [2] "[[1], [2], [3]]" [1 0]
    [1 2] "[[1], [2]]" [[0 1] 0]
    [1 2 5 6] "[[1, 2], [3, 4], [5, 6]]" [[0 2] [0 1]]
    [[1 2] [5 6]] "[[1, 2], [3, 4], [5, 6]]" [[0 2]]))

(t/deftest parse-object-test
  (t/are [expect json path] (= expect (let [parser (json/make-parser path)]
                                        (parser json)))
    [] "{\"bar\": 42}" [:foo]
    [42] "{\"foo\": 42}" [:foo]
    [1] "{\"a\": 1, \"b\": 2}" [#{"a"}]
    [1 2] "{\"a\": 1, \"b\": 2}" [#{"a" "b"}]
    [{"b" 1}] "{\"a\": {\"b\": 1}}" [#{:a}]))

(t/deftest parse-nested-object-test
  (t/are [expect json path] (= expect (let [parser (json/make-parser path)]
                                        (parser json)))
    [42] "{\"a\": {\"b\": 42}}" [#{:a} #{:b}]
    [42 108] "{\"a\": {\"b\": 42, \"c\": 108}}" [#{:a} #{:b :c}]
    [[42]] "{\"a\": {\"b\": {\"c\": [42]}}}" [:a :b :c]
    [42 43] "{\"a\": {\"x\": 42}, \"b\": {\"x\": 43}}" [#{:a :b} :x]
    [42 43] "{\"a\": {\"x\": 42}, \"b\": {\"x\": 43}}" [#{:a :b} :x]
    [{"ans" 42}] "{\"a\": {\"x\": 0}, \"b\": {\"ans\": 42}, \"c\": {\"x\": 0}}" [:b]))

(comment
  (let [p (json/make-parser [1 0])]
    (p "[[1], [2]]"))
  (org.panchromatic.json-parser.default/parse "{\"a\": {\"x\": 0}, \"b\": 1, \"c\": {\"x\": 0}}")
  ((json/make-parser ["b"])
   "{\"a\": {\"x\": 0}, \"b\": 1, \"c\": {\"x\": 0}}")

  (macroexpand '(json/make-parser ["a" "b"]))
         ;;
  )
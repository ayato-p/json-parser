(ns org.panchromatic.json-parser.core-test
  (:require [org.panchromatic.json-parser.core :as json]
            [clojure.test :as t]))

(t/deftest parse-atoms-test
  (t/are [expect json] (= expect (json/parse json))
    42 "42"
    0.42M "0.42"
    "Hello" "\"Hello\""
    true "true"
    false "false"
    nil "null"))

(t/deftest parse-array-test
  (t/are [expect json] (= expect (json/parse json))
    [] "[]"
    [1 2 3] "[1, 2, 3]"
    ["Hello" "World"] "[\"Hello\", \"World\"]"
    [nil true false] "[null, true, false]"))

(t/deftest parse-object-test
  (t/are [expect json] (= expect (json/parse json))
    {"foo" 42} "{\"foo\": 42}"
    {"foo" 1 "bar" 2} "{\"foo\": 1, \"bar\": 2}"))

(t/deftest parse-complect-json-test
  (t/are [expect json] (= expect (json/parse json))
    {"a" {"b" {"c" 42}} "z" 0} "{\"a\": {\"b\": {\"c\": 42}}, \"z\": 0}"
    [{"foo" 1} 2 true nil {"bar" {"baz" 3}}] "[{\"foo\": 1}, 2, true, null, {\"bar\": {\"baz\": 3}}]"))
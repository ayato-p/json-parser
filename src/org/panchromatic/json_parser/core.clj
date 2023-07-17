(ns org.panchromatic.json-parser.core
  (:require [clojure.java.io :as io])
  (:import [com.fasterxml.jackson.core JsonFactory JsonToken JsonParser]))

(declare parse-object
         parse-array)

(defn- parse-atom [^JsonParser parser]
  (condp = (.getCurrentToken parser)
    JsonToken/VALUE_NUMBER_INT (.getLongValue parser)
    JsonToken/VALUE_NUMBER_FLOAT (bigdec (.getText parser))
    JsonToken/VALUE_STRING (.getText parser)
    JsonToken/VALUE_TRUE (.getBooleanValue parser)
    JsonToken/VALUE_FALSE (.getBooleanValue parser)
    JsonToken/VALUE_NULL nil))

(defn- parse-object [^JsonParser parser]
  (letfn [(parse-object-key [parser]
            (.getText parser))
          (parse-object-value [parser]
            (when-let [t (.nextToken parser)]
              (condp = t
                JsonToken/START_ARRAY (parse-array parser)
                JsonToken/START_OBJECT (parse-object parser)
                (parse-atom parser))))]
    (loop [t (.nextToken parser)
           obj {}]
      (condp = t
        JsonToken/END_OBJECT obj
        nil obj
        (let [k (parse-object-key parser)
              v (parse-object-value parser)]
          (recur (.nextToken parser)
                 (assoc obj k v)))))))

(defn- parse-array [^JsonParser parser]
  (loop [t (.nextToken parser)
         ary []]
    (condp = t
      JsonToken/END_ARRAY ary
      JsonToken/START_ARRAY
      (let [ary (conj ary (parse-array parser))]
        (recur (.nextToken parser) ary))
      JsonToken/START_OBJECT
      (let [ary (conj ary (parse-object parser))]
        (recur (.nextToken parser) ary))
      (let [ary (conj ary (parse-atom parser))]
        (recur (.nextToken parser) ary)))))

(defn parse [src]
  (let [^JsonFactory factory (JsonFactory.)
        ^JsonParser parser (.createJsonParser factory src)]
    (when-let [t (.nextToken parser)]
      (condp = t
        JsonToken/START_ARRAY (parse-array parser)
        JsonToken/START_OBJECT (parse-object parser)
        (parse-atom parser)))))

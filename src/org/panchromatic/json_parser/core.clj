(ns org.panchromatic.json-parser.core
  (:import [com.fasterxml.jackson.core JsonFactory JsonParser JsonToken]))

(declare parse*)

(defn- parse-object [^JsonParser parser]
  (loop [t (.nextToken parser)
         obj {}]
    (if (= JsonToken/END_OBJECT t)
      obj
      (let [k (.getText parser)
            v (when-let [_ (.nextToken parser)]
                (parse* parser))]
        (recur (.nextToken parser) (assoc obj k v))))))

(defn- parse-array [^JsonParser parser]
  (loop [t (.nextToken parser)
         ary []]
    (if (= JsonToken/END_ARRAY t)
      ary
      (let [ary (conj ary (parse* parser))]
        (recur (.nextToken parser) ary)))))

(defn- parse* [^JsonParser parser]
  (condp = (.getCurrentToken parser)
    JsonToken/START_ARRAY (parse-array parser)
    JsonToken/START_OBJECT (parse-object parser)
    JsonToken/VALUE_NUMBER_INT (.getLongValue parser)
    JsonToken/VALUE_NUMBER_FLOAT (bigdec (.getText parser))
    JsonToken/VALUE_STRING (.getText parser)
    JsonToken/VALUE_TRUE (.getBooleanValue parser)
    JsonToken/VALUE_FALSE (.getBooleanValue parser)
    JsonToken/VALUE_NULL nil))

(defn parse [src]
  (let [^JsonFactory factory (JsonFactory.)
        ^JsonParser parser (.createJsonParser factory src)]
    (when-let [t (.nextToken parser)]
      (condp = t
        ;; JsonToken/START_ARRAY (parse-array parser)
        ;; JsonToken/START_OBJECT (parse-object parser)
        (parse* parser)))))
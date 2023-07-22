(ns org.panchromatic.json-parser.slim
  (:require [org.panchromatic.json-parser.default :as default])
  (:import [com.fasterxml.jackson.core JsonFactory JsonParser JsonToken]))

(defn next-token [^JsonParser parser]
  ;; (prn (.getCurrentToken parser))
  (.nextToken parser))

(defn skip-elements [^JsonParser parser n]
  (loop [^JsonToken t (.getCurrentToken parser)
         i 0]
    (when (< i n)
      (when (= JsonToken/START_ARRAY t)
        (.skipChildren parser))
      (recur (next-token parser) (inc i)))))

(defn skip-until-end-array [^JsonParser parser]
  (while (let [t (next-token parser)]
           (and t (not= JsonToken/END_ARRAY t)))))

(defn build-array-parser [parser [p & [np :as ps]]]
  (let [p (->> (cons 0 p)
               (partition 2 1)
               (map #(- (second %) (first %))))
        exp (cond
              (sequential? np)
              (interleave (map (fn [x] `(skip-elements ~parser ~x)) p)
                          (repeat (build-array-parser parser ps)))
              :else
              (interleave (map (fn [x] `(skip-elements ~parser ~x)) p)
                          (repeat `(swap! ~'result conj (default/parse* ~parser)))))]
    `(do
       (next-token ~parser)
       ~@exp
       (skip-until-end-array ~parser))))

(defn normalize-path [path]
  (loop [[p & ps] path
         path' []]
    (cond
      (nil? p)
      #_> path'
      (int? p)
      #_> (recur ps (conj path' [p]))
      (or (keyword? p)
          (string? p))
      #_> (recur ps (conj path' #{(name p)}))
      (every? int? p)
      #_> (recur ps (conj path' (-> p sort vec)))
      (every? (some-fn keyword? string?) p)
      #_> (recur ps (conj path' (->> p (map name) set))))))

(defmacro make-parser [path]
  (let [factory (vary-meta 'factory assoc :tag `JsonFactory)
        parser (vary-meta 'parser assoc :tag `JsonParser)
        path' (normalize-path path)]
    `(fn ~'generated-parser [src#]
       (let [~'result (atom [])
             ~factory (JsonFactory.)
             ~parser (.createJsonParser ~factory src#)]
         (next-token ~parser)
         ~(build-array-parser parser path')
         @~'result))))

(comment
  ((make-parser [[0 1]]) "[1, 2, 3]")
  ((make-parser [[1 2]]) "[[1], [0], [3]]")

  (clojure.walk/macroexpand-all '(make-parser [[0 1] 0]))
  ;;
  )

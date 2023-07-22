(ns org.panchromatic.json-parser.slim
  (:require [org.panchromatic.json-parser.default :as default])
  (:import [com.fasterxml.jackson.core JsonFactory JsonParser JsonToken]))

(defn next-token [^JsonParser parser]
  ;; (prn (.getCurrentToken parser) (.getText parser))
  (.nextToken parser))

(defn skip-elements [^JsonParser parser n]
  (loop [^JsonToken t (.getCurrentToken parser)
         i 0]
    (when (< i n)
      (when (or (= JsonToken/START_ARRAY t)
                (= JsonToken/START_OBJECT t))
        (.skipChildren parser))
      (recur (next-token parser) (inc i)))))

(defn skip-field [^JsonParser parser]
  (let [t (next-token parser)]
    (when (or (= JsonToken/START_OBJECT t)
              (= JsonToken/START_ARRAY t))
      (.skipChildren parser))))

(defn skip-until-end-array [^JsonParser parser]
  (while (let [t (next-token parser)]
           (and t (not= JsonToken/END_ARRAY t)))))

(declare build-parser)

(defn build-object-parser [parser [p & ps]]
  (let [exp (build-parser parser ps)]
    `(loop [t# (next-token ~parser)]
       (when-not (= JsonToken/END_OBJECT t#)
         (if (~p (.getText ~parser))
           (do (next-token ~parser)
               ~exp)
           (skip-field ~parser))
         (recur (next-token ~parser))))))

(defn build-array-parser [parser [p & ps]]
  (let [p (->> (cons 0 p)
               (partition 2 1)
               (map #(- (second %) (first %))))
        exp (interleave (map (fn [x] `(skip-elements ~parser ~x)) p)
                        (repeat (build-parser parser ps)))]
    `(do
       (next-token ~parser)
       ~@exp
       (skip-until-end-array ~parser))))

(defn build-parser [^JsonParser parser [p :as path]]
  (cond
    (sequential? p)
    (build-array-parser parser path)
    (set? p)
    (build-object-parser parser path)
    :else
    `(swap! ~'result conj (default/parse* ~parser))))

(let [normalize (fn [p]
                  (cond
                    (int? p) [p]
                    ((some-fn keyword? string?) p) #{(name p)}
                    (every? int? p) (-> p sort vec)
                    (every? (some-fn keyword? string?) p) (->> p (map name) set)))]
  (defn normalize-path [path]
    (loop [[p & ps] path
           path' []]
      (if (nil? p)
        path'
        (recur ps (conj path' (normalize p)))))))

(defmacro make-parser [path]
  (let [factory (vary-meta 'factory assoc :tag `JsonFactory)
        parser (vary-meta 'parser assoc :tag `JsonParser)
        path' (normalize-path path)]
    `(fn ~'generated-parser [src#]
       (let [~'result (atom [])
             ~factory (JsonFactory.)
             ~parser (.createJsonParser ~factory src#)]
         (next-token ~parser)
         ~(build-parser parser path')
         @~'result))))

(comment
  ((make-parser [[0 1]]) "[1, 2, 3]")
  ((make-parser [[1 2]]) "[[1], [0], [3]]")

  (clojure.walk/macroexpand-all '(make-parser [[0 1] 0]))
  ;;
  )

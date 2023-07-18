(ns org.panchromatic.json-parser.slim
  (:import [com.fasterxml.jackson.core JsonFactory JsonParser JsonToken]))

(defn skip-tokens [parser n]
  (dotimes [_ n]
    (.nextToken parser)))

(defn build-parse-array [parser [p & ps]]
  (let [p (->> (cond-> p (not (coll? p)) (-> list set))
               sort
               (cons 0)
               (partition 2 1)
               (map #(- (second %) (first %))))
        exp (->> (for [x p]
                   `((skip-tokens ~parser ~x)
                     ~(if (seq ps)
                        (build-parse-array parser ps)
                        `(swap! ~'result conj (.getIntValue ~parser)))))
                 (apply concat))]
    `(do
       (.nextToken ~parser)
       ~@exp)))

(defmacro make-parser [[p & ps :as path]]
  (let [factory (vary-meta 'factory assoc :tag `JsonFactory)
        parser (vary-meta 'parser assoc :tag `JsonParser)]
    `(fn ~'generated-parser [src#]
       (let [~'result (atom [])
             ~factory (JsonFactory.)
             ~parser (.createJsonParser ~factory src#)]
         (.nextToken ~parser)
         ~(build-parse-array parser path)
         @~'result))))

(comment
  ((make-parser [[0 1]]) "[1, 2, 3]")
  ((make-parser [1 0]) "[1, [0], 3]")
  (macroexpand '(make-parser [1 0]))

  ;;
  )

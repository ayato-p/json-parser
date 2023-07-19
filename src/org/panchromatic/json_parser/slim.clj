(ns org.panchromatic.json-parser.slim
  (:import [com.fasterxml.jackson.core JsonFactory JsonParser JsonToken]))

(defn skip-tokens [parser n]
  (dotimes [_ n]
    (.nextToken parser)))

(defn build-parse-array [parser [p & ps]]
  (let [p (->> (cons 0 p)
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

(defn normalize-path [path]
  (loop [[p & ps] path
         path' []]
    (cond
      (nil? p) path'
      (int? p) (recur ps (conj path' [p]))
      (every? int? p) (recur ps (conj path' (sort p))))))

(defmacro make-parser [path]
  (let [factory (vary-meta 'factory assoc :tag `JsonFactory)
        parser (vary-meta 'parser assoc :tag `JsonParser)
        path' (normalize-path path)]
    `(fn ~'generated-parser [src#]
       (let [~'result (atom [])
             ~factory (JsonFactory.)
             ~parser (.createJsonParser ~factory src#)]
         (.nextToken ~parser)
         ~(build-parse-array parser path')
         @~'result))))

(comment
  ((make-parser [[0 1]]) "[1, 2, 3]")
  ((make-parser [1 0]) "[1, [0], 3]")
  (macroexpand '(make-parser [1 0]))

  ;;
  )

(ns org.panchromatic.json-parser.slim
  (:import [com.fasterxml.jackson.core JsonFactory JsonParser JsonToken]))

(defn skip-tokens [parser n]
  (dotimes [_ n]
    (.nextToken parser)))

(defn build-parse-array [parser [p & path]]
  (let [p (->> (cond-> p (not (coll? p)) (-> list set))
               sort
               (cons 0)
               (partition 2 1)
               (map #(- (second %) (first %))))
        exp (->> (for [x p]
                   `((skip-tokens ~parser ~x)
                     ~(if (seq path)
                        (build-parse-array parser path)
                        `(swap! ~'result conj (.getIntValue ~parser)))))
                 (apply concat))]
    `(do
       (.nextToken ~parser)
       ~@exp)))

(defmacro parse [src path]
  (let [factory (vary-meta 'factory assoc :tag `JsonFactory)
        parser (vary-meta 'parser assoc :tag `JsonParser)]
    `(let [~'result (atom [])
           ~factory (JsonFactory.)
           ~parser (.createJsonParser ~factory ~src)]
       (when-let [t# (.nextToken ~parser)]
         ~(build-parse-array parser path))
       @~'result)))


(comment
  (parse "[1, 2, 3]" [[0 1]])
  (parse "[1, [0], 3]" [1 0])
  (macroexpand '(parse "[1, [0], 3]" [1 0]))
  ;;
  )

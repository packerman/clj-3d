(ns clj-3d.engine.util.error)

(defn throw-if [condition ^String message]
  (when condition
    (throw (RuntimeException. message))))

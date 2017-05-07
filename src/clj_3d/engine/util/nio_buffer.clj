(ns clj-3d.engine.util.nio-buffer
  (:import (com.jogamp.common.nio Buffers)
           (com.jogamp.opengl GL)
           (java.nio FloatBuffer)))

(defrecord NioBuffer [data count byte-size dimension])

(defn is-nio-buffer? [x]
  (instance? NioBuffer x))

(defn- make-float-buffer ^FloatBuffer [data map-to-float]
  (let [num-elements (* (count data) (count (first data)))
        buffer (Buffers/newDirectFloatBuffer num-elements)]
    (doseq [vertex data
            x vertex
            :let [^float f (map-to-float x)]]
      (.put buffer f))
    (.rewind buffer)))

(defn float-buffer [data]
  (let [buffer (make-float-buffer data identity)]
    (map->NioBuffer {:data      buffer
                     :count     (count data)
                     :byte-size (* Float/BYTES (.capacity buffer))
                     :dimension (count (first data))
                     :type      GL/GL_FLOAT})))

(defn int-bits->float-buffer [data]
  (let [buffer (make-float-buffer data #(Float/intBitsToFloat (unchecked-int %)))]
    (map->NioBuffer {:data      buffer
                     :count     (count data)
                     :byte-size (* Float/BYTES (.capacity buffer))
                     :dimension (count (first data))
                     :type      GL/GL_FLOAT})))

(defn int-buffer [data]
  (let [flat-data? (not (sequential? (first data)))
        ^int num-elements (if flat-data? (count data) (* (count data) (count (first data))))
        buffer (Buffers/newDirectIntBuffer num-elements)]
    (if flat-data?
      (doseq [^int x data]
        (.put buffer x))
      (doseq [vertex data
              ^int x vertex]
        (.put buffer x)))
    (.rewind buffer)
    (map->NioBuffer {:data      buffer
                     :count     num-elements
                     :byte-size (* Integer/BYTES (.capacity buffer))
                     :dimension 0
                     :type      GL/GL_UNSIGNED_INT})))

(defn short-buffer [data]
  (let [num-elements (* (count data) (count (first data)))
        buffer (Buffers/newDirectShortBuffer num-elements)]
    (doseq [vertex data
            x vertex
            ^short s (unchecked-short x)]
      (.put buffer s))
    (.rewind buffer)
    (map->NioBuffer {:data      buffer
                     :count     num-elements
                     :byte-size (* Integer/BYTES (.capacity buffer))
                     :type      GL/GL_UNSIGNED_SHORT})))

(defn byte-buffer [data]
  (let [num-elements (* (count data) (count (first data)))
        buffer (Buffers/newDirectByteBuffer num-elements)]
    (doseq [vertex data
            x vertex
            ^byte s (unchecked-byte x)]
      (.put buffer s))
    (.rewind buffer)
    (map->NioBuffer {:data      buffer
                     :count     num-elements
                     :byte-size (* Integer/BYTES (.capacity buffer))
                     :type      GL/GL_UNSIGNED_BYTE})))
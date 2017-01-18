(ns clj-3d.triangle
  (:import (com.jogamp.common.nio Buffers)
           (java.nio FloatBuffer Buffer)
           (com.jogamp.opengl GL4 GL GL3)))

(def vertices [[0 0.5]
               [-0.5 -0.5]
               [0.5 -0.5]])

(def color [0 1 0])

(defn create-nio-buffer [vertices]
  (let [num-elements (* (count vertices) (count (first vertices)))
        buffer (Buffers/newDirectFloatBuffer num-elements)]
    (doseq [vertex vertices
            ^float coord vertex]
      (.put buffer coord))
    (.rewind buffer)))

(defn gl-gen-buffers [^GL gl n]
  (let [buffers (int-array n)]
    (.glGenBuffers gl n buffers 0)
    buffers))

(defn gl-gen-vertex-arrays [^GL3 gl n]
  (let [arrays (int-array n)]
    (.glGenVertexArrays gl n arrays)
    arrays))

(defn create-object [^GL4 gl vertices color]
  (let [^Buffer data (create-nio-buffer vertices)
        size (* Float/BYTES (.capacity data))
        component-count (count (first vertices))
        count (count vertices)
        ^ints vbos (gl-gen-buffers gl 1)
        ^ints vaos (gl-gen-vertex-arrays gl 1)]
    (.glBindBuffer gl GL4/GL_ARRAY_BUFFER (aget vbos 0))
    (.glBufferData gl GL4/GL_ARRAY_BUFFER size data GL4/GL_STATIC_DRAW)

    (.glBindVertexArray gl (aget vaos 0))

    (.glEnableVertexAttribArray gl 0)
    (.glVertexAttribPointer gl 0 component-count GL/GL_FLOAT false 0 0)
    {:vaos vaos
     :vbos vbos
     :count count
     :color color}))

(defn draw-object [^GL4 gl {:keys [^ints vaos count]}]
  (.glBindVertexArray gl (aget vaos 0))
  (.glDrawArrays gl GL/GL_TRIANGLES 0 count)
  (.glBindVertexArray gl 0))

(defn dispose-object [^GL4 gl {:keys [vaos vbos]}]
  (.glDeleteVertexArrays gl 1 vaos 0)
  (.glDeleteBuffers gl 1 vbos 0))

(ns clj-3d.engine.render
  (:require [clj-3d.engine.shader :as shader]
            [clj-3d.engine.color :as color]
            [clj-3d.engine.transform :as transform])
  (:import (com.jogamp.opengl GL4 GL GL3)
           (java.nio Buffer)
           (com.jogamp.common.nio Buffers)))

(defn- create-nio-buffer [vertices]
  (let [num-elements (* (count vertices) (count (first vertices)))
        buffer (Buffers/newDirectFloatBuffer num-elements)]
    (doseq [vertex vertices
            ^float coord vertex]
      (.put buffer coord))
    (.rewind buffer)))

(defn- gl-gen-buffers [^GL gl n]
  (let [buffers (int-array n)]
    (.glGenBuffers gl n buffers 0)
    buffers))

(defn- gl-gen-vertex-arrays [^GL3 gl n]
  (let [arrays (int-array n)]
    (.glGenVertexArrays gl n arrays 0)
    arrays))

(defn- create-object [^GL4 gl node]
  (let [{:keys [vertex-array color]} node
        ^Buffer data (create-nio-buffer vertex-array)
        size (* Float/BYTES (.capacity data))
        component-count (count (first vertex-array))
        count (count vertex-array)
        ^ints vbos (gl-gen-buffers gl 1)
        ^ints vaos (gl-gen-vertex-arrays gl 1)]
    (.glBindBuffer gl GL4/GL_ARRAY_BUFFER (aget vbos 0))
    (.glBufferData gl GL4/GL_ARRAY_BUFFER size data GL4/GL_STATIC_DRAW)

    (.glBindVertexArray gl (aget vaos 0))

    (.glEnableVertexAttribArray gl 0)
    (.glVertexAttribPointer gl 0 component-count GL/GL_FLOAT false 0 0)
    {:vaos         vaos
     :vbos         vbos
     :count        count
     :color        color
     :model-matrix (transform/multiply
                     (:transforms node))
     :program      (shader/build-program gl "flat")}))

(defn- convert-to-rgba [color]
  (if (vector? color)
    color
    (color/to-rgba-float color)))

(defn- draw-object [^GL4 gl object]
  (let [{:keys [program ^ints vaos count color]} object
        [r g b] (convert-to-rgba color)
        color-location (.glGetUniformLocation gl program "color")
        mvp-location (.glGetUniformLocation gl program "mvp")]
    (.glUseProgram gl program)
    (.glUniform4f gl color-location r g b 1)
    (.glUniformMatrix4fv gl mvp-location 1 false (:model-matrix object) 0)
    (.glBindVertexArray gl (aget vaos 0))
    (.glDrawArrays gl GL/GL_TRIANGLES 0 count)
    (.glBindVertexArray gl 0)))

(defn- dispose-object! [^GL4 gl object]
  (let [{:keys [vaos vbos]} object]
    (.glDeleteVertexArrays gl 1 vaos 0)
    (.glDeleteBuffers gl 1 vbos 0)))

(defn create-render-object [gl scene]
  (doall
    (for [node scene]
      (create-object gl node))))

(defn render [gl render-object]
  (doseq [object render-object]
    (draw-object gl object)))

(defn dispose! [gl render-object]
  (doseq [object render-object]
    (dispose-object! gl object)))
(ns clj-3d.engine.render
  (:require [clj-3d.engine.shader :as shader]
            [clj-3d.engine.program :as program]
            [clj-3d.engine.color :as color]
            [clj-3d.engine.transform :as transform]
            [clj-3d.engine.geometry :as geometry]
            [clj-3d.engine.scene.node :as node]
            [clojure.tools.logging :as log]
            [medley.core :refer :all]
            [clojure.pprint :refer [pprint]])
  (:import (com.jogamp.opengl GL4 GL GL3)))

(defn- gl-gen-vertex-arrays [^GL3 gl n]
  (let [arrays (int-array n)]
    (.glGenVertexArrays gl n arrays 0)
    arrays))

(defn- program-name-for-material [material]
  (cond
    (= material :normal) "normal"
    (get-in material [:colors :diffuse]) "diffuse"
    :else "flat"))

(defn- create-object [^GL4 gl programs geometries node]
  (let [{:keys [material]} node
        geometry (or (get geometries (:geometry node))
                     (throw (RuntimeException. (str "No such geometry " (:geometry node)))))
        ^ints vaos (gl-gen-vertex-arrays gl 1)
        program (get programs (program-name-for-material material))]
    (.glBindVertexArray gl (aget vaos 0))
    (geometry/bind-attributes gl geometry program)
    (.glBindVertexArray gl 0)
    {:vaos         vaos
     :geometry     geometry
     :material     material
     :model-matrix (transform/multiply*
                     (:transforms node))
     :program      program}))

(defn when-uniform-exists [program name action]
  (when-let [location (program/uniform-location program name)]
    (action location)))

(defn apply-lights [^GL4 gl program lights]
  (when-let [[light & _] lights]
    (let [[x y z] (:position light)
          [r g b a] (:color light)]
      (when-uniform-exists program "light_position" (fn [location]
                                                      (.glUniform3f gl location x y z)))
      (when-uniform-exists program "light_color" (fn [location]
                                                   (.glUniform4f gl location r g b a))))))

(defn apply-matrices [^GL4 gl program matrices model-matrix]
  (let [model-view-projection-matrix (transform/multiply (:projection-view-matrix matrices) model-matrix)
        model-view-matrix (transform/multiply (:view-matrix matrices) model-matrix)]
    (when-uniform-exists program "model_view_matrix" (fn [location]
                                                       (.glUniformMatrix4fv gl location 1 false
                                                                            model-view-matrix 0)))
    (when-uniform-exists program "normal_matrix" (fn [location]
                                                   (.glUniformMatrix4fv gl location 1 false
                                                                        (-> model-view-matrix
                                                                            transform/inverse
                                                                            transform/transpose) 0)))
    (.glUniformMatrix4fv gl (program/uniform-location program "model_view_projection_matrix")
                         1 false model-view-projection-matrix 0)))

(defn- draw-object [^GL4 gl object matrices lights]
  (let [{:keys [program ^ints vaos ^ints ibos elem-type count material mode geometry]} object]
    (program/use-program gl program)
    (program/apply-material gl program material)

    (apply-lights gl program lights)

    (apply-matrices gl program matrices (:model-matrix object))

    (.glBindVertexArray gl (aget vaos 0))
    (geometry/draw-geometry gl geometry)
    (.glBindVertexArray gl 0)))

(defn- dispose-object! [^GL4 gl object]
  (let [{:keys [vaos geometry]} object]
    (.glDeleteVertexArrays gl 1 vaos 0)
    (geometry/dispose-geometry gl geometry)))

(defn create-render-object [gl scene]
  (let [programs (program/build-programs gl)
        geometries (map-vals
                     (fn [geom] (geometry/create-geometry gl geom))
                     (:geometries scene))]
    {:lights     (scene :lights)
     :geometries geometries
     :objects    (doall
                   (for [node (:nodes scene)]
                     (create-object gl programs geometries
                                    (node/prepare-node node))))}))

(defn render [gl render-object camera]
  (let [matrices {:projection-view-matrix (transform/get-projection-view-matrix camera)
                  :view-matrix            (:view-matrix camera)}]
    (doseq [object (:objects render-object)]
      (draw-object gl object matrices (:lights render-object)))))

(defn dispose! [gl render-object]
  (doseq [object (:objects render-object)]
    (dispose-object! gl object)))

(ns clj-3d.engine.program
  (:require [clj-3d.engine.shader :as shader])
  (:import (com.jogamp.opengl GL2ES2)))

(def programs-to-build #{
                         {:name       "flat"
                          :attributes {
                                       "position" 0
                                       }
                          :uniforms   #{"color" "model_view_projection_matrix"}}
                         {:name       "normal"
                          :attributes {
                                       "position" 0
                                       "normal"   1
                                       }
                          :uniforms   #{"model_view_projection_matrix" "normal_matrix"}}
                         {:name       "diffuse"
                          :attributes {
                                       "position" 0
                                       "normal"   1
                                       }
                          :uniforms   #{"model_view_projection_matrix" "model_view_matrix" "normal_matrix"
                                        "material_ambient" "material_diffuse"
                                        "light_color" "light_position"}}
                         })

(defn build-programs [^GL2ES2 gl]
  (into {}
    (for [program-to-build programs-to-build
          :let [{:keys [name]} program-to-build
                program-id (shader/build-program gl name)]]
      [name {:program-id program-id
             :name name
             :locations {
                         :attributes (:attributes program-to-build)
                         :uniforms (into {}
                                         (for [uniform-name (:uniforms program-to-build)]
                                           [uniform-name (.glGetUniformLocation gl program-id uniform-name)]))}}])))

(defn attribute-location [program nane]
  (get-in program [:locations :attributes name]))

(defn uniform-location [program name]
  (get-in program [:locations :uniforms name]))

(defn use-program [^GL2ES2 gl {:keys [program-id]}]
  (.glUseProgram gl program-id))

(defmulti apply-material (fn [gl program material] (:name program)))

(defmethod apply-material "normal" [_ _ _])

(defmethod apply-material "flat" [^GL2ES2 gl program material]
  (let [[r g b] (get-in material [:colors :ambient])]
    (.glUniform4f gl (get-in program [:locations :uniforms "color"]) r g b 1)))

(defmethod apply-material "diffuse" [^GL2ES2 gl program material]
  (let [[ra ga ba] (get-in material [:colors :ambient])
        [rd gd bd] (get-in material [:colors :diffuse])]
    (.glUniform4f gl (get-in program [:locations :uniforms "material_ambient"]) ra ga ba 1)
    (.glUniform4f gl (get-in program [:locations :uniforms "material_diffuse"]) rd gd bd 1)))

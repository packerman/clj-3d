(ns clj-3d.example.common
  (:require [clj-3d.engine.render :as render]
            [clojure.tools.logging :as log]
            [clj-3d.engine.color :as color]
            [clj-3d.engine.transform :as transform])
  (:import (com.jogamp.opengl GLEventListener GL4 GL GLAutoDrawable GLPipelineFactory)
           (com.jogamp.newt.event KeyListener)))

(defn get-gl4 ^GL4 [^GLAutoDrawable drawable]
  (-> drawable .getGL .getGL4))

(defn init-gl4
  (^GL4 [^GLAutoDrawable drawable {:keys [debug? trace?] :or {debug? false trace? false}}]
   (letfn [(set-debug-context ^GL [^GL gl]
             (if debug?
               (-> gl .getContext
                   (.setGL (GLPipelineFactory/create "com.jogamp.opengl.Debug" nil gl nil)))
               gl))
           (set-trace-context ^GL [^GL gl]
             (if trace?
               (-> gl .getContext
                   (.setGL (GLPipelineFactory/create "com.jogamp.opengl.Trace" nil gl (object-array [System/err]))))
               gl))]
     (let [^GL gl (-> drawable
                      .getGL
                      set-debug-context
                      set-trace-context)]
       (.getGL4 gl))))
  (^GL4 [^GLAutoDrawable drawable] (init-gl4 drawable {})))

(defn set-clear-color! [^GL gl color]
  (let [[r g b a] (color/to-rgba-float color)]
    (.glClearColor gl r g b a)))

(defn make-application-for-scene
  ([scene camera options]
   (let [object (atom nil)
         {:keys [clear-color] :or {clear-color color/dim-gray}} options
         camera (atom camera)]
     (reify
       GLEventListener

       (init [_ drawable]
         (log/info "init")
         (let [gl (init-gl4 drawable {:debug? false :trace? false})]
           (log/info "GL version =" (.glGetString gl GL4/GL_VERSION))
           (log/info "GL renderer =" (.glGetString gl GL4/GL_RENDERER))
           (.glEnable gl GL/GL_DEPTH_TEST)
           (.glDepthFunc gl GL/GL_LESS)
           (.glEnable gl GL/GL_CULL_FACE)
           (.glCullFace gl GL/GL_BACK)
           (.glFrontFace gl GL/GL_CCW)
           (reset! object (render/create-render-object gl scene))))

       (dispose [_ drawable]
         (log/info "dispose")
         (let [gl (get-gl4 drawable)]
           (render/dispose! gl @object))
         (System/exit 0))

       (display [_ drawable]
         (let [gl (get-gl4 drawable)]
           (set-clear-color! gl clear-color)
           (.glClear gl (bit-or GL4/GL_COLOR_BUFFER_BIT
                                GL4/GL_DEPTH_BUFFER_BIT))
           (render/render gl @object @camera)))

       (reshape [_ drawable x y width height]
         (log/info "reshape" x y width height)
         (let [gl (get-gl4 drawable)
               aspect-ratio (float (/ width height))]
           (.glViewport gl x y width height)
           (swap! camera transform/update-projection-matrix aspect-ratio)))

       KeyListener

       (keyPressed [_ e]
         #_(condp = (.getKeyCode e)
             KeyEvent/VK_ESCAPE (when on-exit-hook (on-exit-hook))))

       (keyReleased [_ e]))))
  ([scene camera] (make-application-for-scene scene camera {}))
  ([scene] (make-application-for-scene scene (transform/identity-camera) {})))

(defn grid-vertex-array [m n]
  (letfn [(steps [k a b]
            {:pre [(> k 1)]}
            (let [step (/ (- b a) (dec k))]
              (->>
                (iterate #(+ % step) a)
                (take k))))]
    (let [[x-min y-min x-max y-max] [-0.5 -0.5 0.5 0.5]]
      (mapcat identity
              (-> []
                  (into (map #(list [% y-min] [% y-max]) (steps m x-min x-max)))
                  (into (map #(list [x-min %] [x-max %]) (steps n y-min y-max))))))))

(ns clj-3d.example.common
  (:require [clj-3d.engine.render :as render]
            [clojure.tools.logging :as log]
            [clj-3d.engine.color :as color])
  (:import (com.jogamp.opengl GLEventListener GL4 GL GLAutoDrawable)
           (com.jogamp.newt.event KeyListener)))

(defn get-gl4 ^GL4 [^GLAutoDrawable drawable]
  (-> drawable .getGL .getGL4))

(defn set-clear-color! [^GL gl color]
  (let [[r g b a] (color/to-rgba-float color)]
    (.glClearColor gl r g b a)))

(defn make-application-for-scene
  ([scene options]
   (let [object (atom nil)
         {:keys [clear-color] :or {clear-color color/dim-gray}} options]
     (reify
       GLEventListener

       (init [_ drawable]
         (log/info "init")
         (let [gl (get-gl4 drawable)]
           (log/info "GL version =" (.glGetString gl GL4/GL_VERSION))
           (log/info "GL renderer =" (.glGetString gl GL4/GL_RENDERER))
           (reset! object (render/create-render-object gl scene))))

       (dispose [_ drawable]
         (log/info "dispose")
         (let [gl (get-gl4 drawable)]
           (render/dispose! gl @object))
         (System/exit 0))

       (display [_ drawable]
         (let [gl (get-gl4 drawable)]
           (set-clear-color! gl clear-color)
           (.glClear gl GL4/GL_COLOR_BUFFER_BIT)
           (render/render gl @object)))

       (reshape [_ drawable x y width height]
         (log/info "reshape" x y width height)
         (let [gl (get-gl4 drawable)]
           (.glViewport gl x y width height)))

       KeyListener

       (keyPressed [_ e]
         #_(condp = (.getKeyCode e)
             KeyEvent/VK_ESCAPE (when on-exit-hook (on-exit-hook))))

       (keyReleased [_ e]))))
  ([scene] (make-application-for-scene scene {})))

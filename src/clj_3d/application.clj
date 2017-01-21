(ns clj-3d.application
  (:require [clojure.tools.logging :as log]
            [clj-3d.color :as color])
  (:import (com.jogamp.opengl GLEventListener GLAutoDrawable GL4 GLProfile GLCapabilities GL)
           (com.jogamp.newt.event KeyListener KeyEvent)
           (com.jogamp.newt NewtFactory)
           (com.jogamp.newt.opengl GLWindow)
           (com.jogamp.opengl.util Animator)))

(def default-config {:width 800
                     :height 600
                     :title "clj-3d"
                     :fullscreen false
                     :visible true})

(defprotocol Application
  (render [app gl])
  (resize [app gl x y width height])
  (dispose [app gl]))

(defn get-gl4 ^GL4 [^GLAutoDrawable drawable]
  (-> drawable .getGL .getGL4))

(defn set-clear-color! [^GL gl color]
  (let [[r g b a] (color/to-rgba-float color)]
    (.glClearColor gl r g b a)))

(defn make-application [create-application-fn & {:keys [on-exit-hook]}]
  (let [app (atom nil)]
    (reify
      GLEventListener

      (init [_ drawable]
        (log/info "init")
        (let [gl (get-gl4 drawable)]
          (log/info "GL version =" (.glGetString gl GL4/GL_VERSION))
          (log/info "GL renderer =" (.glGetString gl GL4/GL_RENDERER))
          (reset! app (create-application-fn gl))))

      (dispose [_ drawable]
        (log/info "dispose")
        (let [gl (get-gl4 drawable)]
          (dispose @app gl))
        (System/exit 0))

      (display [_ drawable]
        (let [gl (get-gl4 drawable)]
          (set-clear-color! gl color/deep-sky-blue)
          (.glClear gl GL4/GL_COLOR_BUFFER_BIT)
          (render @app gl)))

      (reshape [_ drawable x y width height]
        (log/info "reshape" x y width height)
        (let [gl (get-gl4 drawable)]
          (.glViewport gl x y width height)
          (resize @app gl x y width height)))

      KeyListener

      (keyPressed [_ e]
        (condp = (.getKeyCode e)
          KeyEvent/VK_ESCAPE (when on-exit-hook (on-exit-hook))))

      (keyReleased [_ e]))))

(defn launch
  ([create-application-fn config]
   (letfn [(make-gl-window []
             (let [screen (-> (NewtFactory/createDisplay nil)
                              (NewtFactory/createScreen 0))
                   gl-capabilities (-> (GLProfile/get GLProfile/GL4)
                                       (GLCapabilities.))]
               (GLWindow/create screen gl-capabilities)))
           (configure-gl-window [^GLWindow gl-window]
             (let [result-config (merge default-config config)]
               (doto gl-window
                 (.setSize (:width result-config) (:height result-config))
                 (.setTitle (:title result-config))
                 (.setFullscreen ^boolean (:fullscreen result-config))
                 (.setVisible (:visible result-config)))))]
     (let [^GLWindow gl-window (-> (make-gl-window) configure-gl-window)
           animator (Animator. gl-window)
           on-exit-hook (fn []
                          (.remove animator gl-window)
                          (.destroy gl-window))
           application (make-application
                         create-application-fn
                         :on-exit-hook on-exit-hook)]
       (doto gl-window
         (.addGLEventListener application)
         (.addKeyListener application))
       (.start animator))))
  ([create-application-fn] (launch create-application-fn {})))

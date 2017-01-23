(ns clj-3d.application
  (:import (com.jogamp.newt NewtFactory)
           (com.jogamp.opengl GLProfile GLCapabilities)
           (com.jogamp.newt.opengl GLWindow)
           (com.jogamp.opengl.util Animator)
           (com.jogamp.newt.event KeyListener KeyEvent)))

(def default-config {:width      800
                     :height     600
                     :title      "clj-3d"
                     :fullscreen false
                     :visible    true})

(defn launch
  ([application config]
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
                 (.setVisible (:visible result-config)))))
           (create-default-key-listener [^GLWindow gl-window ^Animator animator]
             (reify KeyListener
               (keyPressed [this e]
                 (condp = (.getKeyCode e)
                   KeyEvent/VK_ESCAPE (do
                                        (.remove animator gl-window)
                                        (.destroy gl-window))))
               (keyReleased [this e])))]
     (let [^GLWindow gl-window (-> (make-gl-window) configure-gl-window)
           animator (Animator. gl-window)
           key-listener (create-default-key-listener gl-window animator)]
       (doto gl-window
         (.addKeyListener key-listener)
         (.addGLEventListener application)
         (.addKeyListener application))
       (.start animator))))
  ([application] (launch application {})))

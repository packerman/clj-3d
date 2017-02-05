(ns clj-3d.example.cube-normal
  (:gen-class)
  (:require [clj-3d.example.common :as common]
            [clj-3d.engine.color :as color]
            [clj-3d.engine.transform :as transform]
            [clj-3d.application :as application]
            [clj-3d.engine.util.nio-buffer :refer [int-bits->float-buffer]]))

(def scene {:nodes [{:mesh       {:primitive    :lines
                                  :vertex-array (common/grid-vertex-array 11 11)}
                     :color      color/white
                     :transforms [(transform/axis-rotation (Math/toRadians 90) :x)
                                  (transform/scale 10 10 10)]}
                    {:mesh       {
                                  :vertex-arrays {
                                                  "position" (int-bits->float-buffer [[0xC2480000 0xC2480000 0x00000000] [0xC2480000 0x42480000 0x00000000]
                                                                                      [0x42480000 0x42480000 0x00000000] [0x42480000 0xC2480000 0x00000000]
                                                                                      [0xC2480000 0xC2480000 0x42C80000] [0x42480000 0xC2480000 0x42C80000]
                                                                                      [0x42480000 0x42480000 0x42C80000] [0xC2480000 0x42480000 0x42C80000]
                                                                                      [0xC2480000 0xC2480000 0x00000000] [0x42480000 0xC2480000 0x00000000]
                                                                                      [0x42480000 0xC2480000 0x42C80000] [0xC2480000 0xC2480000 0x42C80000]
                                                                                      [0x42480000 0xC2480000 0x00000000] [0x42480000 0x42480000 0x00000000]
                                                                                      [0x42480000 0x42480000 0x42C80000] [0x42480000 0xC2480000 0x42C80000]
                                                                                      [0x42480000 0x42480000 0x00000000] [0xC2480000 0x42480000 0x00000000]
                                                                                      [0xC2480000 0x42480000 0x42C80000] [0x42480000 0x42480000 0x42C80000]
                                                                                      [0xC2480000 0x42480000 0x00000000] [0xC2480000 0xC2480000 0x00000000]
                                                                                      [0xC2480000 0xC2480000 0x42C80000] [0xC2480000 0x42480000 0x42C80000]])
                                                  "normal" (int-bits->float-buffer [[0x00000000 0x00000000 0xBF800000] [0x00000000 0x00000000 0xBF800000]
                                                                                    [0x00000000 0x00000000 0xBF800000] [0x00000000 0x00000000 0xBF800000]
                                                                                    [0x00000000 0x00000000 0x3F800000] [0x00000000 0x00000000 0x3F800000]
                                                                                    [0x00000000 0x00000000 0x3F800000] [0x00000000 0x00000000 0x3F800000]
                                                                                    [0x00000000 0xBF800000 0x00000000] [0x00000000 0xBF800000 0x00000000]
                                                                                    [0x00000000 0xBF800000 0x00000000] [0x80000000 0xBF800000 0x00000000]
                                                                                    [0x3F800000 0x00000000 0x00000000] [0x3F800000 0x00000000 0x00000000]
                                                                                    [0x3F800000 0x00000000 0x00000000] [0x3F800000 0x00000000 0x00000000]
                                                                                    [0x00000000 0x3F800000 0x00000000] [0x00000000 0x3F800000 0x00000000]
                                                                                    [0x00000000 0x3F800000 0x00000000] [0x80000000 0x3F800000 0x00000000]
                                                                                    [0xBF800000 0x00000000 0x00000000] [0xBF800000 0x00000000 0x00000000]
                                                                                    [0xBF800000 0x00000000 0x00000000] [0xBF800000 0x00000000 0x00000000]])
                                                  "texcoord" (int-bits->float-buffer [[0x3F800000 0x00000000] [0x3F800000 0x3F800000] [0x00000000 0x3F800000]
                                                                                      [0x00000000 0x00000000] [0x00000000 0x00000000] [0x3F800000 0x00000000]
                                                                                      [0x3F800000 0x3F800000] [0x00000000 0x3F800000] [0x00000000 0x00000000]
                                                                                      [0x3F800000 0x00000000] [0x3F800000 0x3F800000] [0x00000000 0x3F800000]
                                                                                      [0x00000000 0x00000000] [0x3F800000 0x00000000] [0x3F800000 0x3F800000]
                                                                                      [0x00000000 0x3F800000] [0x00000000 0x00000000] [0x3F800000 0x00000000]
                                                                                      [0x3F800000 0x3F800000] [0x00000000 0x3F800000] [0x00000000 0x00000000]
                                                                                      [0x3F800000 0x00000000] [0x3F800000 0x3F800000] [0x00000000 0x3F800000]])
                                                  }
                                  :index-array   [[0 1 2] [2 3 0] [4 5 6] [6 7 4] [8 9 10]
                                                  [10 11 8] [12 13 14] [14 15 12] [16 17 18]
                                                  [18 19 16] [20 21 22] [22 23 20]]}
                     :material   :normal
                     :transforms [(transform/translation 1.5 0.5 -1.0)
                                  (transform/scale 0.01 0.01 0.01)]
                     }]})

(def camera (transform/perspective-camera {:fovy (Math/toRadians 45.0)
                                           :near 0.1
                                           :far  100.00}
                                          {:eye    [0 2 5]
                                           :center [0 0 0]
                                           :up     [0 1 0]}))

(def app
  (common/make-application-for-scene scene camera))

(defn -main
  [& args]
  (application/launch app))

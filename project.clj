(defproject clj-3d "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :global-vars {*warn-on-reflection* true}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.jogamp.gluegen/gluegen-rt-main "2.3.2"]
                 [org.jogamp.jogl/jogl-all-main "2.3.2"]]
  :main ^:skip-aot clj-3d.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

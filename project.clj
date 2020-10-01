(defproject dn/re-frame-datatable "0.6.2-SNAPSHOT"
  :description "DataTable component for re-frame 0.8.0+"
  :url "https://github.com/debreuck-neirynck/re-frame-datatable"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773"]
                 [reagent "0.10.0"]
                 [re-frame "1.1.1"]]
  :profiles {:dev
             {:dependencies [[midje "1.9.9"]
                             [dn/midje-junit-formatter "0.1.1"]]
              :plugins [[lein-midje "3.2.1"]]}})

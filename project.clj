(defproject svarcheg/rincanter
            "0.0.1-SNAPSHOT"
            :description "Clojure/R integration using rosuda Rserve"
            :dependencies [[org.clojure/clojure "1.6.0"]
                           [incanter "1.5.6"]
                           [org.clojure/tools.logging "0.3.1"]
                           [org.clojure/core.incubator "0.1.3"]
                           [org.rosuda.REngine/REngine "2.1.0"]
                           [org.rosuda.REngine/Rserve "1.8.1"]]
            :autodoc {:name        "rincanter"
                      :description "Clojure/R integration using rosuda JRIEngine"
                      :page-title  "Rincanter API documentation"
                      :copyright   "Eclipse Public License (EPL)"
                      :web-home    "https://github.com/svarcheg/rincanter"}
            :plugins [[lein-kibit "0.1.2"]
                      [lein-ancient "0.6.7"]
                      [lein-bikeshed "0.2.0"]
                      [lein-cloverage "1.0.3"]
                      [codox "0.8.12"]])

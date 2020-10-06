(ns re-frame-datatable.devcards.core
  "This namespace contains devcards and tests"
  (:require [cljsjs.react]
            [cljsjs.react.dom]
            ;; devcards needs cljsjs.react and cljsjs.react.dom to be imported
            ;; separately for shadow-cljs to add shims.
            [devcards.core :refer [start-devcard-ui!]]
            ;; Import all namespaces with cards here to load them.
            [re-frame-datatable.devcards.basic]
            [re-frame-datatable.devcards.custom]
            [re-frame-datatable.devcards.sorting]
            [re-frame-datatable.devcards.pagination]
            [re-frame-datatable.devcards.selection]
            [re-frame.core :as rf]))

(defn ^:dev/after-load clear-cache []
  (rf/clear-subscription-cache!))

(defn ^:export main
  "Start the devcards UI."
  []
  ;; Add a special class to the body to signal we're in devcards mode.
  ;; We want to mostly use the same styles as the app, but might need to make
  ;; some exceptions.
  (js/document.body.classList.add "using-devcards")
  ;; Start the devcards UI.
  (start-devcard-ui!))

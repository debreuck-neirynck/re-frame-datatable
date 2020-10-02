(ns re-frame-datatable.devcards.table
  (:require [reagent.core]
            [devcards.core :as dc :refer [defcard]]
            [re-frame.core :as rf]
            [re-frame-datatable.core :as dt]))

(rf/clear-subscription-cache!)

(rf/reg-sub
 ::test-sub
 (fn [db _]
   [{:name "Test value"}]))

(defcard basic-table
  "Basic table without any special stuff"
  (dc/reagent
   [dt/datatable
    :test-id
    [::test-sub]
    [{::dt/column-key [:name]}]
    {}])
  {})

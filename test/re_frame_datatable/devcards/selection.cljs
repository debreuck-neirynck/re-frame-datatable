(ns re-frame-datatable.devcards.selection
  "Cards for testing selection functionality"
  (:require [reagent.core]
            [devcards.core :as dc :refer [defcard]]
            [re-frame.core :as rf]
            [re-frame-datatable.core :as dt]
            [re-frame-datatable.views :as v]))

(rf/clear-subscription-cache!)

(rf/reg-sub
 ::test-data
 (fn [_ _]
   (->> (range 10)
        (map (fn [v]
               {:name (str "Item " v)
                :value v})))))

(defcard basic-selection
  "Table with basic selection"
  (dc/reagent
   [dt/datatable
    :basic-pagination
    [::test-data]
    [{::dt/column-key [:name]
      ::dt/column-label "Name"}
     {::dt/column-key [:value]
      ::dt/column-label "Value"}]
    {::dt/table-classes ["table"]
     ::dt/selection {::dt/enabled? true}}]))

(ns re-frame-datatable.devcards.sorting
  "Cards for sorting functionality"
  (:require [reagent.core]
            [devcards.core :as dc :refer [defcard]]
            [re-frame.core :as rf]
            [re-frame-datatable.core :as dt]))

(rf/clear-subscription-cache!)

(rf/reg-sub
 ::sortable-data
 (fn [_ _]
   [{:name "First"
     :age 53}
    {:name "Second"
     :age 27}
    {:name "Third"
     :age 40}]))

(defcard basic-sorting
  "Table with single column sorting"
  (dc/reagent
   [dt/datatable
    :single-column-sorting
    [::sortable-data]
    [{::dt/column-key [:name]
      ::dt/column-label "Name"}
     {::dt/column-key [:age]
      ::dt/column-label "Age"
      ::dt/sorting {::dt/enabled? true}}]
    {::dt/table-classes ["table"]}])
  {})

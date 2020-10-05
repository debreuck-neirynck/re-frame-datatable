(ns re-frame-datatable.devcards.pagination
  "Cards for pagination functionality"
  (:require [reagent.core]
            [devcards.core :as dc :refer [defcard]]
            [re-frame.core :as rf]
            [re-frame-datatable.core :as dt]
            [re-frame-datatable.views :as v]))

(rf/clear-subscription-cache!)

(rf/reg-sub
 ::test-data
 (fn [_ _]
   (->> (range 100)
        (map (fn [v]
               {:name (str "Item " v)
                :value v})))))

(defcard basic-pagination
  "Table with basic pagination"
  (dc/reagent
   [:div
    [dt/datatable
     :basic-pagination
     [::test-data]
     [{::dt/column-key [:name]
       ::dt/column-label "Name"}
      {::dt/column-key [:value]
       ::dt/column-label "Value"}]
     {::dt/table-classes ["table"]
      ::dt/pagination {::dt/enabled? true}}]
    [v/default-pagination-controls
     :basic-pagination
     [::test-data]]]))

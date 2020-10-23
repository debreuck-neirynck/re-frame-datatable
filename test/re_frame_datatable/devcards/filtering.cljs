(ns re-frame-datatable.devcards.filtering
  "Cards for filtering functionality"
  (:require [reagent.core]
            [devcards.core :as dc :refer [defcard]]
            [re-frame.core :as rf]
            [re-frame-datatable.core :as dt]
            [re-frame-datatable.views :as v]))

(rf/clear-subscription-cache!)

(rf/reg-sub
  ::test-data
  (fn [_ _]
    (conj
      (->> (range 30)
        (map (fn [v]
               {:name (str (if (even? v) "Item " "item ") v)
                :value v})))
      {:name "Elem 0" :value 0})))

(defcard table-filtering-case-sensitive
  "Table with case-sensitive filtering"
  (dc/reagent
    [:div
     [dt/datatable
      :table-filtering-case-sensitive
      [::test-data]
      [{::dt/column-key [:name]
        ::dt/column-label "Name"}
       {::dt/column-key [:value]
        ::dt/column-label "Value"}]
      {::dt/table-classes ["table"]
       ::dt/pagination {::dt/enabled? true
                        ::dt/per-page 10}}]
     [v/default-pagination-controls
      :table-filtering-case-sensitive
      [::test-data]]
     [v/table-filtering
      :table-filtering-case-sensitive
      [::test-data]]]))

(defcard table-filtering-case-insensitive
  "Table with case-insensitive filtering"
  (dc/reagent
    [:div
     [dt/datatable
      :table-filtering-case-insensitive
      [::test-data]
      [{::dt/column-key [:name]
        ::dt/column-label "Name"}
       {::dt/column-key [:value]
        ::dt/column-label "Value"}]
      {::dt/table-classes ["table"]
       ::dt/pagination {::dt/enabled? true
                        ::dt/per-page 10}
       ::dt/filtering {::dt/case-insensitive-filtering true}}]
     [v/default-pagination-controls
      :table-filtering-case-insensitive
      [::test-data]]
     [v/table-filtering
      :table-filtering-case-insensitive
      [::test-data]]]))

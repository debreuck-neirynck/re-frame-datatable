(ns re-frame-datatable.devcards.basic
  (:require [reagent.core]
            [devcards.core :as dc :refer [defcard]]
            [re-frame.core :as rf]
            [re-frame-datatable.core :as dt]))

(rf/clear-subscription-cache!)

(rf/reg-sub
 ::empty-list
 (fn [db _]
   []))

(defcard empty-table
  "Empty table"
  (dc/reagent
   [dt/datatable
    :empty-table
    [::empty-list]
    [{::dt/column-key [:name]
      ::dt/column-label "name"}]
    {::dt/table-classes ["table"]}])
  {})

(defn- custom-empty-body []
  [:i "This table has no elements"])

(defcard custom-empty-table
  "Empty table with custom component"
  (dc/reagent
   [dt/datatable
    :empty-table
    [::empty-list]
    [{::dt/column-key [:name]}]
    {::dt/empty-tbody-component custom-empty-body}])
  {})

(rf/reg-sub
 ::simple-list
 (fn [db _]
   [{:name "Test value"
     :age 43}]))

(defcard table-without-header
  "Basic table without headers"
  (dc/reagent
   [dt/datatable
    :basic-table
    [::simple-list]
    [{::dt/column-key [:name]}
     {::dt/column-key [:age]}]
    {::dt/table-classes ["table"]
     ::dt/header-enabled? false}])
  {})

(defcard table-with-header
  "Basic table with headers"
  (dc/reagent
   [dt/datatable
    :basic-table
    [::simple-list]
    [{::dt/column-key [:name]
      ::dt/column-label "name"}
     {::dt/column-key [:age]
      ::dt/column-label "age"}]
    {::dt/table-classes ["table"]}])
  {})

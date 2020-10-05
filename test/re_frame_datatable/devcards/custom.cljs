(ns re-frame-datatable.devcards.custom
  "Cards for customization functionality"
  (:require [reagent.core]
            [devcards.core :as dc :refer [defcard]]
            [re-frame.core :as rf]
            [re-frame-datatable.core :as dt]))

(rf/clear-subscription-cache!)

(rf/reg-sub
 ::demo-list
 (fn [db _]
   [{:name "Youngster"
     :age 23}
    {:name "Adult"
     :age 43}
    {:name "Pensioner"
     :age 70}]))

(defn- age-type [a]
  (cond
    (< a 30) :success
    (< a 50) :info
    :else :danger))

(defn- render-age [it _]
  [:span {:class (str "badge badge-" (name (age-type it)))} it])

(defcard custom-cells
  "Table with custom cells"
  (dc/reagent
   [dt/datatable
    :custom-cell
    [::demo-list]
    [{::dt/column-key [:name]
      ::dt/column-label "Name"}
     {::dt/column-key [:age]
      ::dt/column-label "Age"
      ::dt/render-fn render-age}]
    {::dt/table-classes ["table"]}])
  {})

(defn- render-name [n {:keys [age]}]
  [:span {:class (str "badge badge-" (name (age-type age)))} n])

(defcard custom-cells-by-row
  "Table with custom cells by row value"
  (dc/reagent
   [dt/datatable
    :custom-cell
    [::demo-list]
    [{::dt/column-key [:name]
      ::dt/column-label "Name"
      ::dt/render-fn render-name}
     {::dt/column-key [:age]
      ::dt/column-label "Age"}]
    {::dt/table-classes ["table"]}])
  {})

(defn- row-class [{:keys [age]}]
  [(str "text-" (name (age-type age)))])

(defcard custom-rows
  "Table with custom cells by row value"
  (dc/reagent
   [dt/datatable
    :custom-cell
    [::demo-list]
    [{::dt/column-key [:name]
      ::dt/column-label "Name"}
     {::dt/column-key [:age]
      ::dt/column-label "Age"}]
    {::dt/table-classes ["table"]
     ::dt/tr-class-fn row-class}])
  {})

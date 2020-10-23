(ns re-frame-datatable.filtering
  (:require [re-frame.core :as re-frame :refer [trim-v]]
            [re-frame-datatable.db :as db]
            [re-frame-datatable.subs :as subs]))

(defn- set-filter-term [db id term v]
  (db/update-state db id assoc-in [:filtering term] v))

;;; TODO adjust pagination based on filtering
(re-frame/reg-sub
  :re-frame-datatable.core/filtering-state
  (fn [[_ db-id data-sub] _]
    [(re-frame/subscribe data-sub)
     (re-frame/subscribe [::subs/state db-id])
     (re-frame/subscribe [::subs/options db-id])])
  (fn [[items state options] _]
    (let [{:keys [:re-frame-datatable.core/filtering]} options
          fs (:filtering state)]
      {:re-frame-datatable.core/cur-input-filter-val (get fs :cur-input-filter-val "")
       :re-frame-datatable.core/case-insensitive-filtering (get fs :case-insensitive-filtering false)})))

(re-frame/reg-event-db
  :re-frame-datatable.core/set-input-filter-val
  [trim-v]
  (fn [db [db-id filter-term]]
    (set-filter-term db db-id :cur-input-filter-val filter-term)))

(re-frame/reg-event-db
  :re-frame-datatable.core/filter-table
  [trim-v]
  (fn [db [db-id]]
    (set-filter-term db db-id :search-phrase (:cur-input-filter-val (db/filtering db db-id)))))

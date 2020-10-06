(ns re-frame-datatable.pagination
  (:require [re-frame.core :as re-frame :refer [trim-v]]
            [re-frame-datatable.subs :as subs]
            [re-frame-datatable.events :as e]
            [re-frame-datatable.defaults :as d]
            [re-frame-datatable.paths :as p]
            [re-frame-datatable.db :as db]))

(re-frame/reg-sub
 ;; For compatibility, don't use the pagination ns
 :re-frame-datatable.core/pagination-state
 (fn [[_ db-id data-sub] _]
   [(re-frame/subscribe data-sub)
    (re-frame/subscribe [::subs/state db-id])
    (re-frame/subscribe [::subs/options db-id])])
 (fn [[items state options] _]
   (let [{:keys [:re-frame-datatable.core/pagination]} options
         sp (:pagination state)
         pp (or (get sp :per-page)
                (get pagination :re-frame-datatable.core/per-page d/default-per-page))]
     {:re-frame-datatable.core/cur-page (get sp :cur-page 0)
      :re-frame-datatable.core/pages    (->> items
                                             (map-indexed vector)
                                             (map first)
                                             (partition-all
                                              (or pp 1))
                                             (mapv (fn [i] [(first i) (last i)])))
      :re-frame-datatable.core/per-page pp})))

(defn- set-page [db id v]
  (db/update-state db id assoc-in [:pagination :cur-page] v))

(re-frame/reg-event-db
 :re-frame-datatable.core/select-next-page
 [trim-v]
 (fn [db [db-id pagination-state]]
   (let [{:keys [:re-frame-datatable.core/cur-page :re-frame-datatable.core/pages]} pagination-state]
     (set-page db db-id (min (inc cur-page) (dec (count pages)))))))

(re-frame/reg-event-db
 :re-frame-datatable.core/select-prev-page
 [trim-v]
 (fn [db [db-id pagination-state]]
   (let [{:keys [:re-frame-datatable.core/cur-page]} pagination-state]
     (set-page db db-id (max (dec cur-page) 0)))))

(re-frame/reg-event-db
 :re-frame-datatable.core/select-page
 [trim-v]
 (fn [db [db-id _ page-idx]]
   (set-page db db-id page-idx)))

(re-frame/reg-event-db
 :re-frame-datatable.core/set-per-page-value
 [trim-v]
 (fn [db [db-id _ per-page]]
   (-> db
       (db/set-pagination db-id (assoc (db/pagination db db-id) :per-page per-page))
       (set-page db-id 0))))

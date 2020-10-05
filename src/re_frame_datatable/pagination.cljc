(ns re-frame-datatable.pagination
  (:require [re-frame.core :as re-frame :refer [trim-v]]
            [re-frame-datatable.subs :as subs]
            [re-frame-datatable.events :as e]
            [re-frame-datatable.defaults :as d]
            [re-frame-datatable.paths :as p]))

(re-frame/reg-sub
 ;; For compatibility, don't use the pagination ns
 :re-frame-datatable.core/pagination-state
 (fn [[_ db-id data-sub] _]
   [(re-frame/subscribe [::subs/state db-id])
    (re-frame/subscribe data-sub)])
 (fn [[state items] _]
   (let [{:keys [::p/pagination]} state
         pp (get pagination :re-frame-datatable.core/per-page d/default-per-page)]
     {:re-frame-datatable.core/cur-page (or (::p/cur-page pagination) 0)
      :re-frame-datatable.core/pages    (->> items
                                             (map-indexed vector)
                                             (map first)
                                             (partition-all
                                              (or pp 1))
                                             (mapv (fn [i] [(first i) (last i)])))
      :re-frame-datatable.core/per-page (or pp d/default-per-page)})))


(re-frame/reg-event-fx
 :re-frame-datatable.core/select-next-page
 [trim-v]
 (fn [{:keys [db]} [db-id pagination-state]]
   (let [{:keys [:re-frame-datatable.core/cur-page :re-frame-datatable.core/pages]} pagination-state]
     {:db       db
      :dispatch [::e/change-state-value db-id [::p/pagination ::p/cur-page]
                 (min (inc cur-page) (dec (count pages)))]})))


(re-frame/reg-event-fx
 :re-frame-datatable.core/select-prev-page
 [trim-v]
 (fn [{:keys [db]} [db-id pagination-state]]
   (let [{:keys [:re-frame-datatable.core/cur-page]} pagination-state]
     {:db       db
      :dispatch [::e/change-state-value db-id [::p/pagination ::p/cur-page] (max (dec cur-page) 0)]})))


(re-frame/reg-event-fx
 :re-frame-datatable.core/select-page
 [trim-v]
 (fn [{:keys [db]} [db-id pagination-state page-idx]]
   {:db       db
    :dispatch [::e/change-state-value db-id [::p/pagination ::p/cur-page] page-idx]}))


(re-frame/reg-event-fx
 :re-frame-datatable.core/set-per-page-value
 [trim-v]
 (fn [{:keys [db]} [db-id pagination-state per-page]]
   {:db         db
    :dispatch-n [[:re-frame-datatable.core/select-page db-id pagination-state 0]
                 [::e/change-state-value db-id [::p/pagination :re-frame-datatable.core/per-page] per-page]]}))

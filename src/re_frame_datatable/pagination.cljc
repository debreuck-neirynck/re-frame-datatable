(ns re-frame-datatable.pagination
  (:require [re-frame.core :as re-frame :refer [trim-v]]
            [re-frame-datatable.subs :as subs]
            [re-frame-datatable.events :as e]))

(re-frame/reg-sub
 ;; For compatibility, don't use the pagination ns
 :re-frame-datatable.core/pagination-state
 (fn [[_ db-id data-sub]]
   [(re-frame/subscribe [::subs/state db-id])
    (re-frame/subscribe data-sub)])
 (fn [[state items]]
   (let [{:keys [::pagination]} state]
     (merge
      (select-keys pagination [::per-page])
      {::cur-page (or (::cur-page pagination) 0)
       ::pages    (->> items
                       (map-indexed vector)
                       (map first)
                       (partition-all (or (::per-page pagination) 1))
                       (mapv (fn [i] [(first i) (last i)])))}))))


(re-frame/reg-event-fx
 :re-frame-datatable.core/select-next-page
 [trim-v]
 (fn [{:keys [db]} [db-id pagination-state]]
   (let [{:keys [::cur-page ::pages]} pagination-state]
     {:db       db
      :dispatch [::e/change-state-value db-id [::pagination ::cur-page] (min (inc cur-page) (dec (count pages)))]})))


(re-frame/reg-event-fx
 :re-frame-datatable.core/select-prev-page
 [trim-v]
 (fn [{:keys [db]} [db-id pagination-state]]
   (let [{:keys [::cur-page]} pagination-state]
     {:db       db
      :dispatch [::e/change-state-value db-id [::pagination ::cur-page] (max (dec cur-page) 0)]})))


(re-frame/reg-event-fx
 :re-frame-datatable.core/select-page
 [trim-v]
 (fn [{:keys [db]} [db-id pagination-state page-idx]]
   {:db       db
    :dispatch [::e/change-state-value db-id [::pagination ::cur-page] page-idx]}))


(re-frame/reg-event-fx
 :re-frame-datatable.core/set-per-page-value
 [trim-v]
 (fn [{:keys [db]} [db-id pagination-state per-page]]
   {:db         db
    :dispatch-n [[::select-page db-id pagination-state 0]
                 [::e/change-state-value db-id [::pagination ::per-page] per-page]]}))

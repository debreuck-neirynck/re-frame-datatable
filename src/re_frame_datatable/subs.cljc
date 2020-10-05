(ns re-frame-datatable.subs
  (:require [re-frame.core :as re-frame]
            [re-frame-datatable.paths :as p]
            [re-frame-datatable.sorting :as s]
            [re-frame-datatable.defaults :as d]))

(re-frame/reg-sub
 ::state
 (fn [db [_ db-id pagination]]
   (cond-> (get-in db (p/state-db-path db-id))
     pagination (update ::p/pagination merge pagination))))

(re-frame/reg-sub
 ::data
 (fn data-gatherer [[_ db-id data-sub pagination] _]
   [(re-frame/subscribe data-sub)
    (re-frame/subscribe [::state db-id pagination])])

 (fn data-provider [[items state] _]
   (let [sort-data (fn [coll]
                     (let [{:keys [::p/sort-key ::p/sort-comp ::p/sort-fn]
                            :or {sort-fn compare}} (::p/sort state)]
                       (if sort-key
                         (cond->> coll
                           true (sort-by #(get-in (second %) sort-key) sort-fn)
                           (= ::s/sort-desc sort-comp) (reverse))
                         coll)))

         paginate-data (fn [coll]
                         (let [{:keys [:re-frame-datatable.core/per-page
                                       :re-frame-datatable.core/enabled?
                                       ::p/cur-page]
                                :or {per-page d/default-per-page
                                     cur-page 0}} (::p/pagination state)]
                           (if enabled?
                             (->> coll
                                  (drop (* (or cur-page 0) (or per-page 0)))
                                  (take (or per-page 0)))
                             coll)))]

     {:visible-items (->> items
                          (map-indexed vector)
                          (sort-data)
                          (paginate-data))
      :state         state})))

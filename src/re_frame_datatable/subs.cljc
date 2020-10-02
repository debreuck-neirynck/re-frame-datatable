(ns re-frame-datatable.subs
  (:require [re-frame.core :as re-frame]
            [re-frame-datatable.paths :as p]))

(re-frame/reg-sub
 ::state
 (fn [db [_ db-id]]
   (get-in db (p/state-db-path db-id))))

(re-frame/reg-sub
 ::data
 (fn data-gathere [[_ db-id data-sub] _]
   [(re-frame/subscribe data-sub)
    (re-frame/subscribe [::state db-id])])

 (fn data-provider [[items state] _]
   (let [sort-data (fn [coll]
                     (let [{:keys [::sort-key ::sort-comp ::sort-fn]} (::sort state)]
                       (if sort-key
                         (cond->> coll
                           true (sort-by #(get-in (second %) sort-key) sort-fn)
                           (= ::sort-desc sort-comp) (reverse))
                         coll)))

         paginate-data (fn [coll]
                         (let [{:keys [::cur-page ::per-page ::enabled?]} (::pagination state)]
                           (if enabled?
                             (->> coll
                                  (drop (* (or cur-page 0) (or per-page 0)))
                                  (take (or per-page 0)))
                             coll)))]

     {::visible-items (->> items
                           (map-indexed vector)
                           (sort-data)
                           (paginate-data))
      ::state         state})))

(ns re-frame-datatable.pagination-test
  (:require [midje.sweet :refer :all]
            [re-frame-datatable
             [core :as dt]
             [pagination :as pag]
             [paths :as p]
             [db :as db]]
            [re-frame
             [core :as rf]
             [db :refer [app-db]]]))

(rf/clear-subscription-cache!)

(rf/reg-sub
 ::test-items
 (fn [db _]
   (get db ::test-items [])))

(facts "about `pagination-state`"
       (let [id ::test-id
             s (rf/subscribe [::dt/pagination-state id [::test-items]])]

         (fact "exists"
               s => some?)

         (fact "returns pagination info"
               @s => map?
               (keys @s) => (just [::dt/pages ::dt/cur-page ::dt/per-page] :in-any-order))

         (fact "default number of items per page"
               (::dt/per-page @s) => number?)

         (fact "combines settings from options and state"
               (reset! app-db (-> {}
                                  (db/set-options id {::dt/pagination {::dt/per-page 10}})
                                  (db/update-state id assoc-in [:pagination :cur-page] 7))) => map?
               @s => (contains {::dt/per-page 10
                                ::dt/cur-page 7}))

         (fact "takes per-page value from state if provided"
               (reset! app-db (-> {::test-items (->> (range 100)
                                                     (map (partial hash-map :value)))}
                                  (db/set-options id {::dt/pagination {::dt/per-page 10}})
                                  (db/update-state id assoc-in [:pagination :per-page] 20))) => map?
               @s => (contains {::dt/per-page 20
                                ::dt/pages (contains [[0 19]])}))))

(facts "about `select-next-page`"
       (fact "updates pagination state"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [::dt/select-next-page ..id.. {::dt/cur-page 0
                                                              ::dt/pages [[0 10] [11 20]]}])
             (db/pagination @app-db ..id..) => (contains {:cur-page 1}))
       
       (fact "does not skip beyond last page"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [::dt/select-next-page ..id.. {::dt/cur-page 1
                                                              ::dt/pages [[0 10] [11 20]]}])
             (db/pagination @app-db ..id..) => (contains {:cur-page 1})))

(facts "about `select-prev-page`"
       (fact "updates pagination state"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [::dt/select-prev-page ..id.. {::dt/cur-page 1
                                                              ::dt/pages [[0 10] [11 20]]}])
             (db/pagination @app-db ..id..) => (contains {:cur-page 0}))
       
       (fact "does not skip before first page"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [::dt/select-prev-page ..id.. {::dt/cur-page 0
                                                              ::dt/pages [[0 10] [11 20]]}])
             (db/pagination @app-db ..id..) => (contains {:cur-page 0})))

(facts "about `select-page`"
       (fact "updates pagination state"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [::dt/select-page ..id.. {::dt/cur-page 0
                                                         ::dt/pages [[0 10] [11 20]]}
                                1])
             (db/pagination @app-db ..id..) => (contains {:cur-page 1})))

(facts "about `set-per-page-value`"
       (fact "updates per-page in state"
             (reset! app-db (db/set-pagination {} ..id.. {:per-page 10})) => truthy
             (rf/dispatch-sync [::dt/set-per-page-value ..id.. {} 20])
             (db/pagination @app-db ..id..) => (contains {:per-page 20}))

       (fact "resets to first page"
             (reset! app-db (db/set-pagination {} ..id.. {:cur-page 5})) => truthy
             (rf/dispatch-sync [::dt/set-per-page-value ..id.. {} 20])
             (db/pagination @app-db ..id..) => (contains {:cur-page 0})))

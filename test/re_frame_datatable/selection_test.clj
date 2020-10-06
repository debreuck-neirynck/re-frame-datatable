(ns re-frame-datatable.selection-test
  (:require [midje.sweet :refer :all]
            [re-frame
             [core :as rf]
             [db :refer [app-db]]]
            [re-frame-datatable
             [core :as dt]
             [db :as db]
             [selection :as s]]))

(rf/clear-subscription-cache!)

(rf/reg-sub
 ::test-items
 (fn [db _]
   (::test-items db)))

(facts "about `selected-items`"
       (let [id ::test-id
             s (rf/subscribe [::dt/selected-items id [::test-items]])]
         
         (fact "exists"
               s => some?)

         (fact "returns selected items"
               (reset! app-db {::test-items (->> (range 10)
                                                 (map (partial hash-map :value)))}) => truthy
               @s => empty?
               (swap! app-db db/set-selection id #{2 3}) => truthy
               @s => [{:value 2}
                      {:value 3}])))

(facts "about `change-row-selection`"
       (fact "toggles item selection in db"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [::dt/change-row-selection ..id.. 1 true])
             (db/selection @app-db ..id..) => (contains 1)
             (rf/dispatch-sync [::dt/change-row-selection ..id.. 1 false])
             (db/selection @app-db ..id..) =not=> (contains 1)))

(facts "about `change-table-selection`"
       (fact "toggles selection for multiple items in db"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [::dt/change-table-selection ..id.. [1 3] true])
             (db/selection @app-db ..id..) => (contains #{1 3})
             (rf/dispatch-sync [::dt/change-table-selection ..id.. [1] false])
             (db/selection @app-db ..id..) => (just [3])))

(facts "about `unselect-all-rows`"
       (fact "clears selection"
             (reset! app-db (db/set-selection {} ..id.. #{2 3})) => truthy
             (rf/dispatch-sync [::dt/unselect-all-rows ..id..])
             (db/selection @app-db ..id..) => empty?))

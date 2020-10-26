(ns re-frame-datatable.filtering-test
  (:require [midje.sweet :refer :all]
            [re-frame-datatable
             [core :as dt]
             [db :as db]
             [filtering :as filtering]]
            [re-frame
             [core :as rf]
             [db :refer [app-db]]]))

(rf/clear-subscription-cache!)

(rf/reg-sub
  ::test-items
  (fn [db _]
    (get db ::test-items [])))

(facts "about `filtering-state`"
  (let [id ::test-id
        s (rf/subscribe [::dt/filtering-state id [::test-items]])]

    (fact "exists"
      s => some?)

    (fact "returns filtering info"
      @s => map?
      (keys @s) => (just [::dt/cur-input-filter-val] :in-any-order))

    (fact "default current input filter value"
      (::dt/cur-input-filter-val @s) => string?)

    (fact "takes current input filter value from state if provided"
      (reset! app-db (-> {::test-items (->> (range 100)
                                         (map (partial hash-map :value)))}
                       (db/update-state id assoc-in [:filtering :cur-input-filter-val] "Item"))) => map?
      @s => (contains {::dt/cur-input-filter-val "Item"}))))

(facts "about `set-input-filter-val`"
  (fact "updates filtering state"
    (reset! app-db {}) => truthy
    (rf/dispatch-sync [::dt/set-input-filter-val ..id.. "Elem"])
    (db/filtering @app-db ..id..) => (contains {:cur-input-filter-val "Elem"})))

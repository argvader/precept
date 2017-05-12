(ns libx.todomvc.rules-debug
  (:require [clara.rules.accumulators :as acc]
            [clara.rules :as cr]
            [libx.spec.sub :as sub]
            [libx.listeners :as l]
            [libx.schema :as schema]
            [libx.util :refer [insert! insert-unconditional! retract! guid] :as util]
            #?(:clj [libx.tuplerules :refer [def-tuple-session def-tuple-rule deflogical
                                             store-action]])
            #?(:cljs [libx.tuplerules :refer-macros [deflogical store-action def-tuple-session
                                                     def-tuple-rule]])))

(defn trace [& args]
  (apply prn args))

;(store-action :entry/foo-action)

(def-tuple-rule handle-action
  {:group :action}
  [[_ :entry/foo-action ?v]]
  =>
  (insert-unconditional! [[(guid) :foo/id (:foo/id ?v)]
                          [(guid) :foo/name (:foo/name ?v)]]))

(deflogical [?e :entry/new-title "Hello again!"] :- [[?e :entry/title]])

(def-tuple-rule all-facts
  {:group :report}
  [?facts <- (acc/all) :from [:all]]
  =>
  (println "FACTs at the end!" ?facts))

(def-tuple-session app-session
   'libx.todomvc.rules-debug
   :schema schema/libx-schema)

(-> app-session
  (l/replace-listener)
  (util/insert [[1 :entry/title "First"]
                [1 :entry/title "Second"]
                [2 :todo/title "First"]
                [2 :todo/title "Second"]])
  (util/insert-action [(guid) :entry/foo-action {:foo/id 2 :foo/name "bar"}])
  (cr/fire-rules)
  (l/vec-ops))


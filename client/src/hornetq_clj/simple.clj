(ns hornetq-clj.simple
  (:require [hornetq-clj.core-client :as core]))

(def session (atom nil))

(def session-factory (atom nil))

(defn init
  [{:keys [host port user password]
    :or {:host "localhost" :port 5445 :user "guest" :password "guest"}
    :as options}]
  (reset! session-factory (core/netty-session-factory {:host host :port port}))
  (reset! session (core/session @session-factory user password nil))
  (.start @session))

(defn listen
  [queue-name handle-fn]
  {:pre [@session]}
  (core/ensure-queue @session queue-name nil)
  (let [consumer (core/create-consumer @session queue-name nil)
        handler (core/message-handler (fn [hq-msg]
                                        (handle-fn (core/read-message-string hq-msg))))]
    (.setMessageHandler consumer handler)))

(def get-producer
  (memoize
   (fn [queue-name]
     (core/ensure-queue @session queue-name nil)
     (core/create-producer @session queue-name))))

(defn publish
  [queue-name message]
  {:pre [@session]}
  (let [producer (get-producer queue-name)
        hq-msg (core/create-message @session false)]
    (core/write-message-string hq-msg (prn-str message))
    (core/send-message producer hq-msg queue-name)))


(comment
  (init {:host "localhost" :port 5445})
  (listen "greeting" prn)
  (publish "greeting" "hello,zzwu!"))

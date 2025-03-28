// Copyright 2015-2018 The NATS Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.nats.client.impl;

import io.nats.client.Dispatcher;
import io.nats.client.MessageHandler;
import io.nats.client.Subscription;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.nats.client.support.Validator.*;

class NatsDispatcher extends NatsConsumer implements Dispatcher, Runnable {

    protected final MessageQueue incoming;
    protected final MessageHandler defaultHandler;

    protected Future<Boolean> thread;
    protected final AtomicBoolean running;
    protected final AtomicBoolean started;

    protected String id;

    // We will use the subject as the key for subscriptions that use the
    // default handler.
    protected final Map<String, NatsSubscription> subscriptionsUsingDefaultHandler;

    // We will use the SID as the key. Since  these subscriptions provide
    // their own handlers, we allow duplicates. There is a subtle but very
    // important difference here.
    protected final Map<String, NatsSubscription> subscriptionsWithHandlers;

    // We use the SID as the key here.
    protected final Map<String, MessageHandler> subscriptionHandlers;

    protected final Duration waitForMessage;

    NatsDispatcher(NatsConnection conn, MessageHandler handler) {
        super(conn);
        this.defaultHandler = handler;
        this.incoming = new MessageQueue(true, conn.getOptions().getRequestCleanupInterval());
        this.subscriptionsUsingDefaultHandler = new ConcurrentHashMap<>();
        this.subscriptionsWithHandlers = new ConcurrentHashMap<>();
        this.subscriptionHandlers = new ConcurrentHashMap<>();
        this.running = new AtomicBoolean(false);
        this.started = new AtomicBoolean(false);
        this.waitForMessage = Duration.ofMinutes(5); // This can be long since we aren't doing anything
    }

    @Override
    public void start(String id) {
        internalStart(id, true);
    }

    protected void internalStart(String id, boolean threaded) {
        if (!started.get()) {
            this.id = id;
            this.running.set(true);
            this.started.set(true);
            if (threaded) {
                thread = connection.getExecutor().submit(this, Boolean.TRUE);
            }
        }
    }

    boolean breakRunLoop() {
        return this.incoming.isDrained();
    }

    public void run() {
        try {
            while (running.get() && !Thread.interrupted()) {
                NatsMessage msg = this.incoming.pop(this.waitForMessage);
                if (msg != null) {
                    NatsSubscription sub = msg.getNatsSubscription();
                    if (sub != null && sub.isActive()) {
                        MessageHandler handler = subscriptionHandlers.get(sub.getSID());
                        if (handler == null) {
                            handler = defaultHandler;
                        }
                        // A dispatcher can have a null defaultHandler. You can't subscribe without a handler,
                        // but messages might come in while the dispatcher is being closed or after unsubscribe
                        // and the [non-default] handler has already been removed from subscriptionHandlers
                        if (handler != null) {
                            sub.incrementDeliveredCount();
                            this.incrementDeliveredCount();

                            try {
                                handler.onMessage(msg);
                            } catch (Exception exp) {
                                connection.processException(exp);
                            } catch (Error err) {
                                connection.processException(new Exception(err));
                            }

                            if (sub.reachedUnsubLimit()) {
                                connection.invalidate(sub);
                            }
                        }
                    }
                }

                if (breakRunLoop()) {
                    return;
                }
            }
        }
        catch (InterruptedException exp) {
            if (this.running.get()){
                this.connection.processException(exp);
            } //otherwise we did it
            Thread.currentThread().interrupt();
        }
        finally {
            this.running.set(false);
            this.thread = null;
        }
    }

    // Template method (default: synchronous)
    protected void handleMessage(NatsSubscription sub, NatsMessage msg, MessageHandler handler) throws InterruptedException {
        handler.onMessage(msg);
    }

    void stop(boolean unsubscribeAll) {
        this.running.set(false);
        this.incoming.pause();

        if (this.thread != null) {
            try {
                if (!this.thread.isCancelled()) {
                    this.thread.cancel(true);
                }
            } catch (Exception exp) {
                // let it go
            }
        }

        if (unsubscribeAll) {
            this.subscriptionsUsingDefaultHandler.forEach((subj, sub) -> {
                this.connection.unsubscribe(sub, -1);
            });
            this.subscriptionsWithHandlers.forEach((sid, sub) -> {
                this.connection.unsubscribe(sub, -1);
            });
        }

        this.subscriptionsUsingDefaultHandler.clear();
        this.subscriptionsWithHandlers.clear();
        this.subscriptionHandlers.clear();
    }

    public boolean isActive() {
        return this.running.get();
    }

    String getId() {
        return id;
    }

    MessageQueue getMessageQueue() {
        return incoming;
    }

    Map<String, MessageHandler> getSubscriptionHandlers() {
        return subscriptionHandlers;
    }

    void resendSubscriptions() {
        this.subscriptionsUsingDefaultHandler.forEach((id, sub)->{
            this.connection.sendSubscriptionMessage(sub.getSID(), sub.getSubject(), sub.getQueueName(), true);
        });
        this.subscriptionsWithHandlers.forEach((sid, sub)->{
            this.connection.sendSubscriptionMessage(sub.getSID(), sub.getSubject(), sub.getQueueName(), true);
        });
    }

    // Called by the connection when a subscription is removed.
    // We will first attempt to remove from subscriptionsWithHandlers
    // using the sub's SID, and if we don't find it there, we'll check
    // the subscriptionsUsingDefaultHandler Map and verify the SID
    // matches before removing. By verifying the SID in all cases we can
    // be certain we're removing the correct Subscription.
    void remove(NatsSubscription sub) {
        if (this.subscriptionsWithHandlers.remove(sub.getSID()) != null) {
            this.subscriptionHandlers.remove(sub.getSID());
        } else {
            NatsSubscription s = this.subscriptionsUsingDefaultHandler.get(sub.getSubject());
            if (s.getSID().equals(sub.getSID())) {
                this.subscriptionsUsingDefaultHandler.remove(sub.getSubject());
            }
        }
    }

    public Dispatcher subscribe(String subject) {
        validateSubject(subject, true);
        this.subscribeImplCore(subject, null, null);
        return this;
    }

    NatsSubscription subscribeReturningSubscription(String subject) {
        validateSubject(subject, true);
        return this.subscribeImplCore(subject, null, null);
    }

    public Subscription subscribe(String subject, MessageHandler handler) {
        validateSubject(subject, true);
        required(handler, "Handler");
        return this.subscribeImplCore(subject, null, handler);
    }

    public Dispatcher subscribe(String subject, String queueName) {
        validateSubject(subject, true);
        validateQueueName(queueName, true);
        this.subscribeImplCore(subject, queueName, null);
        return this;
    }

    public Subscription subscribe(String subject, String queueName,  MessageHandler handler) {
        validateSubject(subject, true);
        validateQueueName(queueName, true);
        if (handler == null) {
            throw new IllegalArgumentException("MessageHandler is required in subscribe");
        }
        return this.subscribeImplCore(subject, queueName, handler);
    }

    // Assumes the subj/queuename checks are done, does check for closed status
    NatsSubscription subscribeImplCore(String subject, String queueName, MessageHandler handler) {
        checkBeforeSubImpl();

        // If the handler is null, then we use the default handler, which will not allow
        // duplicate subscriptions to exist.
        if (handler == null) {
            NatsSubscription sub = this.subscriptionsUsingDefaultHandler.get(subject);

            if (sub == null) {
                sub = connection.createSubscription(subject, queueName, this, null);
                NatsSubscription wonTheRace = this.subscriptionsUsingDefaultHandler.putIfAbsent(subject, sub);
                if (wonTheRace != null) {
                    this.connection.unsubscribe(sub, -1); // Could happen on very bad timing
                }
            }

            return sub;
        }

        return _subscribeImplHandlerProvided(subject, queueName, handler, null);
    }

    NatsSubscription subscribeImplJetStream(String subject, String queueName, MessageHandler handler, NatsSubscriptionFactory nsf) {
        checkBeforeSubImpl();
        return _subscribeImplHandlerProvided(subject, queueName, handler, nsf);
    }

    private NatsSubscription _subscribeImplHandlerProvided(String subject, String queueName, MessageHandler handler, NatsSubscriptionFactory nsf) {
        NatsSubscription sub = connection.createSubscription(subject, queueName, this, nsf);
        this.subscriptionsWithHandlers.put(sub.getSID(), sub);
        this.subscriptionHandlers.put(sub.getSID(), handler);
        return sub;
    }

    String reSubscribe(NatsSubscription sub, String subject, String queueName, MessageHandler handler) {
        String sid = connection.reSubscribe(sub, subject, queueName);
        this.subscriptionsWithHandlers.put(sid, sub);
        this.subscriptionHandlers.put(sid, handler);
        return sid;
    }

    private void checkBeforeSubImpl() {
        if (!running.get()) {
            throw new IllegalStateException("Dispatcher is closed");
        }

        if (isDraining()) {
            throw new IllegalStateException("Dispatcher is draining");
        }
    }

    public Dispatcher unsubscribe(String subject) {
        return this.unsubscribe(subject, -1);
    }

    public Dispatcher unsubscribe(Subscription subscription) {
        return this.unsubscribe(subscription, -1);
    }

    public Dispatcher unsubscribe(String subject, int after) {
        if (!this.running.get()) {
            throw new IllegalStateException("Dispatcher is closed");
        }

        if (isDraining()) { // No op while draining
            return this;
        }

        if (subject == null || subject.length() == 0) {
            throw new IllegalArgumentException("Subject is required in unsubscribe");
        }

        NatsSubscription sub = this.subscriptionsUsingDefaultHandler.get(subject);

        if (sub != null) {
            this.connection.unsubscribe(sub, after); // Connection will tell us when to remove from the map
        }

        return this;
    }

    public Dispatcher unsubscribe(Subscription subscription, int after) {
        if (!this.running.get()) {
            throw new IllegalStateException("Dispatcher is closed");
        }

        if (isDraining()) { // No op while draining
            return this;
        }

        if (subscription.getDispatcher() != this) {
            throw new IllegalStateException("Subscription is not managed by this Dispatcher");
        }

        // We can probably optimize this path by adding getSID() to the Subscription interface.
        if (!(subscription instanceof NatsSubscription)) {
            throw new IllegalArgumentException("This Subscription implementation is not known by Dispatcher");
        }
        
        NatsSubscription ns = ((NatsSubscription) subscription);
        // Grab the NatsSubscription to verify we weren't given a different manager's subscription.
        NatsSubscription sub = this.subscriptionsWithHandlers.get(ns.getSID());

        if (sub != null) {
            this.connection.unsubscribe(sub, after); // Connection will tell us when to remove from the map
        }

        return this;
    }

    void sendUnsubForDrain() {
        this.subscriptionsUsingDefaultHandler.forEach((id, sub)->{
            this.connection.sendUnsub(sub, -1);
        });
        this.subscriptionsWithHandlers.forEach((sid, sub)->{
            this.connection.sendUnsub(sub, -1);
        });
    }

    void cleanUpAfterDrain() {
        this.connection.cleanupDispatcher(this);
    }

    public boolean isDrained() {
        return !isActive() && super.isDrained();
    }
}

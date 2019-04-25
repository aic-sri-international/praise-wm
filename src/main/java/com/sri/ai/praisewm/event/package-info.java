/**
 * Events are sent between services using Google's EventBus.
 *
 * <p>Events allow services to communicate while remaining loosely coupled.
 *
 * <p>Multiple services can subscribe to the same event.
 *
 * <p>All events are currently synchronous.
 *
 * <p>The main consumer of events is the {@link com.sri.ai.praisewm.service.NotificationService}
 * that sends {@link com.sri.ai.praisewm.event.notification.NotificationEvent}s to UI clients.
 */
package com.sri.ai.praisewm.event;

/**
 * The Service classes.
 *
 * <p>Each service implements the {@link com.sri.ai.praisewm.service.Service} interface and its
 * class is instantiated by the {@link com.sri.ai.praisewm.service.ServiceRegistry}. Services are
 * loosely coupled and rarely, need to communicate to one another. When services need to
 * communicate, they will typically use the Google EventBus. If more direct access is required, they
 * can access other services using the {@link
 * com.sri.ai.praisewm.service.ServiceManager#getService(Class)} method.
 *
 * <p>During application initialization, the {@link com.sri.ai.praisewm.service.ServiceManager} will
 * iterate through the {@link com.sri.ai.praisewm.service.ServiceRegistry} calling each service's
 * {@link com.sri.ai.praisewm.service.Service#start(com.sri.ai.praisewm.service.ServiceManager)}
 * method. At shutdown, the ServiceManager will call each service's {@link
 * com.sri.ai.praisewm.service.Service#stop()} method.
 *
 * <p>Most services are thin wrappers that create an instance of their corresponding {@link
 * com.sri.ai.praisewm.web.rest.route} class. The following core services are the exceptions:
 *
 * <ul>
 *   <li>{@link com.sri.ai.praisewm.service.SecurityService}
 *   <li>{@link com.sri.ai.praisewm.service.NotificationService}
 *   <li>{@link com.sri.ai.praisewm.service.SystemStatusService}
 *   <li>{@link com.sri.ai.praisewm.service.PraiseService}
 * </ul>
 */
package com.sri.ai.praisewm.service;

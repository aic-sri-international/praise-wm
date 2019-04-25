/**
 * The Web Layer.
 *
 * <p>The web layer includes
 *
 * <ul>
 *   <li>The {@link com.sri.ai.praisewm.web.WebController} for REST or WebSocket
 *   <li>The {@link com.sri.ai.praisewm.web.rest rest} layer
 *   <li>The {@link com.sri.ai.praisewm.web.ws.NotificationSessionManager} that uses WebSocket to
 *       push messages to UI clients.
 *   <li>The {@link com.sri.ai.praisewm.web.error error} layer for exceptions
 * </ul>
 *
 * <p>Each routes class is created by a corresponding {@link com.sri.ai.praisewm.service Service}
 * class.
 *
 * <p>Each routes class must implement {@link com.sri.ai.praisewm.web.rest.route.AbstractRouteGroup}
 * class.
 *
 * <p>Each routes implementation class ends with {@code Routes} and is located in the {@link
 * com.sri.ai.praisewm.web.rest.route route} package.
 *
 * <p>When constructing a routes instance, a {@link com.sri.ai.praisewm.web.rest.util.RouteScope}
 * must be specified for the routes. Even though authorization is not currently enforced, the
 * mechanism remains in-place to mitigate the amount of work required if it needs to be activated in
 * the future.
 *
 * <p>All routes should use the {@link com.sri.ai.praisewm.web.rest.util.SparkUtil} methods for each
 * of their REST methods.
 *
 * <p>Cross-origin resource sharing (CORS) is supported by using the {@link
 * com.sri.ai.praisewm.web.rest.CorsFilter}.
 */
package com.sri.ai.praisewm.web;

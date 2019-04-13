/**
 * Custom runtime exceptions.
 *
 * <p>Exception handing in this application uses the practice of only throwing a checked exception
 * if it is known that the immediate calling code can actually do something about the condition that
 * caused the exception.
 *
 * <p>All exceptions are caught and logged by the {@link com.sri.ai.praisewm.web.WebController}. The
 * WebController knows about the REST call that initiated the processing and will log that
 * information along with the error message. So, it is preferable not to log errors that occur in
 * code paths initiated by REST calls, but rather to throw an exception with a meaningful message
 * and let the exception get logged by the WebController. The WebController also passes exception
 * messages back to the browser client that initiated the REST call.
 *
 * <p>In the vast majority of cases, when encountering an error condition, the code should throw a
 * RuntimeException type with a meaningful error message. Code at a higher level up the call stack
 * should typically only contain try/catch code for a RuntimeException if it can add meaningful
 * information for the caller, and include it in a new RuntimeException, with the other exception as
 * the cause.
 *
 * <p>If it is known that the exception message is complete, a {@link
 * com.sri.ai.praisewm.web.error.ProcessingException} should be thrown, which should typically not
 * be processed by code other than the WebController. If it is felt that logging a stack trace will
 * not provide any real value to help analyse the cause of the error, always throw a
 * ProcessingException but first call {@link
 * com.sri.ai.praisewm.web.error.ProcessingException#setLogStackTrace(boolean)} passing {@code
 * false} as the parameter so that the stack trace does not needlessly add to the log file.
 * ProcessingException also supports a message to display to the browser client, and a separate,
 * more detailed technical message, that will be logged by the client, but not display in the UI.
 */
package com.sri.ai.praisewm.web.error;

package com.example.grocery.testsupport;

import jakarta.enterprise.util.AnnotationLiteral;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * CDI annotation literal for the {@link RestClient} qualifier.
 *
 * REST client beans (interfaces annotated with @RegisterRestClient) are only injectable
 * via the @RestClient qualifier - plain type-based lookups won't resolve the actual proxy.
 * QuarkusMock.installMockForType needs this literal passed explicitly, otherwise it resolves
 * the wrong (or no) bean and fails with "not a client proxy, only normal scoped beans may be
 * mocked".
 *
 * Written in Java because Kotlin does not allow a class to implement a Java annotation
 * interface directly (required here alongside extending AnnotationLiteral).
 */
public class RestClientLiteral extends AnnotationLiteral<RestClient> implements RestClient {
}


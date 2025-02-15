/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the "Elastic License
 * 2.0", the "GNU Affero General Public License v3.0 only", and the "Server Side
 * Public License v 1"; you may not use this file except in compliance with, at
 * your election, the "Elastic License 2.0", the "GNU Affero General Public
 * License v3.0 only", or the "Server Side Public License, v 1".
 */
package org.elasticsearch.test.rest.yaml.section;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.core.Tuple;
import org.elasticsearch.xcontent.XContentLocation;
import org.elasticsearch.xcontent.XContentParser;

import java.io.IOException;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * Represents a gt assert section:
 * <p>
 * - gt:    { fields._ttl: 0}
 */
public class GreaterThanAssertion extends Assertion {
    public static GreaterThanAssertion parse(XContentParser parser) throws IOException {
        XContentLocation location = parser.getTokenLocation();
        Tuple<String, Object> stringObjectTuple = ParserUtils.parseTuple(parser);
        if ((stringObjectTuple.v2() instanceof Comparable) == false) {
            throw new IllegalArgumentException(
                "gt section can only be used with objects that support natural ordering, found "
                    + stringObjectTuple.v2().getClass().getSimpleName()
            );
        }
        return new GreaterThanAssertion(location, stringObjectTuple.v1(), stringObjectTuple.v2());
    }

    private static final Logger logger = LogManager.getLogger(GreaterThanAssertion.class);

    public GreaterThanAssertion(XContentLocation location, String field, Object expectedValue) {
        super(location, field, expectedValue);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void doAssert(Object actualValue, Object expectedValue) {
        logger.trace("assert that [{}] is greater than [{}] (field: [{}])", actualValue, expectedValue, getField());
        assertThat(
            "value of [" + getField() + "] is not comparable (got [" + safeClass(actualValue) + "])",
            actualValue,
            instanceOf(Comparable.class)
        );
        assertThat(
            "expected value of [" + getField() + "] is not comparable (got [" + expectedValue.getClass() + "])",
            expectedValue,
            instanceOf(Comparable.class)
        );
        if (actualValue instanceof Long && expectedValue instanceof Integer) {
            expectedValue = (long) (int) expectedValue;
        }
        try {
            assertThat(errorMessage(), (Comparable) actualValue, greaterThan((Comparable) expectedValue));
        } catch (ClassCastException e) {
            throw new AssertionError("cast error while checking (" + errorMessage() + "): " + e, e);
        }
    }

    private String errorMessage() {
        return "field [" + getField() + "] is not greater than [" + getExpectedValue() + "]";
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axiom.ts.soap.header;

import java.util.Iterator;

import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.ts.soap.SOAPSampleSet;
import org.apache.axiom.ts.soap.SOAPSpec;
import org.apache.axiom.ts.soap.SampleBasedSOAPTestCase;

public class TestExamineAllHeaderBlocksWithParser extends SampleBasedSOAPTestCase {
    public TestExamineAllHeaderBlocksWithParser(OMMetaFactory metaFactory, SOAPSpec spec) {
        super(metaFactory, spec, SOAPSampleSet.WSA);
    }

    protected void runTest(SOAPEnvelope envelope) throws Throwable {
        Iterator iterator = envelope.getHeader().examineAllHeaderBlocks();
        iterator.hasNext();
        SOAPHeaderBlock headerBlock = (SOAPHeaderBlock)iterator.next();
        assertEquals("MessageID", headerBlock.getLocalName());
        assertTrue(iterator.hasNext());
        headerBlock = (SOAPHeaderBlock)iterator.next();
        assertEquals("ReplyTo", headerBlock.getLocalName());
        assertTrue(iterator.hasNext());
        headerBlock = (SOAPHeaderBlock)iterator.next();
        assertEquals("To", headerBlock.getLocalName());
        assertTrue(iterator.hasNext());
        headerBlock = (SOAPHeaderBlock)iterator.next();
        assertEquals("Action", headerBlock.getLocalName());
        assertFalse(iterator.hasNext());
    }
}

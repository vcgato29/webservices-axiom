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
package org.apache.axiom.mime;

import junit.framework.TestCase;

public class MediaTypeTest extends TestCase {
    public void testBasic() {
        MediaType mt = new MediaType("text", "xml");
        assertEquals("text", mt.getPrimaryType());
        assertEquals("xml", mt.getSubType());
    }
    
    public void testEquals() {
        MediaType mt1 = new MediaType("text", "xml");
        MediaType mt2 = new MediaType("text", "xml");
        MediaType mt3 = new MediaType("text", "plain");
        MediaType mt4 = new MediaType("application", "xml");
        assertTrue(mt1.equals(mt2));
        assertFalse(mt1.equals(mt3));
        assertFalse(mt1.equals(mt4));
        assertFalse(mt3.equals(mt4));
    }
    
    public void testEqualsIgnoresCase() {
        MediaType mt1 = new MediaType("text", "xml");
        MediaType mt2 = new MediaType("TEXT", "XML");
        assertTrue(mt1.equals(mt2));
    }
    
    public void testHashCode() {
        MediaType mt1 = new MediaType("text", "xml");
        MediaType mt2 = new MediaType("text", "xml");
        MediaType mt3 = new MediaType("text", "plain");
        MediaType mt4 = new MediaType("application", "xml");
        assertTrue(mt1.hashCode() == mt2.hashCode());
        assertFalse(mt1.hashCode() == mt3.hashCode());
        assertFalse(mt1.hashCode() == mt4.hashCode());
        assertFalse(mt3.hashCode() == mt4.hashCode());
    }

    public void testHashCodeIgnoresCase() {
        MediaType mt1 = new MediaType("text", "xml");
        MediaType mt2 = new MediaType("TEXT", "XML");
        assertTrue(mt1.hashCode() == mt2.hashCode());
    }
    
    public void testToString() {
        MediaType mt = new MediaType("application", "octet-stream");
        assertEquals("application/octet-stream", mt.toString());
    }
}

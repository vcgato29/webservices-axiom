/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axiom.attachments;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Header;

import org.apache.axiom.om.AbstractTestCase;


/**
 * Test the PartOnFile class
 */

public class PartOnFileTest extends AbstractTestCase {
	
	public PartOnFileTest(String testName) {
        super(testName);
    }

    String inMimeFileName = "mtom/MTOMAttachmentStream.bin";
    String contentTypeString = "multipart/related; boundary=\"MIMEBoundaryurn:uuid:A3ADBAEE51A1A87B2A11443668160701\"; type=\"application/xop+xml\"; start=\"<0.urn:uuid:A3ADBAEE51A1A87B2A11443668160702@apache.org>\"; start-info=\"application/soap+xml\"; charset=UTF-8;action=\"mtomSample\"";

    File temp;

    public void setUp() throws Exception {
		createTemporaryDirectory();
    }
    
    public void tearDown() throws Exception {
		deleteTemporaryDirectory();
    }

	public void testHeaderGetSet() throws Exception {

        InputStream inStream = new FileInputStream(getTestResourceFile(inMimeFileName));
		Attachments attachments = new Attachments(inStream, contentTypeString, true, temp.getPath(), "1");

		DataHandler p = attachments.getDataHandler("1.urn:uuid:A3ADBAEE51A1A87B2A11443668160943@apache.org");
		
		if (!(p.getDataSource() instanceof FileDataSource)) {
			fail("Expected PartOnFile");
		}

//		assertEquals("<1.urn:uuid:A3ADBAEE51A1A87B2A11443668160943@apache.org>", p.getContentID());
		assertEquals("image/jpeg", p.getContentType());

//		p.addHeader("Some-New-Header", "TestNH");
//		assertEquals(p.getHeader("Some-New-Header"), "TestNH");
	}

	public void testGetAllheaders() throws Exception {

//        InputStream inStream = new FileInputStream(getTestResourceFile(inMimeFileName));
//		Attachments attachments = new Attachments(inStream, contentTypeString, true, temp.getPath(), "1");
//
//		Part p = attachments.getDataHandler("1.urn:uuid:A3ADBAEE51A1A87B2A11443668160943@apache.org");
//		
//		if (!(p instanceof PartOnFile)) {
//			fail("Expected PartOnFile");
//		}
//		
//		assertEquals("<1.urn:uuid:A3ADBAEE51A1A87B2A11443668160943@apache.org>", p.getContentID());
//
//		// Check if the enumeration works
//		p.addHeader("Some-New-Header", "TestNH");
//		
//		Enumeration e = p.getAllHeaders();
//		boolean cTypeFound = false;
//		boolean cTransferEncFound = false;
//		boolean cIdFound = false;
//		boolean snhFound = false;
//
//		while (e.hasMoreElements()) {
//			Header h = (Header) e.nextElement();
//			if (h.getName().toLowerCase().equals("content-type")) {
//				cTypeFound = true;
//			}
//			
//			if (h.getName().toLowerCase().equals("content-transfer-encoding")) {
//				cTransferEncFound = true;
//			}
//			
//			if (h.getName().toLowerCase().equals("content-id")) {
//				cIdFound = true;
//			}
//			
//			if (h.getName().toLowerCase().equals("some-new-header")) {
//				snhFound = true;
//			}
//		}
//		
//		if (!cTypeFound || !cTransferEncFound || !cIdFound || !snhFound) {
//			fail("Header enumeration failed");
//		}

	}

	private void createTemporaryDirectory() throws Exception {
		temp = File.createTempFile("partOnFileTest", ".tmp");

		if (!temp.delete()) {
			fail("Cannot delete from temporary directory. File: " + temp.toURL());
		}
		
		if (!temp.mkdir()) {
			fail("Cannot create a temporary location for part files");
		}
	}

	private void deleteTemporaryDirectory() throws Exception {

		String[] fileList = temp.list();
		for (int i=0; i < fileList.length; i++) {
            if (!(new File(temp, fileList[i])).delete()) {
            	System.err.println("WARNING: temporary directory removal failed.");
            }
        }

		if (!temp.delete()) {
			System.err.println("WARNING: temporary directory removal failed.");
		}
	}
}

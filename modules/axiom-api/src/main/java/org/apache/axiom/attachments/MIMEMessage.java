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
package org.apache.axiom.attachments;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

import org.apache.axiom.attachments.impl.ContentStore;
import org.apache.axiom.attachments.impl.ContentStoreFactory;
import org.apache.axiom.attachments.lifecycle.LifecycleManager;
import org.apache.axiom.attachments.lifecycle.impl.LifecycleManagerImpl;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.util.DetachableInputStream;
import org.apache.axiom.util.UIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.stream.EntityState;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeConfig;
import org.apache.james.mime4j.stream.MimeTokenStream;
import org.apache.james.mime4j.stream.RecursionMode;

class MIMEMessage extends AttachmentsImpl {
    private static final Log log = LogFactory.getLog(MIMEMessage.class);

    /** <code>ContentType</code> of the MIME message */
    private final ContentType contentType;
    
    private final int contentLength; // Content Length

    private final DetachableInputStream filterIS;

    private final MimeTokenStream parser;
    
    /**
     * Stores the Data Handlers of the already parsed Mime Body Parts in the order that the attachments
     * occur in the message. This map is keyed using the content-ID's.
     */
    private final Map attachmentsMap = new LinkedHashMap();

    /** <code>partIndex</code>- Number of Mime parts parsed */
    private int partIndex = 0;

    /** Container to hold streams for direct access */
    private IncomingAttachmentStreams streams;

    /** <code>boolean</code> Indicating if any streams have been directly requested */
    private boolean streamsRequested;

    /** <code>boolean</code> Indicating if any data handlers have been directly requested */
    private boolean partsRequested;

    private String firstPartId;

    private final boolean fileCacheEnable;

    private final String attachmentRepoDir;

    private final int fileStorageThreshold;
    
    private LifecycleManager manager;
    
    MIMEMessage(LifecycleManager manager, InputStream inStream, String contentTypeString, boolean fileCacheEnable,
            String attachmentRepoDir, int fileStorageThreshold, int contentLength) throws OMException {
        this.manager = manager;
        this.contentLength = contentLength;
        this.attachmentRepoDir = attachmentRepoDir;
        this.fileCacheEnable = fileCacheEnable;
        if (log.isDebugEnabled()) {
            log.debug("Attachments contentLength=" + contentLength + ", contentTypeString=" + contentTypeString);
        }
        this.fileStorageThreshold = fileStorageThreshold;
        try {
            contentType = new ContentType(contentTypeString);
        } catch (ParseException e) {
            throw new OMException(
                    "Invalid Content Type Field in the Mime Message"
                    , e);
        }

        // If the length is not known, install a TeeInputStream
        // so that we can retrieve it later.
        InputStream is = inStream;
        if (contentLength <= 0) {
            filterIS = new DetachableInputStream(inStream);
            is = filterIS;
        } else {
            filterIS = null;
        }
        
        MimeConfig config = new MimeConfig();
        config.setStrictParsing(true);
        parser = new MimeTokenStream(config);
        parser.setRecursionMode(RecursionMode.M_NO_RECURSE);
        parser.parseHeadless(is, contentTypeString);
        
        // Move the parser to the beginning of the first part
        while (parser.getState() != EntityState.T_START_BODYPART) {
            try {
                parser.next();
            } catch (IOException ex) {
                throw new OMException(ex);
            } catch (MimeException ex) {
                throw new OMException(ex);
            }
        }

        // Read the SOAP part and cache it
        getDataHandler(getSOAPPartContentID());

        // Now reset partsRequested. SOAP part is a special case which is always 
        // read beforehand, regardless of request.
        partsRequested = false;
    }

    ContentType getContentType() {
        return contentType;
    }

    LifecycleManager getLifecycleManager() {
        if(manager == null) {
            manager = new LifecycleManagerImpl();   
        }
        return manager;
    }

    void setLifecycleManager(LifecycleManager manager) {
        this.manager = manager;
    }

    DataHandler getDataHandler(String contentID) {
        do {
            DataHandler dataHandler = (DataHandler)attachmentsMap.get(contentID);
            if (dataHandler != null) {
                return dataHandler;
            }
        } while (getNextPartDataHandler() != null);
        return null;
    }

    void addDataHandler(String contentID, DataHandler dataHandler) {
        attachmentsMap.put(contentID, dataHandler);
    }
    
    void removeDataHandler(String blobContentID) {
        do {
            if (attachmentsMap.remove(blobContentID) != null) {
                return;
            }
        } while (getNextPartDataHandler() != null);
    }

    InputStream getSOAPPartInputStream() throws OMException {
        DataHandler dh;
        try {
            dh = getDataHandler(getSOAPPartContentID());
            if (dh == null) {
                throw new OMException(
                        "Mandatory Root MIME part containing the SOAP Envelope is missing");
            }
            return dh.getInputStream();
        } catch (IOException e) {
            throw new OMException(
                    "Problem with DataHandler of the Root Mime Part. ", e);
        }
    }

    String getSOAPPartContentID() {
        String rootContentID = contentType.getParameter("start");
        if (log.isDebugEnabled()) {
            log.debug("getSOAPPartContentID rootContentID=" + rootContentID);
        }

        // to handle the Start parameter not mentioned situation
        if (rootContentID == null) {
            if (partIndex == 0) {
                getNextPartDataHandler();
            }
            rootContentID = firstPartId;
        } else {
            rootContentID = rootContentID.trim();

            if ((rootContentID.indexOf("<") > -1)
                    & (rootContentID.indexOf(">") > -1)) {
                rootContentID = rootContentID.substring(1, (rootContentID
                        .length() - 1));
            }
        }
        // Strips off the "cid:" part from content-id
        if (rootContentID.length() > 4
                && "cid:".equalsIgnoreCase(rootContentID.substring(0, 4))) {
            rootContentID = rootContentID.substring(4);
        }
        return rootContentID;
    }
    
    String getSOAPPartContentType() {
        String soapPartContentID = getSOAPPartContentID();
        if (soapPartContentID == null) {
            throw new OMException("Unable to determine the content ID of the SOAP part");
        }
        DataHandler soapPart = getDataHandler(soapPartContentID);
        if (soapPart == null) {
            throw new OMException("Unable to locate the SOAP part; content ID was " + soapPartContentID);
        }
        return soapPart.getContentType();
    }
    
    IncomingAttachmentStreams getIncomingAttachmentStreams() {
        if (partsRequested) {
            throw new IllegalStateException(
                    "The attachments stream can only be accessed once; either by using the IncomingAttachmentStreams class or by getting a " +
                            "collection of AttachmentPart objects. They cannot both be called within the life time of the same service request.");
        }
        
        streamsRequested = true;
        
        if (this.streams == null) {
            this.streams = new MultipartAttachmentStreams(parser);
        }
        
        return this.streams;
    }

    /**
     * Force reading of all attachments.
     */
    private void fetchAllParts() {
        while (getNextPartDataHandler() != null) {
            // Just loop until getNextPartDataHandler returns null
        }
    }

    Set getContentIDs(boolean fetchAll) {
        if (fetchAll) {
            fetchAllParts();
        }
        return attachmentsMap.keySet();
    }
    
    Map getMap() {
        fetchAllParts();
        return Collections.unmodifiableMap(attachmentsMap);
    }
    
    long getContentLength() throws IOException {
        if (contentLength > 0) {
            return contentLength;
        } else {
            // Ensure all parts are read
            fetchAllParts();
            // Now get the count from the filter
            return filterIS.length();
        }
    }
    
    /**
     * @return the Next valid MIME part + store the Part in the Parts List
     * @throws OMException throw if content id is null or if two MIME parts contain the same
     *                     content-ID & the exceptions throws by getPart()
     */
    private DataHandler getNextPartDataHandler() throws OMException {
        if (parser.getState() == EntityState.T_END_MULTIPART) {
            return null;
        } else {
            Part nextPart = getPart();
            try {
                long size = nextPart.getSize();
                String partContentID;
                DataHandler dataHandler;
                try {
                    partContentID = nextPart.getContentID();

                    if (partContentID == null & partIndex == 1) {
                        String id = "firstPart_" + UIDGenerator.generateContentId();
                        firstPartId = id;
                        if (size > 0) {
                            dataHandler = nextPart.getDataHandler();
                        } else {
                            // Either the mime part is empty or the stream ended without having 
                            // a MIME message terminator
                            dataHandler = new DataHandler(new ByteArrayDataSource(new byte[]{}));
                        }
                        addDataHandler(id, dataHandler);
                        return dataHandler;
                    }
                    if (partContentID == null) {
                        throw new OMException(
                                "Part content ID cannot be blank for non root MIME parts");
                    }
                    if ((partContentID.indexOf("<") > -1)
                            & (partContentID.indexOf(">") > -1)) {
                        partContentID = partContentID.substring(1, (partContentID
                                .length() - 1));

                    }
                    if (partIndex == 1) {
                        firstPartId = partContentID;
                    }
                    if (attachmentsMap.containsKey(partContentID)) {
                        throw new OMException(
                                "Two MIME parts with the same Content-ID not allowed.");
                    }
                    if (size > 0) {
                        dataHandler = nextPart.getDataHandler();
                    } else {
                        // Either the mime part is empty or the stream ended without having 
                        // a MIME message terminator
                        dataHandler = new DataHandler(new ByteArrayDataSource(new byte[]{}));
                    }
                    addDataHandler(partContentID, dataHandler);
                    return dataHandler;
                } catch (MessagingException e) {
                    throw new OMException("Error reading Content-ID from the Part."
                            + e);
                }
            } catch (MessagingException e) {
                throw new OMException(e);
            }
        }
    }

    /**
     * @return This will return the next available MIME part in the stream.
     * @throws OMException if Stream ends while reading the next part...
     */
    private Part getPart() throws OMException {

        if (streamsRequested) {
            throw new IllegalStateException("The attachments stream can only be accessed once; either by using the IncomingAttachmentStreams class or by getting a collection of AttachmentPart objects. They cannot both be called within the life time of the same service request.");
        }

        partsRequested = true;

        boolean isSOAPPart = (partIndex == 0);
        int threshhold = (fileCacheEnable) ? fileStorageThreshold : 0;

        try {
            Hashtable headers = readHeaders();
            
            Header contentTypeHeader = (Header)headers.get("content-type");
            // The PartFactory will determine which Part implementation is most appropriate.
            ContentStore content = ContentStoreFactory.createContentStore(getLifecycleManager(), parser, 
                                          isSOAPPart, 
                                          contentTypeHeader == null ? null : contentTypeHeader.getValue(),
                                          threshhold, 
                                          attachmentRepoDir, 
                                          contentLength);  // content-length for the whole message
            
            EntityState state = parser.next();
            if (state == EntityState.T_EPILOGUE) {
                while (parser.next() != EntityState.T_END_MULTIPART) {
                    // Just loop
                }
            } else if (state != EntityState.T_START_BODYPART && state != EntityState.T_END_MULTIPART) {
                throw new IllegalStateException("Internal error: unexpected parser state " + state);
            }
            
            partIndex++;
            return new PartImpl(headers, content);
        } catch (IOException ex) {
            throw new OMException(ex);
        } catch (MimeException ex) {
            throw new OMException(ex);
        }
    }

    private Hashtable readHeaders() throws IOException, MimeException {
        if(log.isDebugEnabled()){
            log.debug("initHeaders");
        }
        
        checkParserState(parser.next(), EntityState.T_START_HEADER);
        
        Hashtable headers = new Hashtable();
        while (parser.next() == EntityState.T_FIELD) {
            Field field = parser.getField();
            String name = field.getName();
            String value = field.getBody();
            
            if (log.isDebugEnabled()){
                log.debug("addHeader: (" + name + ") value=(" + value +")");
            }
            Header headerObj = new Header(name, value);
            
            // Use the lower case name as the key
            String key = name.toLowerCase();
            headers.put(key, headerObj);
        }
        
        checkParserState(parser.next(), EntityState.T_BODY);
        
        return headers;
    }
    
    private static void checkParserState(EntityState state, EntityState expected) throws IllegalStateException {
        if (expected != state) {
            throw new IllegalStateException("Internal error: expected parser to be in state "
                    + expected + ", but got " + state);
        }
    }
}

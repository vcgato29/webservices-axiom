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
package org.apache.axiom.om.impl.common;

import java.io.IOException;

import javax.activation.DataHandler;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.core.CharacterData;
import org.apache.axiom.ext.stax.datahandler.DataHandlerProvider;
import org.apache.axiom.om.OMException;
import org.apache.axiom.util.base64.Base64Utils;

public final class TextContent implements CharacterData {
    final String value;
    
    final String mimeType;
    
    /** Field contentID for the mime part used when serializing Binary stuff as MTOM optimized. */
    String contentID;
    
    /**
     * Contains a {@link DataHandler} or {@link DataHandlerProvider} object if the text node
     * represents base64 encoded binary data.
     */
    Object dataHandlerObject;

    boolean optimize;
    boolean binary;
    
    TextContent(String value) {
        this.value = value;
        this.mimeType = null;
    }
    
    public TextContent(String value, String mimeType, boolean optimize) {
        this.value = value;
        this.mimeType = mimeType;
        binary = true;
        this.optimize = optimize;
    }
    
    public TextContent(Object dataHandlerObject, boolean optimize) {
        this.value = null;
        mimeType = null;
        this.dataHandlerObject = dataHandlerObject;
        binary = true;
        this.optimize = optimize;
    }
    
    public TextContent(String contentID, DataHandlerProvider dataHandlerProvider, boolean optimize) {
        this.value = null;
        mimeType = null;
        dataHandlerObject = dataHandlerProvider;
        binary = true;
        this.optimize = optimize;
    }
    
    TextContent(TextContent other) {
        this.value = other.value;
        this.mimeType = other.mimeType;
        this.contentID = other.contentID;
        this.dataHandlerObject = other.dataHandlerObject;
        this.optimize = other.optimize;
        this.binary = other.binary;
    }

    DataHandler getDataHandler() {
        if (dataHandlerObject != null) {
            if (dataHandlerObject instanceof DataHandlerProvider) {
                try {
                    dataHandlerObject = ((DataHandlerProvider)dataHandlerObject).getDataHandler();
                } catch (IOException ex) {
                    throw new OMException(ex);
                }
            }
            return (DataHandler)dataHandlerObject;
        } else if (binary) {
            return new DataHandler(new ByteArrayDataSource(Base64Utils.decode(value), mimeType));
        } else {
            throw new OMException("No DataHandler available");
        }
    }
    
    @Override
    public String toString() {
        if (dataHandlerObject != null) {
            try {
                return Base64Utils.encode(getDataHandler());
            } catch (Exception e) {
                throw new OMException(e);
            }
        } else {
            return value;
        }
    }
    
    char[] toCharArray() {
        if (dataHandlerObject != null) {
            try {
                return Base64Utils.encodeToCharArray(getDataHandler());
            } catch (IOException ex) {
                throw new OMException(ex);
            }
        } else {
            return value.toCharArray();
        }
    }
}

/*
 * Copyright (C) 2015, The Max Planck Institute for
 * Psycholinguistics.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * A copy of the GNU General Public License is included in the file
 * LICENSE-gpl-3.0.txt. If that file is missing, see
 * <http://www.gnu.org/licenses/>.
 */

package nl.clariah.oai.harvester.action;

import nl.mpi.oai.harvester.action.Action;
import nl.mpi.oai.harvester.metadata.Metadata;
import nl.mpi.oai.harvester.metadata.Record;
import nl.mpi.oai.harvester.utils.Queue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.evt.XMLEvent2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This action look for and remove record with null value only.
 */
public class OmitEmptyRecordAction implements Action {

    private final Logger logger = LogManager.getLogger(OmitEmptyRecordAction.class);

    private final XPath xpath;
    private final DocumentBuilder db;

    public OmitEmptyRecordAction() throws ParserConfigurationException {
        XPathFactory xpf = XPathFactory.newInstance();
        xpath = xpf.newXPath();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        db = dbf.newDocumentBuilder();
    }

    boolean isRootNull(Document doc) {
        if (doc == null) {
            return true;
        }
        logger.info("Root element: {}", doc.getDocumentElement().getNodeName());
        return "null".equals(doc.getDocumentElement().getNodeName());
    }

    @Override
    public boolean perform(List<Record> records) {
        logger.info("Splitting {} records", records.size());
        List<Metadata> newRecords = new ArrayList<>();

        for (Record rec : records) {
            Metadata record = (Metadata) rec;
            Document doc = record.getDoc();

            if (!isRootNull(doc)) {
                newRecords.add(record);
            }
        }

        records.clear();
        records.addAll(newRecords);
        return true;
    }

    @Override
    public String toString() {
        return "omit-empty-record";
    }

    // All split actions are equal.
    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof OmitEmptyRecordAction;
    }

    @Override
    public Action clone() {
        try {
            // All split actions are the same. This is effectively a "deep"
            // copy since it has its own XPath object.
            return new OmitEmptyRecordAction();
        } catch (ParserConfigurationException ex) {
            logger.error(ex);
        }
        return null;
    }

}

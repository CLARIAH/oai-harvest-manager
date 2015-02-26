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

package nl.mpi.oai.harvester.overview;

import nl.mpi.oai.harvester.generated.EndpointType;
import nl.mpi.oai.harvester.generated.HarvestingType;
import nl.mpi.oai.harvester.generated.ObjectFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <br>Make available endpoint type attributes <br><br>
 *
 * The attributes that determine a harvesting cycle are defined by an XML file
 * that takes a form that is defined by the harvesting.xsd file. Please refer
 * to the cycle interface and and endpoint interface for a description of the
 * semantics involved. <br><br>
 *
 * JAXB generates classes representing the XML files. It also provides a
 * factory for creating the elements in them. <br><br>
 *
 * First, an EndpointAdaptor object associates itself with a HarvestingType
 * object. After that, it looks for the endpoint. If it finds it, it remembers
 * it. Otherwise it will ask the generated JAXB factory to create a endpoint,
 * and set the fields to default values. <br><br>
 *
 * When an adapter method needs to obtain a cycle attribute, it will invoke a
 * corresponding method on the HarvestingType object, either found or created.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class EndpointAdapter implements Endpoint {

    // the JAXB created object representing elements from the XML file
    private final HarvestingType harvesting;

    // the JAXB created and URI referenced endpoint
    private EndpointType endpointType;

    // the JAXB factory needed to create a default endpoint
    private final ObjectFactory factory;

    /**
     * Create default EndpointType object
     *
     * @param endpointURI the URI identifying the endpoint
     */
    private EndpointType CreateDefault(String endpointURI) {

        /* The factory has been initialised, refer to the constructor. Ask it
           to create a new endpoint.
        */
        endpointType = factory.createEndpointType();

        // set endpoint fields to default values
        endpointType.setBlock(Boolean.FALSE);
        endpointType.setIncremental(Boolean.TRUE);
        endpointType.setURI(endpointURI);

        return endpointType;
    }

    /**
     * Look for the endpoint in a HarvestingType object, use an URI as the
     * as a key
     *
     * @param endpointURI the URI identifying the endpoint
     * @return null or the endpoint
     */
    private EndpointType FindEndpoint(String endpointURI) {

        // assume the endpoint is not there
        endpointType = null;

        // iterate over the elements in the harvested element
        Boolean found = false;

        for (int i = 0; i < harvesting.getEndpoint().size() && !found; i++) {
            endpointType = harvesting.getEndpoint().get(i);
            if (endpointType.getURI().compareTo(endpointURI) == 0) {
                found = true;
            }
        }

        if (found) {
            return endpointType;
        } else {
            return null;
        }
    }

    /**
     * Associate the adapter with an endpoint URI and HarvestingType object
     *
     * @param endpointURI the URI of the endpoint to be harvested by the cycle
     * @param harvesting the JAXB representation of the harvesting overview file
     * @param factory the JAXB factory for havesting overview XML files
     */
    public EndpointAdapter(String endpointURI, HarvestingType harvesting,
                           ObjectFactory factory) {

        this.harvesting = harvesting;
        this.factory    = factory;

        // look for the endpoint in the XML data
        endpointType = FindEndpoint(endpointURI);

        if (endpointType == null) {
            // if it is not in the XML, create a default endpoint data
            endpointType = CreateDefault(endpointURI);

            // and add this data to the XML
            harvesting.getEndpoint().add(endpointType);
        }
    }

    @Override
    public String getURI() {

        return endpointType.getURI();
    }

    @Override
    public void setURI(String URI) {

        endpointType.setURI(URI);
    }

    @Override
    public String getGroup() {

        return endpointType.getGroup();
    }

    @Override
    public void setGroup(String group) {

        endpointType.setGroup(group);
    }

    @Override
    public boolean blocked() {

        return endpointType.isBlock();
    }

    @Override
    public boolean retry() {

        return endpointType.isRetry();
    }

    @Override
    public boolean allowIncrementalHarvest() {

        return endpointType.isIncremental();
    }

    @Override
    public String getScenario() {

        return endpointType.getScenario();
    }

    @Override
    public String getRecentHarvestDate() {

        // convert XMLGregorianCalendar to string

        XMLGregorianCalendar XMLDate;
        XMLDate = harvesting.getHarvestFromDate();

        return XMLDate.toString();
    }

    @Override
    public void doneHarvesting(Boolean done) {

        // try to get the current date

        XMLGregorianCalendar XMLDate;

        try {
            XMLDate = DatatypeFactory.newInstance().newXMLGregorianCalendar();

            Calendar c = Calendar.getInstance();
            c.getTime();
            XMLDate.setDay(c.get(Calendar.DAY_OF_MONTH));
            XMLDate.setMonth(c.get(Calendar.MONTH) + 1);
            XMLDate.setYear(c.get(Calendar.YEAR));

            // in any case, at this date an attempt was made
            endpointType.setAttempted(XMLDate);

            if (done) {

                // set a new date for incremental harvesting
                endpointType.setHarvested(XMLDate);
            }

        } catch (DatatypeConfigurationException ex) {

            Logger.getLogger(EndpointAdapter.class.getName()).log(
                    Level.SEVERE, null, endpointType);
        }
    }

    @Override
    public long getCount() {

        return endpointType.getCount();
    }

    @Override
    public void setCount(long count) {

        endpointType.setCount(count);
    }

    @Override
    public long getIncrement() {

        return endpointType.getIncrement();
    }

    @Override
    public void setIncrement(long increment) {
        endpointType.setIncrement(increment);
    }
}

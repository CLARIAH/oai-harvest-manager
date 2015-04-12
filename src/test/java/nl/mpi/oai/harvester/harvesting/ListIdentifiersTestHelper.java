
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

package nl.mpi.oai.harvester.harvesting;

import java.util.ArrayList;

/**
 * <br> Define the traces for a test targeting the list identifiers scenario <br><br>
 *
 * A trace definition needs to be added for every record the scenario for
 * harvesting yields. By letting the addToTable method spy on the metadata
 * constructor invoked each time a scenario creates a record, in the end the
 * test should end up with an empty list of traces.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public class ListIdentifiersTestHelper extends TestHelper {

    @Override
    ArrayList<Trace> getTraces() {

        ArrayList<Trace> traces= new ArrayList<>();

        traces.add(new Trace ("http://metalb.csc.fi/cgi-bin/que", "cmdi0571", "oai:kielipankki.fi:sh03de30"));
        traces.add(new Trace ("http://metalb.csc.fi/cgi-bin/que", "cmdi0571", "oai:kielipankki.fi:sh7b8760"));
        traces.add(new Trace ("http://metalb.csc.fi/cgi-bin/que", "cmdi0571", "oai:kielipankki.fi:sh034a70"));
        traces.add(new Trace ("http://metalb.csc.fi/cgi-bin/que", "cmdi0571", "oai:kielipankki.fi:sh136cd0"));
        traces.add(new Trace ("http://metalb.csc.fi/cgi-bin/que", "cmdi0571", "oai:kielipankki.fi:sh3123d0"));
        traces.add(new Trace ("http://metalb.csc.fi/cgi-bin/que", "cmdi0571", "oai:kielipankki.fi:shaccf90"));
        traces.add(new Trace ("http://metalb.csc.fi/cgi-bin/que", "cmdi0571", "oai:kielipankki.fi:shd0d800"));
        traces.add(new Trace ("http://metalb.csc.fi/cgi-bin/que", "cmdi0571", "oai:kielipankki.fi:shff9200"));

        return traces;
    }

    @Override
    String[] getEndpointURIs() {
        // a table of endpoint URIs
        final String[] endpointURIs;

        endpointURIs = new String[]{
                "http://metalb.csc.fi/cgi-bin/que"};

        return endpointURIs;
    }

    @Override
    String getTestName() {

        return "ListIdentifiers";
    }
}

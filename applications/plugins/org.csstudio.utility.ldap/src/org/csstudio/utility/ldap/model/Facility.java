/*
 * Copyright (c) 2008 Stiftung Deutsches Elektronen-Synchrotron,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS.
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE.
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS,
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION,
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */
package org.csstudio.utility.ldap.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.log4j.Logger;
import org.csstudio.platform.logging.CentralLogger;
import org.csstudio.utility.ldap.LdapUtils;

/**
 *
 *
 * @author bknerr
 */
public class Facility {
    private final Logger LOG = CentralLogger.getInstance().getLogger(this);

    private final String _name;

    private final Map<String, IOC> _iocs = new HashMap<String, IOC>();

    /**
     * Constructor.
     */
    public Facility(final String name) {
        _name = name;
    }

    public IOC addIOC(final String efan, final String econ) {
        return addIOC(efan, econ, null);
    }

    public IOC addIOC(final String efan, final String econ, final Attributes attributes) {
        final String econKey = econ.toUpperCase();
        IOC ioc = _iocs.get(econKey);
        if (ioc == null) {
            ioc = new IOC(econ, efan);
            _iocs.put(econKey, ioc);
        }
        if (attributes != null) {
            final Attribute emailAddressAttr = attributes.get(LdapUtils.ATTR_FIELD_RESPONSIBLE_PERSON);
            if (emailAddressAttr != null) {
                try {
                    ioc.setResponsible((String)emailAddressAttr.get());
                } catch (final NoSuchElementException nsee) {
                    LOG.warn("Attribute " + LdapUtils.ATTR_FIELD_RESPONSIBLE_PERSON + " has not any values set.");
                } catch (final NamingException ne) {
                    LOG.warn("Attribute " + LdapUtils.ATTR_FIELD_RESPONSIBLE_PERSON + " could not be retrieved.\n" + ne.getExplanation());
                }
            }
        }
        return ioc;
    }

    public IOC getIOC(final String iocName) {
        return _iocs.get(iocName.toUpperCase());
    }

    /**
     * The names of the contained IOCs.
     * @return a copy of the name set of the currently contained IOCs
     */
    public Set<String> getIocNames() {
        return new HashSet<String>(_iocs.keySet());
    }

    /**
     * A copy of the contained IOCs.
     * @return a copy of the set of the currently contained IOCs
     */
    public Set<IOC> getIOCs() {
        return new HashSet<IOC>(_iocs.values());
    }

    public String getName() {
        return _name;
    }

}
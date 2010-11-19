/*
 * Copyright (c) 2010 Stiftung Deutsches Elektronen-Synchrotron,
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
package org.csstudio.archive.service.mysqlimpl.dao;

import static org.csstudio.archive.service.mysqlimpl.MySQLArchiveServicePreference.AUTO_CONNECT;
import static org.csstudio.archive.service.mysqlimpl.MySQLArchiveServicePreference.PASSWORD;
import static org.csstudio.archive.service.mysqlimpl.MySQLArchiveServicePreference.URL;
import static org.csstudio.archive.service.mysqlimpl.MySQLArchiveServicePreference.USER;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.log4j.Logger;
import org.csstudio.archive.service.ArchiveConnectionException;
import org.csstudio.archive.service.mysqlimpl.channel.ArchiveChannelDaoImpl;
import org.csstudio.archive.service.mysqlimpl.channel.IArchiveChannelDao;
import org.csstudio.archive.service.mysqlimpl.engine.ArchiveEngineDaoImpl;
import org.csstudio.archive.service.mysqlimpl.engine.IArchiveEngineDao;
import org.csstudio.archive.service.mysqlimpl.sample.ArchiveSampleDaoImpl;
import org.csstudio.archive.service.mysqlimpl.sample.IArchiveSampleDao;
import org.csstudio.archive.service.mysqlimpl.samplemode.ArchiveSampleModeDaoImpl;
import org.csstudio.archive.service.mysqlimpl.samplemode.IArchiveSampleModeDao;
import org.csstudio.archive.service.mysqlimpl.severity.ArchiveSeverityDaoImpl;
import org.csstudio.archive.service.mysqlimpl.severity.IArchiveSeverityDao;
import org.csstudio.archive.service.mysqlimpl.status.ArchiveStatusDaoImpl;
import org.csstudio.archive.service.mysqlimpl.status.IArchiveStatusDao;
import org.csstudio.platform.logging.CentralLogger;

import com.google.common.collect.Maps;

/**
 * The archive dao manager.
 *
 * Envisioned to handle connection pools and transactions with CRUD command abstraction.
 *
 * @author bknerr
 * @since 11.11.2010
 */
public enum ArchiveDaoManager {

    INSTANCE;

    private static final Logger LOG = CentralLogger.getInstance().getLogger(ArchiveDaoManager.class);

    private static final String ARCHIVE_CONNECTION_EXCEPTION_MSG = "Archive connection could not be established";

    private String _url;
    private String _user;
    private String _password;

    private final Boolean _autoConnect = AUTO_CONNECT.getValue();

    /**
     * Any thread owns a connection.
     */
    private final ThreadLocal<Connection> _archiveConnection = new ThreadLocal<Connection>();

    /**
     * DAOs.
     */
    private IArchiveChannelDao _archiveChannelDao;
    private IArchiveSampleModeDao _archiveSampleModeDao;
    private IArchiveSampleDao _archiveSampleDao;
    private IArchiveEngineDao _archiveEngineDao;
    private IArchiveSeverityDao _archiveSeverityDao;
    private IArchiveStatusDao _archiveStatusDao;

    /**
     * Constructor.
     */
    private ArchiveDaoManager() {

        if (_autoConnect) {
            final Map<String, Object> prefs = createConnectionPrefsFromEclipsePrefs();

            try {
                connect(prefs);
            } catch (final ArchiveConnectionException e) {
                // FIXME (bknerr) : Cannot be propagated by enum constructor!
                _archiveConnection.set(null);
            }
        }
    }

    public void connect(@Nonnull final Map<String, Object> prefs) throws ArchiveConnectionException {

        try {
            Connection connection = _archiveConnection.get();
            if (connection != null) { // close existing connection
                _archiveConnection.set(null);
                connection.close();
            }
            _url = (String) prefs.get(URL.getKeyAsString());
            _user = (String) prefs.get(USER.getKeyAsString());
            _password = (String) prefs.get(PASSWORD.getKeyAsString());

            // Get class loader to find the driver
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            connection = DriverManager.getConnection(_url, _user, _password);

            final DatabaseMetaData meta = connection.getMetaData();
            LOG.debug("MySQL connection: " +
                      meta.getDatabaseProductName() +
                      " " +
                      meta.getDatabaseProductVersion());

            // allow for transactions ?!
            connection.setAutoCommit(false);

            _archiveConnection.set(connection);

        } catch (final InstantiationException e) {
            throw new ArchiveConnectionException(ARCHIVE_CONNECTION_EXCEPTION_MSG, e);
        } catch (final IllegalAccessException e) {
            throw new ArchiveConnectionException(ARCHIVE_CONNECTION_EXCEPTION_MSG, e);
        } catch (final ClassNotFoundException e) {
            throw new ArchiveConnectionException(ARCHIVE_CONNECTION_EXCEPTION_MSG, e);
        } catch (final SQLException e) {
            throw new ArchiveConnectionException(ARCHIVE_CONNECTION_EXCEPTION_MSG, e);
        }
    }

    public void reconnect() throws ArchiveConnectionException {
        final Map<String, Object> prefs = createConnectionPrefsFromEclipsePrefs();
        connect(prefs);
    }

    private Map<String, Object> createConnectionPrefsFromEclipsePrefs() {
        final Map<String, Object> prefs = Maps.newHashMap();
        prefs.put(URL.getKeyAsString(), URL.getValue());
        prefs.put(USER.getKeyAsString(), USER.getValue());
        prefs.put(PASSWORD.getKeyAsString(), PASSWORD.getValue());
        return prefs;
    }

    /**
     * Disconnects the connection for the owning thread.
     * @throws ArchiveConnectionException
     */
    public void disconnect() throws ArchiveConnectionException {
        final Connection connection = _archiveConnection.get();
        if (connection != null) {
            try {
                connection.close();
            } catch (final SQLException e) {
                throw new ArchiveConnectionException("Archive disconnection failed. Ignore.", e);
            }
        }
    }


    /**
     * Returns the current connection for the owning thread.
     * This method is invoked by the dedicated daos to retrieve their connection.
     *
     * @return the connection
     */
    @CheckForNull
    Connection getConnection() {
        return _archiveConnection.get();
    }

    /**
     * @return the archive channel dao
     */
    @Nonnull
    public IArchiveChannelDao getChannelDao() {
        if (_archiveChannelDao == null) {
            _archiveChannelDao = new ArchiveChannelDaoImpl();
        }
        return _archiveChannelDao;
    }

    /**
     * @return the archive sample mode dao
     */
    @Nonnull
    public IArchiveSampleModeDao getSampleModeDao() {
        if (_archiveSampleModeDao == null) {
            _archiveSampleModeDao = new ArchiveSampleModeDaoImpl();
        }
        return _archiveSampleModeDao;
    }

    /**
     * @return the archive sample dao
     */
    @Nonnull
    public IArchiveSampleDao getSampleDao() {
        if (_archiveSampleDao == null) {
            _archiveSampleDao = new ArchiveSampleDaoImpl();
        }
        return _archiveSampleDao;
    }

    /**
     * @return the archive engine dao
     */
    @Nonnull
    public IArchiveEngineDao getEngineDao() {
        if (_archiveEngineDao == null) {
            _archiveEngineDao = new ArchiveEngineDaoImpl();
        }
        return _archiveEngineDao;
    }

    /**
     * @return the archive severity dao
     */
    @Nonnull
    public IArchiveSeverityDao getSeverityDao() {
        if (_archiveSeverityDao == null) {
            _archiveSeverityDao = new ArchiveSeverityDaoImpl();
        }
        return _archiveSeverityDao;
    }

    /**
     * @return the archive status dao
     */
    @Nonnull
    public IArchiveStatusDao getStatusDao() {
        if (_archiveStatusDao == null) {
            _archiveStatusDao = new ArchiveStatusDaoImpl();
        }
        return _archiveStatusDao;
    }
}

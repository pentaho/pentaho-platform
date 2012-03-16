/**
 * The Pentaho proprietary code is licensed under the terms and conditions
 * of the software license agreement entered into between the entity licensing
 * such code and Pentaho Corporation. 
 */
package org.pentaho.platform.plugin.services.importexport.pdi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryAttributeInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNodeRef;
import org.pentaho.platform.repository.RepositoryFilenameUtils;

public class JobDelegate extends AbstractDelegate implements java.io.Serializable {

  private static final long serialVersionUID = -1006715561242639895L; /* EESOURCE: UPDATE SERIALVERUID */

  private static final String PROP_SHARED_FILE = "SHARED_FILE"; //$NON-NLS-1$

  private static final String PROP_USE_LOGFIELD = "USE_LOGFIELD";//$NON-NLS-1$

  private static final String PROP_PASS_BATCH_ID = "PASS_BATCH_ID";//$NON-NLS-1$

  private static final String PROP_USE_BATCH_ID = "USE_BATCH_ID";//$NON-NLS-1$

  private static final String PROP_MODIFIED_DATE = "MODIFIED_DATE";//$NON-NLS-1$

  private static final String PROP_MODIFIED_USER = "MODIFIED_USER";//$NON-NLS-1$

  private static final String PROP_CREATED_DATE = "CREATED_DATE";//$NON-NLS-1$

  private static final String PROP_CREATED_USER = "CREATED_USER";//$NON-NLS-1$

  private static final String PROP_TABLE_NAME_LOG = "TABLE_NAME_LOG";//$NON-NLS-1$

  private static final String PROP_DATABASE_LOG = "DATABASE_LOG";//$NON-NLS-1$

  private static final String PROP_JOB_STATUS = "JOB_STATUS";//$NON-NLS-1$

  private static final String PROP_JOB_VERSION = "JOB_VERSION";//$NON-NLS-1$

  private static final String PROP_EXTENDED_DESCRIPTION = "EXTENDED_DESCRIPTION";//$NON-NLS-1$

  private static final String NODE_PARAMETERS = "parameters";//$NON-NLS-1$

  private static final String PROP_NR_PARAMETERS = "NR_PARAMETERS";//$NON-NLS-1$

  private static final String PROP_NR_HOPS = "NR_HOPS";//$NON-NLS-1$

  private static final String NODE_HOPS = "hops";//$NON-NLS-1$

  private static final String NODE_CUSTOM = "custom";//$NON-NLS-1$

  private static final String PROP_JOBENTRY_TYPE = "JOBENTRY_TYPE";//$NON-NLS-1$

  private static final String PROP_PARALLEL = "PARALLEL";//$NON-NLS-1$

  private static final String PROP_GUI_DRAW = "GUI_DRAW";//$NON-NLS-1$

  private static final String PROP_GUI_LOCATION_Y = "GUI_LOCATION_Y";//$NON-NLS-1$

  private static final String PROP_GUI_LOCATION_X = "GUI_LOCATION_X";//$NON-NLS-1$

  // ~ Static fields/initializers ======================================================================================

  private static final String PROP_NR = "NR";//$NON-NLS-1$

  private static final String PROP_NR_JOB_ENTRY_COPIES = "NR_JOB_ENTRY_COPIES";//$NON-NLS-1$

  private static final String PROP_NR_NOTES = "NR_NOTES";//$NON-NLS-1$

  private static final String NODE_JOB = "job";//$NON-NLS-1$

  private static final String NODE_NOTES = "notes";//$NON-NLS-1$

  private static final String NOTE_PREFIX = "__NOTE__#";//$NON-NLS-1$

  private static final String PROP_XML = "XML";//$NON-NLS-1$

  private static final String NODE_ENTRIES = "entries";//$NON-NLS-1$

  private static final String EXT_JOB_ENTRY_COPY = ".kjc";//$NON-NLS-1$

  private static final String JOB_HOP_FROM = "JOB_HOP_FROM";//$NON-NLS-1$

  private static final String JOB_HOP_FROM_NR = "JOB_HOP_FROM_NR";//$NON-NLS-1$

  private static final String JOB_HOP_TO = "JOB_HOP_TO";//$NON-NLS-1$

  private static final String JOB_HOP_TO_NR = "JOB_HOP_TO_NR";//$NON-NLS-1$

  private static final String JOB_HOP_ENABLED = "JOB_HOP_ENABLED";//$NON-NLS-1$

  private static final String JOB_HOP_EVALUATION = "JOB_HOP_EVALUATION";//$NON-NLS-1$

  private static final String JOB_HOP_UNCONDITIONAL = "JOB_HOP_UNCONDITIONAL";//$NON-NLS-1$

  private static final String JOB_HOP_PREFIX = "__JOB_HOP__#";//$NON-NLS-1$

  private static final String PARAM_PREFIX = "__PARAM_#";//$NON-NLS-1$

  private static final String PARAM_KEY = "KEY";//$NON-NLS-1$

  private static final String PARAM_DESC = "DESC";//$NON-NLS-1$

  private static final String PARAM_DEFAULT = "DEFAULT";//$NON-NLS-1$

  private static final String PROP_LOG_SIZE_LIMIT = "LOG_SIZE_LIMIT";//$NON-NLS-1$

  private static Class<?> PKG = JobDelegate.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  // ~ Instance fields =================================================================================================

  private Repository repo;

  // ~ Constructors ====================================================================================================

  public JobDelegate(final Repository repo) {
    super();
    this.repo = repo;
  }

  // ~ Methods =========================================================================================================
  @SuppressWarnings("unchecked")
  public SharedObjects loadSharedObjects(final RepositoryElementInterface element,
      final Map<RepositoryObjectType, List<? extends SharedObjectInterface>> sharedObjectsByType)
      throws KettleException {
    JobMeta jobMeta = (JobMeta) element;
    jobMeta.setSharedObjects(jobMeta.readSharedObjects());

    // Repository objects take priority so let's overwrite them...
    //
    readDatabases(jobMeta, true, (List<DatabaseMeta>) sharedObjectsByType.get(RepositoryObjectType.DATABASE));
    readSlaves(jobMeta, true, (List<SlaveServer>) sharedObjectsByType.get(RepositoryObjectType.SLAVE_SERVER));

    return jobMeta.getSharedObjects();
  }

  public void saveSharedObjects(final RepositoryElementInterface element, final String versionComment)
      throws KettleException {
    JobMeta jobMeta = (JobMeta) element;
    // Now store the databases in the job.
    // Only store if the database has actually changed or doesn't have an object ID (imported)
    //
    for (DatabaseMeta databaseMeta : jobMeta.getDatabases()) {
      if (databaseMeta.hasChanged() || databaseMeta.getObjectId() == null) {
        repo.save(databaseMeta, versionComment, null);
      }
    }

    // Store the slave server
    //
    for (SlaveServer slaveServer : jobMeta.getSlaveServers()) {
      if (slaveServer.hasChanged() || slaveServer.getObjectId() == null) {
        repo.save(slaveServer, versionComment, null);
      }
    }

  }

  public RepositoryElementInterface dataNodeToElement(final DataNode rootNode) throws KettleException {
    JobMeta jobMeta = new JobMeta();
    dataNodeToElement(rootNode, jobMeta);
    return jobMeta;
  }

  public void dataNodeToElement(final DataNode rootNode, final RepositoryElementInterface element)
      throws KettleException {

    JobMeta jobMeta = (JobMeta) element;

    jobMeta.setSharedObjectsFile(getString(rootNode, PROP_SHARED_FILE));

    // Keep a unique list of job entries to facilitate in the loading.
    //
    List<JobEntryInterface> jobentries = new ArrayList<JobEntryInterface>();

    // Read the job entry copies
    //
    DataNode entriesNode = rootNode.getNode(NODE_ENTRIES);
    int nrCopies = (int) entriesNode.getProperty(PROP_NR_JOB_ENTRY_COPIES).getLong();

    // read the copies...
    //
    for (DataNode copyNode : entriesNode.getNodes()) {

      // Read the entry...
      //
      JobEntryInterface jobEntry = readJobEntry(copyNode, jobMeta, jobentries);

      JobEntryCopy copy = new JobEntryCopy(jobEntry);

      copy.setName(getString(copyNode, PROP_NAME));
      copy.setDescription(getString(copyNode, PROP_DESCRIPTION));
      copy.setObjectId(new StringObjectId(copyNode.getId().toString()));

      copy.setNr((int) copyNode.getProperty(PROP_NR).getLong());
      int x = (int) copyNode.getProperty(PROP_GUI_LOCATION_X).getLong();
      int y = (int) copyNode.getProperty(PROP_GUI_LOCATION_Y).getLong();
      copy.setLocation(x, y);
      copy.setDrawn(copyNode.getProperty(PROP_GUI_DRAW).getBoolean());
      copy.setLaunchingInParallel(copyNode.getProperty(PROP_PARALLEL).getBoolean());

      jobMeta.getJobCopies().add(copy);

    }

    if (jobMeta.getJobCopies().size() != nrCopies) {
      throw new KettleException("The number of job entry copies read [" + jobMeta.getJobCopies().size()
          + "] was not the number we expected [" + nrCopies + "]");
    }

    // Read the notes...
    //
    DataNode notesNode = rootNode.getNode(NODE_NOTES);
    int nrNotes = (int) notesNode.getProperty(PROP_NR_NOTES).getLong();
    for (DataNode noteNode : notesNode.getNodes()) {
      String xml = getString(noteNode, PROP_XML);
      jobMeta.addNote(new NotePadMeta(XMLHandler.getSubNode(XMLHandler.loadXMLString(xml), NotePadMeta.XML_TAG)));
    }
    if (jobMeta.nrNotes() != nrNotes) {
      throw new KettleException("The number of notes read [" + jobMeta.nrNotes() + "] was not the number we expected ["
          + nrNotes + "]");
    }

    // Read the hops...
    //
    DataNode hopsNode = rootNode.getNode(NODE_HOPS);
    int nrHops = (int) hopsNode.getProperty(PROP_NR_HOPS).getLong();
    for (DataNode hopNode : hopsNode.getNodes()) {
      String copyFromName = getString(hopNode, JOB_HOP_FROM);
      int copyFromNr = (int) hopNode.getProperty(JOB_HOP_FROM_NR).getLong();
      String copyToName = getString(hopNode, JOB_HOP_TO);
      int copyToNr = (int) hopNode.getProperty(JOB_HOP_TO_NR).getLong();

      boolean enabled = true;
      if (hopNode.hasProperty(JOB_HOP_ENABLED)) {
        enabled = hopNode.getProperty(JOB_HOP_ENABLED).getBoolean();
      }

      boolean evaluation = true;
      if (hopNode.hasProperty(JOB_HOP_EVALUATION)) {
        evaluation = hopNode.getProperty(JOB_HOP_EVALUATION).getBoolean();
      }

      boolean unconditional = true;
      if (hopNode.hasProperty(JOB_HOP_UNCONDITIONAL)) {
        unconditional = hopNode.getProperty(JOB_HOP_UNCONDITIONAL).getBoolean();
      }

      JobEntryCopy copyFrom = jobMeta.findJobEntry(copyFromName, copyFromNr, true);
      JobEntryCopy copyTo = jobMeta.findJobEntry(copyToName, copyToNr, true);

      JobHopMeta jobHopMeta = new JobHopMeta(copyFrom, copyTo);
      jobHopMeta.setEnabled(enabled);
      jobHopMeta.setEvaluation(evaluation);
      jobHopMeta.setUnconditional(unconditional);
      jobMeta.addJobHop(jobHopMeta);

    }
    if (jobMeta.nrJobHops() != nrHops) {
      throw new KettleException("The number of hops read [" + jobMeta.nrJobHops()
          + "] was not the number we expected [" + nrHops + "]");
    }

    // Load the details at the end, to make sure we reference the databases correctly, etc.
    //
    loadJobMetaDetails(rootNode, jobMeta);

    jobMeta.eraseParameters();
    DataNode paramsNode = rootNode.getNode(NODE_PARAMETERS);
    int count = (int) paramsNode.getProperty(PROP_NR_PARAMETERS).getLong();
    for (int idx = 0; idx < count; idx++) {
      DataNode paramNode = paramsNode.getNode(PARAM_PREFIX + idx);
      String key = getString(paramNode, PARAM_KEY);
      String def = getString(paramNode, PARAM_DEFAULT);
      String desc = getString(paramNode, PARAM_DESC);
      jobMeta.addParameterDefinition(key, def, desc);
    }
  }

  protected void loadJobMetaDetails(DataNode rootNode, JobMeta jobMeta) throws KettleException {
    try {
      jobMeta.setExtendedDescription(getString(rootNode, PROP_EXTENDED_DESCRIPTION));
      jobMeta.setJobversion(getString(rootNode, PROP_JOB_VERSION));
      jobMeta.setJobstatus((int) rootNode.getProperty(PROP_JOB_STATUS).getLong());
      jobMeta.getJobLogTable().setTableName(getString(rootNode, PROP_TABLE_NAME_LOG));

      jobMeta.setCreatedUser(getString(rootNode, PROP_CREATED_USER));
      jobMeta.setCreatedDate(getDate(rootNode, PROP_CREATED_DATE));

      jobMeta.setModifiedUser(getString(rootNode, PROP_MODIFIED_USER));
      jobMeta.setModifiedDate(getDate(rootNode, PROP_MODIFIED_DATE));

      if (rootNode.hasProperty(PROP_DATABASE_LOG)) {
        String id = rootNode.getProperty(PROP_DATABASE_LOG).getRef().getId().toString();
        DatabaseMeta conn = (DatabaseMeta.findDatabase(jobMeta.getDatabases(), new StringObjectId(id)));
        jobMeta.getJobLogTable().setConnectionName(conn.getName());
      }

      jobMeta.getJobLogTable().setBatchIdUsed(rootNode.getProperty(PROP_USE_BATCH_ID).getBoolean());
      jobMeta.setBatchIdPassed(rootNode.getProperty(PROP_PASS_BATCH_ID).getBoolean());
      jobMeta.getJobLogTable().setLogFieldUsed(rootNode.getProperty(PROP_USE_LOGFIELD).getBoolean());

      jobMeta.getJobLogTable().setLogSizeLimit(getString(rootNode, PROP_LOG_SIZE_LIMIT));

      // Load the logging tables too..
      //
      RepositoryAttributeInterface attributeInterface = new RepositoryAttribute(rootNode, jobMeta.getDatabases());
      for (LogTableInterface logTable : jobMeta.getLogTables()) {
        logTable.loadFromRepository(attributeInterface);
      }
    } catch (Exception e) {
      throw new KettleException("Error loading job details", e);
    }

  }

  protected JobEntryInterface readJobEntry(DataNode copyNode, JobMeta jobMeta, List<JobEntryInterface> jobentries)
      throws KettleException {
    try {
      String name = getString(copyNode, PROP_NAME);
      for (JobEntryInterface entry : jobentries) {
        if (entry.getName().equalsIgnoreCase(name)) {
          return entry; // already loaded!
        }
      }

      // load the entry from the node
      //
      String typeId = getString(copyNode, "JOBENTRY_TYPE");
      
      PluginRegistry registry = PluginRegistry.getInstance();
      PluginInterface jobPlugin = registry.findPluginWithId(JobEntryPluginType.class, typeId);
      JobEntryInterface entry = (JobEntryInterface) registry.loadClass(jobPlugin);
      entry.setName(name);
      entry.setDescription(getString(copyNode, PROP_DESCRIPTION));
      entry.setObjectId(new StringObjectId(copyNode.getId().toString()));
      RepositoryProxy proxy = new RepositoryProxy(copyNode.getNode(NODE_CUSTOM));
      entry.loadRep(proxy, null, jobMeta.getDatabases(), jobMeta.getSlaveServers());
      jobentries.add(entry);
      return entry;
    } catch (Exception e) {
      throw new KettleException("Unable to read job entry interface information from repository", e);
    }
  }

  public DataNode elementToDataNode(final RepositoryElementInterface element, final List<Character> reservedChars) throws KettleException {
    JobMeta jobMeta = (JobMeta) element;
    DataNode rootNode = new DataNode(NODE_JOB);

    // Save the notes
    //
    DataNode notesNode = rootNode.addNode(NODE_NOTES);

    notesNode.setProperty(PROP_NR_NOTES, jobMeta.nrNotes());
    for (int i = 0; i < jobMeta.nrNotes(); i++) {
      NotePadMeta note = jobMeta.getNote(i);
      DataNode noteNode = notesNode.addNode(NOTE_PREFIX + i);
      noteNode.setProperty(PROP_XML, note.getXML());
    }

    //
    // Save the job entry copies
    //
    if (log.isDetailed()) {
      log.logDetailed(toString(), "Saving " + jobMeta.nrJobEntries() + " Job enty copies to repository..."); //$NON-NLS-1$ //$NON-NLS-2$
    }
    DataNode entriesNode = rootNode.addNode(NODE_ENTRIES);
    entriesNode.setProperty(PROP_NR_JOB_ENTRY_COPIES, jobMeta.nrJobEntries());
    for (int i = 0; i < jobMeta.nrJobEntries(); i++) {

      JobEntryCopy copy = jobMeta.getJobEntry(i);
      JobEntryInterface entry = copy.getEntry();

      // Create a new node for each entry...
      //
      DataNode copyNode = entriesNode.addNode(RepositoryFilenameUtils.escape(copy.getName(), reservedChars) + "_" + (i + 1) //$NON-NLS-1$
          + EXT_JOB_ENTRY_COPY);

      copyNode.setProperty(PROP_NAME, copy.getName());
      copyNode.setProperty(PROP_DESCRIPTION, copy.getDescription());

      copyNode.setProperty(PROP_NR, copy.getNr());
      copyNode.setProperty(PROP_GUI_LOCATION_X, copy.getLocation().x);
      copyNode.setProperty(PROP_GUI_LOCATION_Y, copy.getLocation().y);
      copyNode.setProperty(PROP_GUI_DRAW, copy.isDrawn());
      copyNode.setProperty(PROP_PARALLEL, copy.isLaunchingInParallel());

      // Save the entry information here as well, for completeness.  TODO: since this slightly stores duplicate information, figure out how to store this separately.
      //
      copyNode.setProperty(PROP_JOBENTRY_TYPE, entry.getPluginId());
      DataNode customNode = new DataNode(NODE_CUSTOM);
      RepositoryProxy proxy = new RepositoryProxy(customNode);
      entry.saveRep(proxy, null);
      copyNode.addNode(customNode);
    }

    // Finally, save the hops
    //
    DataNode hopsNode = rootNode.addNode(NODE_HOPS);
    hopsNode.setProperty(PROP_NR_HOPS, jobMeta.nrJobHops());
    for (int i = 0; i < jobMeta.nrJobHops(); i++) {
      JobHopMeta hop = jobMeta.getJobHop(i);
      DataNode hopNode = hopsNode.addNode(JOB_HOP_PREFIX + i);

      hopNode.setProperty(JOB_HOP_FROM, hop.getFromEntry().getName());
      hopNode.setProperty(JOB_HOP_FROM_NR, hop.getFromEntry().getNr());
      hopNode.setProperty(JOB_HOP_TO, hop.getToEntry().getName());
      hopNode.setProperty(JOB_HOP_TO_NR, hop.getToEntry().getNr());
      hopNode.setProperty(JOB_HOP_ENABLED, hop.isEnabled());
      hopNode.setProperty(JOB_HOP_EVALUATION, hop.getEvaluation());
      hopNode.setProperty(JOB_HOP_UNCONDITIONAL, hop.isUnconditional());
    }

    String[] paramKeys = jobMeta.listParameters();
    DataNode paramsNode = rootNode.addNode(NODE_PARAMETERS);
    paramsNode.setProperty(PROP_NR_PARAMETERS, paramKeys == null ? 0 : paramKeys.length);

    for (int idx = 0; idx < paramKeys.length; idx++) {
      DataNode paramNode = paramsNode.addNode(PARAM_PREFIX + idx);
      String key = paramKeys[idx];
      String description = jobMeta.getParameterDescription(paramKeys[idx]);
      String defaultValue = jobMeta.getParameterDefault(paramKeys[idx]);

      paramNode.setProperty(PARAM_KEY, key != null ? key : ""); //$NON-NLS-1$
      paramNode.setProperty(PARAM_DEFAULT, defaultValue != null ? defaultValue : ""); //$NON-NLS-1$
      paramNode.setProperty(PARAM_DESC, description != null ? description : ""); //$NON-NLS-1$
    }

    // Let's not forget to save the details of the transformation itself.
    // This includes logging information, parameters, etc.
    //
    saveJobDetails(rootNode, jobMeta);

    return rootNode;
  }

  private void saveJobDetails(DataNode rootNode, JobMeta jobMeta) throws KettleException {
    rootNode.setProperty(PROP_EXTENDED_DESCRIPTION, jobMeta.getExtendedDescription());
    rootNode.setProperty(PROP_JOB_VERSION, jobMeta.getJobversion());
    rootNode.setProperty(PROP_JOB_STATUS, jobMeta.getJobstatus() < 0 ? -1L : jobMeta.getJobstatus());

    if (jobMeta.getJobLogTable().getDatabaseMeta() != null) {
      DataNodeRef ref = new DataNodeRef(jobMeta.getJobLogTable().getDatabaseMeta().getObjectId().getId());
      rootNode.setProperty(PROP_DATABASE_LOG, ref);
    }
    rootNode.setProperty(PROP_TABLE_NAME_LOG, jobMeta.getJobLogTable().getTableName());

    rootNode.setProperty(PROP_CREATED_USER, jobMeta.getCreatedUser());
    rootNode.setProperty(PROP_CREATED_DATE, jobMeta.getCreatedDate());
    rootNode.setProperty(PROP_MODIFIED_USER, jobMeta.getModifiedUser());
    rootNode.setProperty(PROP_MODIFIED_DATE, jobMeta.getModifiedDate());
    rootNode.setProperty(PROP_USE_BATCH_ID, jobMeta.getJobLogTable().isBatchIdUsed());
    rootNode.setProperty(PROP_PASS_BATCH_ID, jobMeta.isBatchIdPassed());
    rootNode.setProperty(PROP_USE_LOGFIELD, jobMeta.getJobLogTable().isLogFieldUsed());
    rootNode.setProperty(PROP_SHARED_FILE, jobMeta.getSharedObjectsFile());
    
    rootNode.setProperty(PROP_LOG_SIZE_LIMIT, jobMeta.getJobLogTable().getLogSizeLimit());
    
    // Save the logging tables too..
    //
    RepositoryAttributeInterface attributeInterface = new RepositoryAttribute(rootNode, jobMeta.getDatabases());
    for (LogTableInterface logTable : jobMeta.getLogTables()) {
      logTable.saveToRepository(attributeInterface);
    }
  }

  /**
   * Insert all the databases from the repository into the JobMeta object, overwriting optionally
   * 
   * @param JobMeta The transformation to load into.
   * @param overWriteShared if an object with the same name exists, overwrite 
   */
  protected void readDatabases(JobMeta jobMeta, boolean overWriteShared, List<DatabaseMeta> databaseMetas){
    for (DatabaseMeta databaseMeta : databaseMetas) {
      if (overWriteShared || jobMeta.findDatabase(databaseMeta.getName()) == null) {
        if (databaseMeta.getName() != null) {
          databaseMeta.shareVariablesWith(jobMeta);
          jobMeta.addOrReplaceDatabase(databaseMeta);
          if (!overWriteShared)
            databaseMeta.setChanged(false);
        }
      }
    }
    jobMeta.clearChanged();
  }

  /**
   * Add the slave servers in the repository to this job if they are not yet present.
   * @param JobMeta The job to load into.
   * @param overWriteShared if an object with the same name exists, overwrite
   */
  protected void readSlaves(JobMeta jobMeta, boolean overWriteShared, List<SlaveServer> slaveServers) {
    for (SlaveServer slaveServer : slaveServers) {
      if (overWriteShared || jobMeta.findSlaveServer(slaveServer.getName()) == null) {
        if (!Const.isEmpty(slaveServer.getName())) {
          slaveServer.shareVariablesWith(jobMeta);
          jobMeta.addOrReplaceSlaveServer(slaveServer);
          if (!overWriteShared)
            slaveServer.setChanged(false);
        }
      }
    }
  }

}

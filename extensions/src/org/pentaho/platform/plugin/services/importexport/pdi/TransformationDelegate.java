/**
 * The Pentaho proprietary code is licensed under the terms and conditions
 * of the software license agreement entered into between the entity licensing
 * such code and Pentaho Corporation. 
 */
package org.pentaho.platform.plugin.services.importexport.pdi;

import java.util.List;
import java.util.Map;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginLoaderException;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryAttributeInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNodeRef;
import org.pentaho.platform.repository.RepositoryFilenameUtils;

public class TransformationDelegate extends AbstractDelegate implements  java.io.Serializable {

  private static final long serialVersionUID = 3766852226384368923L; /* EESOURCE: UPDATE SERIALVERUID */

  private static final String PROP_STEP_ERROR_HANDLING_MIN_PCT_ROWS = "step_error_handling_min_pct_rows";//$NON-NLS-1$

  private static final String PROP_STEP_ERROR_HANDLING_MAX_PCT_ERRORS = "step_error_handling_max_pct_errors";//$NON-NLS-1$

  private static final String PROP_STEP_ERROR_HANDLING_MAX_ERRORS = "step_error_handling_max_errors";//$NON-NLS-1$

  private static final String PROP_STEP_ERROR_HANDLING_CODES_VALUENAME = "step_error_handling_codes_valuename";//$NON-NLS-1$

  private static final String PROP_STEP_ERROR_HANDLING_FIELDS_VALUENAME = "step_error_handling_fields_valuename";//$NON-NLS-1$

  private static final String PROP_STEP_ERROR_HANDLING_DESCRIPTIONS_VALUENAME = "step_error_handling_descriptions_valuename";//$NON-NLS-1$

  private static final String PROP_STEP_ERROR_HANDLING_NR_VALUENAME = "step_error_handling_nr_valuename";//$NON-NLS-1$

  private static final String PROP_STEP_ERROR_HANDLING_IS_ENABLED = "step_error_handling_is_enabled";//$NON-NLS-1$

  private static final String PROP_STEP_ERROR_HANDLING_TARGET_STEP = "step_error_handling_target_step";//$NON-NLS-1$

  private static final String PROP_LOG_SIZE_LIMIT = "LOG_SIZE_LIMIT";//$NON-NLS-1$

  private static final String PROP_LOG_INTERVAL = "LOG_INTERVAL";//$NON-NLS-1$

  private static final String PROP_TRANSFORMATION_TYPE = "TRANSFORMATION_TYPE";//$NON-NLS-1$

  private static final String PROP_STEP_PERFORMANCE_LOG_TABLE = "STEP_PERFORMANCE_LOG_TABLE";//$NON-NLS-1$

  private static final String PROP_STEP_PERFORMANCE_CAPTURING_DELAY = "STEP_PERFORMANCE_CAPTURING_DELAY";//$NON-NLS-1$

  private static final String PROP_STEP_PERFORMANCE_CAPTURING_SIZE_LIMIT = "STEP_PERFORMANCE_CAPTURING_SIZE_LIMIT";//$NON-NLS-1$

  private static final String PROP_CAPTURE_STEP_PERFORMANCE = "CAPTURE_STEP_PERFORMANCE";//$NON-NLS-1$

  private static final String PROP_SHARED_FILE = "SHARED_FILE";//$NON-NLS-1$

  private static final String PROP_USING_THREAD_PRIORITIES = "USING_THREAD_PRIORITIES";//$NON-NLS-1$

  private static final String PROP_FEEDBACK_SIZE = "FEEDBACK_SIZE";//$NON-NLS-1$

  private static final String PROP_FEEDBACK_SHOWN = "FEEDBACK_SHOWN";//$NON-NLS-1$

  private static final String PROP_UNIQUE_CONNECTIONS = "UNIQUE_CONNECTIONS";//$NON-NLS-1$

  private static final String PROP_ID_DIRECTORY = "ID_DIRECTORY";//$NON-NLS-1$

  private static final String PROP_SIZE_ROWSET = "SIZE_ROWSET";//$NON-NLS-1$

  private static final String PROP_MODIFIED_DATE = "MODIFIED_DATE";//$NON-NLS-1$

  private static final String PROP_MODIFIED_USER = "MODIFIED_USER";//$NON-NLS-1$

  private static final String PROP_CREATED_DATE = "CREATED_DATE";//$NON-NLS-1$

  private static final String PROP_CREATED_USER = "CREATED_USER";//$NON-NLS-1$

  private static final String PROP_DIFF_MAXDATE = "DIFF_MAXDATE";//$NON-NLS-1$

  private static final String PROP_OFFSET_MAXDATE = "OFFSET_MAXDATE";//$NON-NLS-1$

  private static final String PROP_FIELD_NAME_MAXDATE = "FIELD_NAME_MAXDATE";//$NON-NLS-1$

  private static final String PROP_TABLE_NAME_MAXDATE = "TABLE_NAME_MAXDATE";//$NON-NLS-1$

  private static final String PROP_ID_DATABASE_MAXDATE = "ID_DATABASE_MAXDATE";//$NON-NLS-1$

  private static final String PROP_USE_LOGFIELD = "USE_LOGFIELD";//$NON-NLS-1$

  private static final String PROP_USE_BATCHID = "USE_BATCHID";//$NON-NLS-1$

  private static final String PROP_TABLE_NAME_LOG = "TABLE_NAME_LOG";//$NON-NLS-1$

  private static final String PROP_DATABASE_LOG = "DATABASE_LOG";//$NON-NLS-1$

  private static final String PROP_STEP_REJECTED = "STEP_REJECTED";//$NON-NLS-1$

  private static final String PROP_STEP_UPDATE = "STEP_UPDATE";//$NON-NLS-1$

  private static final String PROP_STEP_OUTPUT = "STEP_OUTPUT";//$NON-NLS-1$

  private static final String PROP_STEP_INPUT = "STEP_INPUT";//$NON-NLS-1$

  private static final String PROP_STEP_WRITE = "STEP_WRITE";//$NON-NLS-1$

  private static final String PROP_STEP_READ = "STEP_READ";//$NON-NLS-1$

  private static final String PROP_TRANS_STATUS = "TRANS_STATUS";//$NON-NLS-1$

  private static final String PROP_TRANS_VERSION = "TRANS_VERSION";//$NON-NLS-1$

  private static final String PROP_EXTENDED_DESCRIPTION = "EXTENDED_DESCRIPTION";//$NON-NLS-1$

  private static final String PROP_NR_PARAMETERS = "NR_PARAMETERS";//$NON-NLS-1$

  private static final String NODE_PARAMETERS = "parameters";//$NON-NLS-1$

  private static final String PROP_NR_HOPS = "NR_HOPS";//$NON-NLS-1$

  private static final String PROP_NR_NOTES = "NR_NOTES";//$NON-NLS-1$

  private static final String NODE_NOTES = "notes";//$NON-NLS-1$

  private static final String NODE_HOPS = "hops";//$NON-NLS-1$

  private static final String PROP_STEP_ERROR_HANDLING_SOURCE_STEP = "step_error_handling_source_step";//$NON-NLS-1$

  private static final String NODE_PARTITIONER_CUSTOM = "partitionerCustom";//$NON-NLS-1$

  private static final String PROP_PARTITIONING_SCHEMA = "PARTITIONING_SCHEMA";//$NON-NLS-1$

  private static final String PROP_PARTITIONING_METHOD = "PARTITIONING_METHOD";//$NON-NLS-1$

  private static final String PROP_CLUSTER_SCHEMA = "cluster_schema";//$NON-NLS-1$

  private static final String NODE_STEP_CUSTOM = "custom";//$NON-NLS-1$

  private static Class<?> PKG = TransformationDelegate.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private static final String PROP_STEP_DISTRIBUTE = "STEP_DISTRIBUTE";//$NON-NLS-1$

  private static final String PROP_STEP_GUI_DRAW = "STEP_GUI_DRAW";//$NON-NLS-1$

  private static final String PROP_STEP_GUI_LOCATION_Y = "STEP_GUI_LOCATION_Y";//$NON-NLS-1$

  private static final String PROP_STEP_GUI_LOCATION_X = "STEP_GUI_LOCATION_X";//$NON-NLS-1$

  private static final String PROP_STEP_COPIES = "STEP_COPIES";//$NON-NLS-1$

  private static final String PROP_STEP_TYPE = "STEP_TYPE";//$NON-NLS-1$

  private static final String NODE_TRANS = "transformation";//$NON-NLS-1$

  private static final String EXT_STEP = ".kst";//$NON-NLS-1$

  private static final String NODE_STEPS = "steps";//$NON-NLS-1$

  private static final String PROP_XML = "XML";//$NON-NLS-1$

  private static final String NOTE_PREFIX = "__NOTE__#";//$NON-NLS-1$

  private static final String TRANS_HOP_FROM = "TRANS_HOP_FROM";//$NON-NLS-1$

  private static final String TRANS_HOP_TO = "TRANS_HOP_TO";//$NON-NLS-1$

  private static final String TRANS_HOP_ENABLED = "TRANS_HOP_ENABLED";//$NON-NLS-1$

  private static final String TRANS_HOP_PREFIX = "__TRANS_HOP__#";//$NON-NLS-1$

  private static final String TRANS_PARAM_PREFIX = "__TRANS_PARAM__#";//$NON-NLS-1$

  private static final String PARAM_KEY = "PARAM_KEY";//$NON-NLS-1$

  private static final String PARAM_DESC = "PARAM_DESC";//$NON-NLS-1$

  private static final String PARAM_DEFAULT = "PARAM_DEFAULT";//$NON-NLS-1$

  private Repository repo;

  public TransformationDelegate(final Repository repo) {
    super();
    this.repo = repo;
  }

  public RepositoryElementInterface dataNodeToElement(final DataNode rootNode) throws KettleException {
    TransMeta transMeta = new TransMeta();
    dataNodeToElement(rootNode, transMeta);
    return transMeta;
  }

  public void dataNodeToElement(final DataNode rootNode, final RepositoryElementInterface element)
      throws KettleException {
    TransMeta transMeta = (TransMeta) element;

    // read the steps...
    //
    DataNode stepsNode = rootNode.getNode(NODE_STEPS);
    for (DataNode stepNode : stepsNode.getNodes()) {

      StepMeta stepMeta = new StepMeta(new StringObjectId(stepNode.getId().toString()));
      stepMeta.setParentTransMeta(transMeta); // for tracing, retain hierarchy

      // Read the basics
      //
      stepMeta.setName(getString(stepNode, PROP_NAME));
      if (stepNode.hasProperty(PROP_DESCRIPTION)) {
        stepMeta.setDescription(getString(stepNode, PROP_DESCRIPTION));
      }
      stepMeta.setDistributes(stepNode.getProperty(PROP_STEP_DISTRIBUTE).getBoolean());
      stepMeta.setDraw(stepNode.getProperty(PROP_STEP_GUI_DRAW).getBoolean());
      stepMeta.setCopies((int) stepNode.getProperty(PROP_STEP_COPIES).getLong());

      int x = (int) stepNode.getProperty(PROP_STEP_GUI_LOCATION_X).getLong();
      int y = (int) stepNode.getProperty(PROP_STEP_GUI_LOCATION_Y).getLong();
      stepMeta.setLocation(x, y);

      String stepType = getString(stepNode, PROP_STEP_TYPE);

      // Create a new StepMetaInterface object...
      //
      PluginRegistry registry = PluginRegistry.getInstance();
      PluginInterface stepPlugin = registry.findPluginWithId(StepPluginType.class, stepType);
      
      StepMetaInterface stepMetaInterface = null;
      if (stepPlugin != null) {
        stepMetaInterface = (StepMetaInterface)registry.loadClass(stepPlugin);
        stepType = stepPlugin.getIds()[0]; // revert to the default in case we loaded an alternate version
      } else {
        throw new KettlePluginLoaderException(stepType, BaseMessages.getString(PKG, "StepMeta.Exception.UnableToLoadClass", stepType)); //$NON-NLS-1$
      }

      stepMeta.setStepID(stepType);

      // Read the metadata from the repository too...
      //
      RepositoryProxy proxy = new RepositoryProxy(stepNode.getNode(NODE_STEP_CUSTOM));
      stepMetaInterface.readRep(proxy, null, transMeta.getDatabases(), transMeta.getCounters());
      stepMeta.setStepMetaInterface(stepMetaInterface);

      // Get the partitioning as well...
      StepPartitioningMeta stepPartitioningMeta = new StepPartitioningMeta();
      if (stepNode.hasProperty(PROP_PARTITIONING_SCHEMA)) {
        String partSchemaId = stepNode.getProperty(PROP_PARTITIONING_SCHEMA).getRef().getId().toString();
        String schemaName = repo.loadPartitionSchema(new StringObjectId(partSchemaId), null).getName();

        stepPartitioningMeta.setPartitionSchemaName(schemaName);
        String methodCode = getString(stepNode, PROP_PARTITIONING_METHOD);
        stepPartitioningMeta.setMethod(StepPartitioningMeta.getMethod(methodCode));
        if (stepPartitioningMeta.getPartitioner() != null) {
          proxy = new RepositoryProxy(stepNode.getNode(NODE_PARTITIONER_CUSTOM));
          stepPartitioningMeta.getPartitioner().loadRep(proxy, null);
        }
        stepPartitioningMeta.hasChanged(true);
      }
      stepMeta.setStepPartitioningMeta(stepPartitioningMeta);

      stepMeta.getStepPartitioningMeta().setPartitionSchemaAfterLoading(transMeta.getPartitionSchemas());
      // Get the cluster schema name
      String clusterSchemaName = getString(stepNode, PROP_CLUSTER_SCHEMA);
      stepMeta.setClusterSchemaName(clusterSchemaName);
      if(clusterSchemaName != null && transMeta.getClusterSchemas() != null) {
    	  // Get the cluster schema from the given name
    	  for(ClusterSchema clusterSchema : transMeta.getClusterSchemas()) {
    		  if(clusterSchema.getName().equals(clusterSchemaName)) {
    			  stepMeta.setClusterSchema(clusterSchema);
    			  break;
    		  }
    	  }
      }

      transMeta.addStep(stepMeta);

    }

    for (DataNode stepNode : stepsNode.getNodes()) {

      ObjectId stepObjectId = new StringObjectId(stepNode.getId().toString());
      StepMeta stepMeta = StepMeta.findStep(transMeta.getSteps(), stepObjectId);
      
      // Also load the step error handling metadata
      //
      if (stepNode.hasProperty(PROP_STEP_ERROR_HANDLING_SOURCE_STEP)) {
        StepErrorMeta meta = new StepErrorMeta(transMeta, stepMeta);
        meta.setTargetStep(StepMeta.findStep(transMeta.getSteps(), stepNode.getProperty(
            PROP_STEP_ERROR_HANDLING_TARGET_STEP).getString()));
        meta.setEnabled(stepNode.getProperty(PROP_STEP_ERROR_HANDLING_IS_ENABLED).getBoolean());
        meta.setNrErrorsValuename(getString(stepNode, PROP_STEP_ERROR_HANDLING_NR_VALUENAME));
        meta.setErrorDescriptionsValuename(getString(stepNode, PROP_STEP_ERROR_HANDLING_DESCRIPTIONS_VALUENAME));
        meta.setErrorFieldsValuename(getString(stepNode, PROP_STEP_ERROR_HANDLING_FIELDS_VALUENAME));
        meta.setErrorCodesValuename(getString(stepNode, PROP_STEP_ERROR_HANDLING_CODES_VALUENAME));
        meta.setMaxErrors(getString(stepNode, PROP_STEP_ERROR_HANDLING_MAX_ERRORS));
        meta.setMaxPercentErrors(getString(stepNode, PROP_STEP_ERROR_HANDLING_MAX_PCT_ERRORS));
        meta.setMinPercentRows(getString(stepNode, PROP_STEP_ERROR_HANDLING_MIN_PCT_ROWS));
        meta.getSourceStep().setStepErrorMeta(meta); // a bit of a trick, I know.                        
      }
    }
    
    // Have all StreamValueLookups, etc. reference the correct source steps...
    //
    for (int i = 0; i < transMeta.nrSteps(); i++)
    {
        StepMeta stepMeta = transMeta.getStep(i);
        StepMetaInterface sii = stepMeta.getStepMetaInterface();
        if (sii != null) sii.searchInfoAndTargetSteps(transMeta.getSteps());
    }
    
    // Read the notes...
    //
    DataNode notesNode = rootNode.getNode(NODE_NOTES);
    int nrNotes = (int) notesNode.getProperty(PROP_NR_NOTES).getLong();
    for (DataNode noteNode : notesNode.getNodes()) {
      String xml = getString(noteNode, PROP_XML);
      transMeta.addNote(new NotePadMeta(XMLHandler.getSubNode(XMLHandler.loadXMLString(xml), NotePadMeta.XML_TAG)));
    }
    if (transMeta.nrNotes() != nrNotes) {
      throw new KettleException("The number of notes read [" + transMeta.nrNotes()
          + "] was not the number we expected [" + nrNotes + "]");
    }

    // Read the hops...
    //
    DataNode hopsNode = rootNode.getNode(NODE_HOPS);
    int nrHops = (int) hopsNode.getProperty(PROP_NR_HOPS).getLong();
    for (DataNode hopNode : hopsNode.getNodes()) {
      String stepFromName = getString(hopNode, TRANS_HOP_FROM);
      String stepToName = getString(hopNode, TRANS_HOP_TO);
      boolean enabled = true;
      if (hopNode.hasProperty(TRANS_HOP_ENABLED)) {
        enabled = hopNode.getProperty(TRANS_HOP_ENABLED).getBoolean();
      }

      StepMeta stepFrom = StepMeta.findStep(transMeta.getSteps(), stepFromName);
      StepMeta stepTo = StepMeta.findStep(transMeta.getSteps(), stepToName);
      
      // Make sure to only accept valid hops PDI-5519
      //
      if (stepFrom!=null && stepTo!=null) {
        transMeta.addTransHop(new TransHopMeta(stepFrom, stepTo, enabled));
      }

    }
    if (transMeta.nrTransHops() != nrHops) {
      throw new KettleException("The number of hops read [" + transMeta.nrTransHops()
          + "] was not the number we expected [" + nrHops + "]");
    }

    // Load the details at the end, to make sure we reference the databases correctly, etc.
    //
    loadTransformationDetails(rootNode, transMeta);

    transMeta.eraseParameters();

    DataNode paramsNode = rootNode.getNode(NODE_PARAMETERS);

    int count = (int) paramsNode.getProperty(PROP_NR_PARAMETERS).getLong();
    for (int idx = 0; idx < count; idx++) {
      DataNode paramNode = paramsNode.getNode(TRANS_PARAM_PREFIX + idx);
      String key = getString(paramNode, PARAM_KEY);
      String def = getString(paramNode, PARAM_DEFAULT);
      String desc = getString(paramNode, PARAM_DESC);
      transMeta.addParameterDefinition(key, def, desc);
    }
  }

  protected void loadTransformationDetails(final DataNode rootNode, final TransMeta transMeta) throws KettleException {
    transMeta.setExtendedDescription(getString(rootNode, PROP_EXTENDED_DESCRIPTION));
    transMeta.setTransversion(getString(rootNode, PROP_TRANS_VERSION));
    transMeta.setTransstatus((int) rootNode.getProperty(PROP_TRANS_STATUS).getLong());

    if (rootNode.hasProperty(PROP_STEP_READ)) {
      transMeta.getTransLogTable().setStepRead(
          StepMeta.findStep(transMeta.getSteps(), getString(rootNode, PROP_STEP_READ)));
    }
    if (rootNode.hasProperty(PROP_STEP_WRITE)) {
      transMeta.getTransLogTable().setStepWritten(
          StepMeta.findStep(transMeta.getSteps(), getString(rootNode, PROP_STEP_WRITE)));
    }
    if (rootNode.hasProperty(PROP_STEP_INPUT)) {
      transMeta.getTransLogTable().setStepInput(
          StepMeta.findStep(transMeta.getSteps(), getString(rootNode, PROP_STEP_INPUT)));
    }
    if (rootNode.hasProperty(PROP_STEP_OUTPUT)) {
      transMeta.getTransLogTable().setStepOutput(
          StepMeta.findStep(transMeta.getSteps(), getString(rootNode, PROP_STEP_OUTPUT)));
    }
    if (rootNode.hasProperty(PROP_STEP_UPDATE)) {
      transMeta.getTransLogTable().setStepUpdate(
          StepMeta.findStep(transMeta.getSteps(), getString(rootNode, PROP_STEP_UPDATE)));
    }
    if (rootNode.hasProperty(PROP_STEP_REJECTED)) {
      transMeta.getTransLogTable().setStepRejected(
          StepMeta.findStep(transMeta.getSteps(), getString(rootNode, PROP_STEP_REJECTED)));
    }

    if (rootNode.hasProperty(PROP_DATABASE_LOG)) {
      String id = rootNode.getProperty(PROP_DATABASE_LOG).getRef().getId().toString();
      DatabaseMeta conn = DatabaseMeta.findDatabase(transMeta.getDatabases(), new StringObjectId(id));
      transMeta.getTransLogTable().setConnectionName(conn.getName());
    }
    transMeta.getTransLogTable().setTableName(getString(rootNode, PROP_TABLE_NAME_LOG));
    transMeta.getTransLogTable().setBatchIdUsed(rootNode.getProperty(PROP_USE_BATCHID).getBoolean());
    transMeta.getTransLogTable().setLogFieldUsed(rootNode.getProperty(PROP_USE_LOGFIELD).getBoolean());

    if (rootNode.hasProperty(PROP_ID_DATABASE_MAXDATE)) {
      String id = rootNode.getProperty(PROP_ID_DATABASE_MAXDATE).getRef().getId().toString();
      transMeta.setMaxDateConnection(DatabaseMeta.findDatabase(transMeta.getDatabases(), new StringObjectId(id)));
    }
    transMeta.setMaxDateTable(getString(rootNode, PROP_TABLE_NAME_MAXDATE));
    transMeta.setMaxDateField(getString(rootNode, PROP_FIELD_NAME_MAXDATE));
    transMeta.setMaxDateOffset(rootNode.getProperty(PROP_OFFSET_MAXDATE).getDouble());
    transMeta.setMaxDateDifference(rootNode.getProperty(PROP_DIFF_MAXDATE).getDouble());

    transMeta.setCreatedUser(getString(rootNode, PROP_CREATED_USER));
    transMeta.setCreatedDate(getDate(rootNode, PROP_CREATED_DATE));

    transMeta.setModifiedUser(getString(rootNode, PROP_MODIFIED_USER));
    transMeta.setModifiedDate(getDate(rootNode, PROP_MODIFIED_DATE));

    // Optional:
    transMeta.setSizeRowset(Const.ROWS_IN_ROWSET);
    long val_size_rowset = rootNode.getProperty(PROP_SIZE_ROWSET).getLong();
    if (val_size_rowset > 0) {
      transMeta.setSizeRowset((int) val_size_rowset);
    }

    if (rootNode.hasProperty(PROP_ID_DIRECTORY)) {
      String id_directory = getString(rootNode, PROP_ID_DIRECTORY);
      if (log.isDetailed())
        log.logDetailed(toString(), PROP_ID_DIRECTORY + "=" + id_directory); //$NON-NLS-1$
      // Set right directory...
      transMeta.setRepositoryDirectory(repo.findDirectory(
          new StringObjectId(id_directory))); // always reload the folder structure
    }

    transMeta.setUsingUniqueConnections(rootNode.getProperty(PROP_UNIQUE_CONNECTIONS).getBoolean());
    boolean feedbackShown = true;
    if (rootNode.hasProperty(PROP_FEEDBACK_SHOWN)) {
      feedbackShown = rootNode.getProperty(PROP_FEEDBACK_SHOWN).getBoolean();
    }
    transMeta.setFeedbackShown(feedbackShown);
    transMeta.setFeedbackSize((int) rootNode.getProperty(PROP_FEEDBACK_SIZE).getLong());
    boolean usingThreadPriorityManagement = true;
    if (rootNode.hasProperty(PROP_USING_THREAD_PRIORITIES)) {
      usingThreadPriorityManagement = rootNode.getProperty(PROP_USING_THREAD_PRIORITIES).getBoolean();
    }
    transMeta.setUsingThreadPriorityManagment(usingThreadPriorityManagement);
    transMeta.setSharedObjectsFile(getString(rootNode, PROP_SHARED_FILE));
    String transTypeCode = getString(rootNode, PROP_TRANSFORMATION_TYPE);
    transMeta.setTransformationType(TransformationType.getTransformationTypeByCode(transTypeCode));

    // Performance monitoring for steps...
    //
    boolean capturingStepPerformanceSnapShots = true;
    if (rootNode.hasProperty(PROP_CAPTURE_STEP_PERFORMANCE)) {
      capturingStepPerformanceSnapShots = rootNode.getProperty(PROP_CAPTURE_STEP_PERFORMANCE).getBoolean();
    }
    transMeta.setCapturingStepPerformanceSnapShots(capturingStepPerformanceSnapShots);
    transMeta.setStepPerformanceCapturingDelay(getLong(rootNode, PROP_STEP_PERFORMANCE_CAPTURING_DELAY));
    transMeta.setStepPerformanceCapturingSizeLimit(getString(rootNode, PROP_STEP_PERFORMANCE_CAPTURING_SIZE_LIMIT));
    transMeta.getPerformanceLogTable().setTableName(getString(rootNode, PROP_STEP_PERFORMANCE_LOG_TABLE));
    transMeta.getTransLogTable().setLogSizeLimit(getString(rootNode, PROP_LOG_SIZE_LIMIT));
    
    // Load the logging tables too..
    //
    RepositoryAttributeInterface attributeInterface = new RepositoryAttribute(rootNode, transMeta.getDatabases());
    for (LogTableInterface logTable : transMeta.getLogTables()) {
      logTable.loadFromRepository(attributeInterface);
    }
  }

  public DataNode elementToDataNode(final RepositoryElementInterface element, final List<Character> reservedChars) throws KettleException {
    TransMeta transMeta = (TransMeta) element;

    DataNode rootNode = new DataNode(NODE_TRANS);

    DataNode stepsNode = rootNode.addNode(NODE_STEPS);

    // Also save all the steps in the transformation!
    //
    int stepNr = 0;
    for (StepMeta step : transMeta.getSteps()) {
      stepNr++;
      DataNode stepNode = stepsNode.addNode(RepositoryFilenameUtils.escape(step.getName(), reservedChars) + "_" + stepNr + EXT_STEP); //$NON-NLS-1$

      // Store the main data
      //
      stepNode.setProperty(PROP_NAME, step.getName());
      stepNode.setProperty(PROP_DESCRIPTION, step.getDescription());
      stepNode.setProperty(PROP_STEP_TYPE, step.getStepID());
      stepNode.setProperty(PROP_STEP_DISTRIBUTE, step.isDistributes());
      stepNode.setProperty(PROP_STEP_COPIES, step.getCopies());
      stepNode.setProperty(PROP_STEP_GUI_LOCATION_X, step.getLocation().x);
      stepNode.setProperty(PROP_STEP_GUI_LOCATION_Y, step.getLocation().y);
      stepNode.setProperty(PROP_STEP_GUI_DRAW, step.isDrawn());

      // Save the step metadata using the repository save method, NOT XML
      // That is because we want to keep the links to databases, conditions, etc by ID, not name.
      //
      StepMetaInterface stepMetaInterface = step.getStepMetaInterface();
      DataNode stepCustomNode = new DataNode(NODE_STEP_CUSTOM);
      Repository proxy = new RepositoryProxy(stepCustomNode);
      stepMetaInterface.saveRep(proxy, null, null);
      stepNode.addNode(stepCustomNode);

      // Save the partitioning information by reference as well...
      //
      StepPartitioningMeta partitioningMeta = step.getStepPartitioningMeta();
      if (partitioningMeta != null && partitioningMeta.getPartitionSchema() != null && partitioningMeta.isPartitioned()) {
        DataNodeRef ref = new DataNodeRef(partitioningMeta.getPartitionSchema().getObjectId().getId());
        stepNode.setProperty(PROP_PARTITIONING_SCHEMA, ref);
        stepNode.setProperty(PROP_PARTITIONING_METHOD, partitioningMeta.getMethodCode()); // method of partitioning
        if (partitioningMeta.getPartitioner() != null) {
          DataNode partitionerCustomNode = new DataNode(NODE_PARTITIONER_CUSTOM);
          proxy = new RepositoryProxy(partitionerCustomNode);
          partitioningMeta.getPartitioner().saveRep(proxy, null, null);
          stepNode.addNode(partitionerCustomNode);
        }
      }

      // Save the clustering information as well...
      //
      stepNode.setProperty(PROP_CLUSTER_SCHEMA, step.getClusterSchema() == null ? "" : step.getClusterSchema() //$NON-NLS-1$
          .getName());

      // Save the error hop metadata
      //
      StepErrorMeta stepErrorMeta = step.getStepErrorMeta();
      if (stepErrorMeta != null) {
        stepNode.setProperty(PROP_STEP_ERROR_HANDLING_SOURCE_STEP,
            stepErrorMeta.getSourceStep() != null ? stepErrorMeta.getSourceStep().getName() : ""); //$NON-NLS-1$
        stepNode.setProperty(PROP_STEP_ERROR_HANDLING_TARGET_STEP,
            stepErrorMeta.getTargetStep() != null ? stepErrorMeta.getTargetStep().getName() : ""); //$NON-NLS-1$
        stepNode.setProperty(PROP_STEP_ERROR_HANDLING_IS_ENABLED, stepErrorMeta.isEnabled());
        stepNode.setProperty(PROP_STEP_ERROR_HANDLING_NR_VALUENAME, stepErrorMeta.getNrErrorsValuename());
        stepNode.setProperty(PROP_STEP_ERROR_HANDLING_DESCRIPTIONS_VALUENAME, stepErrorMeta
            .getErrorDescriptionsValuename());
        stepNode.setProperty(PROP_STEP_ERROR_HANDLING_FIELDS_VALUENAME, stepErrorMeta.getErrorFieldsValuename());
        stepNode.setProperty(PROP_STEP_ERROR_HANDLING_CODES_VALUENAME, stepErrorMeta.getErrorCodesValuename());
        stepNode.setProperty(PROP_STEP_ERROR_HANDLING_MAX_ERRORS, stepErrorMeta.getMaxErrors());
        stepNode.setProperty(PROP_STEP_ERROR_HANDLING_MAX_PCT_ERRORS, stepErrorMeta.getMaxPercentErrors());
        stepNode.setProperty(PROP_STEP_ERROR_HANDLING_MIN_PCT_ROWS, stepErrorMeta.getMinPercentRows());
      }

    }

    // Save the notes
    //
    DataNode notesNode = rootNode.addNode(NODE_NOTES);
    notesNode.setProperty(PROP_NR_NOTES, transMeta.nrNotes());
    for (int i = 0; i < transMeta.nrNotes(); i++) {
      NotePadMeta note = transMeta.getNote(i);
      DataNode noteNode = notesNode.addNode(NOTE_PREFIX + i);

      noteNode.setProperty(PROP_XML, note.getXML());
    }

    // Finally, save the hops
    //
    DataNode hopsNode = rootNode.addNode(NODE_HOPS);
    hopsNode.setProperty(PROP_NR_HOPS, transMeta.nrTransHops());
    for (int i = 0; i < transMeta.nrTransHops(); i++) {
      TransHopMeta hop = transMeta.getTransHop(i);
      DataNode hopNode = hopsNode.addNode(TRANS_HOP_PREFIX + i);
      hopNode.setProperty(TRANS_HOP_FROM, hop.getFromStep().getName());
      hopNode.setProperty(TRANS_HOP_TO, hop.getToStep().getName());
      hopNode.setProperty(TRANS_HOP_ENABLED, hop.isEnabled());
    }

    // Parameters
    //
    String[] paramKeys = transMeta.listParameters();
    DataNode paramsNode = rootNode.addNode(NODE_PARAMETERS);
    paramsNode.setProperty(PROP_NR_PARAMETERS, paramKeys == null ? 0 : paramKeys.length);

    for (int idx = 0; idx < paramKeys.length; idx++) {
      DataNode paramNode = paramsNode.addNode(TRANS_PARAM_PREFIX + idx);
      String key = paramKeys[idx];
      String description = transMeta.getParameterDescription(paramKeys[idx]);
      String defaultValue = transMeta.getParameterDefault(paramKeys[idx]);

      paramNode.setProperty(PARAM_KEY, key != null ? key : ""); //$NON-NLS-1$
      paramNode.setProperty(PARAM_DEFAULT, defaultValue != null ? defaultValue : ""); //$NON-NLS-1$
      paramNode.setProperty(PARAM_DESC, description != null ? description : ""); //$NON-NLS-1$
    }

    // Let's not forget to save the details of the transformation itself.
    // This includes logging information, parameters, etc.
    //
    saveTransformationDetails(rootNode, transMeta);

    return rootNode;
  }

  private void saveTransformationDetails(final DataNode rootNode, final TransMeta transMeta) throws KettleException {

    rootNode.setProperty(PROP_EXTENDED_DESCRIPTION, transMeta.getExtendedDescription());
    rootNode.setProperty(PROP_TRANS_VERSION, transMeta.getTransversion());
    rootNode.setProperty(PROP_TRANS_STATUS, transMeta.getTransstatus() < 0 ? -1L : transMeta.getTransstatus());

    rootNode.setProperty(PROP_STEP_READ, transMeta.getTransLogTable().getStepnameRead());
    rootNode.setProperty(PROP_STEP_WRITE, transMeta.getTransLogTable().getStepnameWritten());
    rootNode.setProperty(PROP_STEP_INPUT, transMeta.getTransLogTable().getStepnameInput());
    rootNode.setProperty(PROP_STEP_OUTPUT, transMeta.getTransLogTable().getStepnameOutput());
    rootNode.setProperty(PROP_STEP_UPDATE, transMeta.getTransLogTable().getStepnameUpdated());
    rootNode.setProperty(PROP_STEP_REJECTED, transMeta.getTransLogTable().getStepnameRejected());

    if (transMeta.getTransLogTable().getDatabaseMeta() != null) {
      DataNodeRef ref = new DataNodeRef(transMeta.getTransLogTable().getDatabaseMeta().getObjectId().getId());
      rootNode.setProperty(PROP_DATABASE_LOG, ref);
    }

    rootNode.setProperty(PROP_TABLE_NAME_LOG, transMeta.getTransLogTable().getTableName());

    rootNode.setProperty(PROP_USE_BATCHID, Boolean.valueOf(transMeta.getTransLogTable().isBatchIdUsed()));
    rootNode.setProperty(PROP_USE_LOGFIELD, Boolean.valueOf(transMeta.getTransLogTable().isLogFieldUsed()));

    if (transMeta.getMaxDateConnection() != null) {
      DataNodeRef ref = new DataNodeRef(transMeta.getMaxDateConnection().getObjectId().getId());
      rootNode.setProperty(PROP_ID_DATABASE_MAXDATE, ref);
    }

    rootNode.setProperty(PROP_TABLE_NAME_MAXDATE, transMeta.getMaxDateTable());
    rootNode.setProperty(PROP_FIELD_NAME_MAXDATE, transMeta.getMaxDateField());
    rootNode.setProperty(PROP_OFFSET_MAXDATE, new Double(transMeta.getMaxDateOffset()));
    rootNode.setProperty(PROP_DIFF_MAXDATE, new Double(transMeta.getMaxDateDifference()));

    rootNode.setProperty(PROP_CREATED_USER, transMeta.getCreatedUser());
    rootNode.setProperty(PROP_CREATED_DATE, transMeta.getCreatedDate());

    rootNode.setProperty(PROP_MODIFIED_USER, transMeta.getModifiedUser());
    rootNode.setProperty(PROP_MODIFIED_DATE, transMeta.getModifiedDate());

    rootNode.setProperty(PROP_SIZE_ROWSET, transMeta.getSizeRowset());

    rootNode.setProperty(PROP_UNIQUE_CONNECTIONS, transMeta.isUsingUniqueConnections());
    rootNode.setProperty(PROP_FEEDBACK_SHOWN, transMeta.isFeedbackShown());
    rootNode.setProperty(PROP_FEEDBACK_SIZE, transMeta.getFeedbackSize());
    rootNode.setProperty(PROP_USING_THREAD_PRIORITIES, transMeta.isUsingThreadPriorityManagment());
    rootNode.setProperty(PROP_SHARED_FILE, transMeta.getSharedObjectsFile());

    rootNode.setProperty(PROP_CAPTURE_STEP_PERFORMANCE, transMeta.isCapturingStepPerformanceSnapShots());
    rootNode.setProperty(PROP_STEP_PERFORMANCE_CAPTURING_DELAY, transMeta.getStepPerformanceCapturingDelay());
    rootNode.setProperty(PROP_STEP_PERFORMANCE_CAPTURING_SIZE_LIMIT, transMeta.getStepPerformanceCapturingSizeLimit());
    rootNode.setProperty(PROP_STEP_PERFORMANCE_LOG_TABLE, transMeta.getPerformanceLogTable().getTableName());

    rootNode.setProperty(PROP_LOG_SIZE_LIMIT, transMeta.getTransLogTable().getLogSizeLimit());
    rootNode.setProperty(PROP_LOG_INTERVAL, transMeta.getTransLogTable().getLogInterval());

    rootNode.setProperty(PROP_TRANSFORMATION_TYPE, transMeta.getTransformationType().getCode());
    
    // Save the logging tables too..
    //
  	RepositoryAttributeInterface attributeInterface = new RepositoryAttribute(rootNode, transMeta.getDatabases());
  	for (LogTableInterface logTable : transMeta.getLogTables()) {
  	  logTable.saveToRepository(attributeInterface);
  	}
  }

  @SuppressWarnings("unchecked")
  public SharedObjects loadSharedObjects(final RepositoryElementInterface element,
      final Map<RepositoryObjectType, List<? extends SharedObjectInterface>> sharedObjectsByType) throws KettleException {
    TransMeta transMeta = (TransMeta) element;
    transMeta.setSharedObjects(transMeta.readSharedObjects());

    // Repository objects take priority so let's overwrite them...
    //
    readDatabases(transMeta, true, (List<DatabaseMeta>) sharedObjectsByType.get(RepositoryObjectType.DATABASE));
    readPartitionSchemas(transMeta, true, (List<PartitionSchema>) sharedObjectsByType.get(RepositoryObjectType.PARTITION_SCHEMA));
    readSlaves(transMeta, true, (List<SlaveServer>) sharedObjectsByType.get(RepositoryObjectType.SLAVE_SERVER));
    readClusters(transMeta, true, (List<ClusterSchema>) sharedObjectsByType.get(RepositoryObjectType.CLUSTER_SCHEMA));

    return transMeta.getSharedObjects();
  }

  /**
   * Insert all the databases from the repository into the TransMeta object, overwriting optionally
   * 
   * @param TransMeta The transformation to load into.
   * @param overWriteShared if an object with the same name exists, overwrite
   * @throws KettleException 
   */
  protected void readDatabases(TransMeta transMeta, boolean overWriteShared, List<DatabaseMeta> databaseMetas) {
    for (DatabaseMeta databaseMeta : databaseMetas) {
      if (overWriteShared || transMeta.findDatabase(databaseMeta.getName()) == null) {
        if (databaseMeta.getName() != null) {
          databaseMeta.shareVariablesWith(transMeta);
          transMeta.addOrReplaceDatabase(databaseMeta);
          if (!overWriteShared)
            databaseMeta.setChanged(false);
        }
      }
    }
    transMeta.clearChangedDatabases();
  }

  /**
   * Add clusters in the repository to this transformation if they are not yet present.
   * 
   * @param TransMeta The transformation to load into.
   * @param overWriteShared if an object with the same name exists, overwrite
   */
  protected void readClusters(TransMeta transMeta, boolean overWriteShared, List<ClusterSchema> clusterSchemas) {
    for (ClusterSchema clusterSchema : clusterSchemas) {
      if (overWriteShared || transMeta.findClusterSchema(clusterSchema.getName()) == null) {
        if (!Const.isEmpty(clusterSchema.getName())) {
          clusterSchema.shareVariablesWith(transMeta);
          transMeta.addOrReplaceClusterSchema(clusterSchema);
          if (!overWriteShared)
            clusterSchema.setChanged(false);
        }
      }
    }
  }

  /**
   * Add the partitions in the repository to this transformation if they are not yet present.
   * @param TransMeta The transformation to load into.
   * @param overWriteShared if an object with the same name exists, overwrite
   */
  protected void readPartitionSchemas(TransMeta transMeta, boolean overWriteShared, List<PartitionSchema> partitionSchemas) {
    for (PartitionSchema partitionSchema : partitionSchemas) {
      if (overWriteShared || transMeta.findPartitionSchema(partitionSchema.getName()) == null) {
        if (!Const.isEmpty(partitionSchema.getName())) {
          transMeta.addOrReplacePartitionSchema(partitionSchema);
          if (!overWriteShared)
            partitionSchema.setChanged(false);
        }
      }
    }
  }

  /**
   * Add the slave servers in the repository to this transformation if they are not yet present.
   * @param TransMeta The transformation to load into.
   * @param overWriteShared if an object with the same name exists, overwrite
   */
  protected void readSlaves(TransMeta transMeta, boolean overWriteShared, List<SlaveServer> slaveServers) {
    for (SlaveServer slaveServer : slaveServers) {
      if (overWriteShared || transMeta.findSlaveServer(slaveServer.getName()) == null) {
        if (!Const.isEmpty(slaveServer.getName())) {
          slaveServer.shareVariablesWith(transMeta);
          transMeta.addOrReplaceSlaveServer(slaveServer);
          if (!overWriteShared)
            slaveServer.setChanged(false);
        }
      }
    }
  }

  public void saveSharedObjects(final RepositoryElementInterface element, final String versionComment)
      throws KettleException {
    TransMeta transMeta = (TransMeta) element;
    // First store the databases and other depending objects in the transformation.
    //

    // Only store if the database has actually changed or doesn't have an object ID (imported)
    //
    for (DatabaseMeta databaseMeta : transMeta.getDatabases()) {
      if (databaseMeta.hasChanged() || databaseMeta.getObjectId() == null) {
        repo.save(databaseMeta, versionComment, null);
      }
    }

    // Store the slave servers...
    //
    for (SlaveServer slaveServer : transMeta.getSlaveServers()) {
      if (slaveServer.hasChanged() || slaveServer.getObjectId() == null) {
        repo.save(slaveServer, versionComment, null);
      }
    }

    // Store the cluster schemas
    //
    for (ClusterSchema clusterSchema : transMeta.getClusterSchemas()) {
      if (clusterSchema.hasChanged() || clusterSchema.getObjectId() == null) {
        repo.save(clusterSchema, versionComment, null);
      }
    }

    // Save the partition schemas
    //
    for (PartitionSchema partitionSchema : transMeta.getPartitionSchemas()) {
      if (partitionSchema.hasChanged() || partitionSchema.getObjectId() == null) {
        repo.save(partitionSchema, versionComment, null);
      }
    }

  }

}

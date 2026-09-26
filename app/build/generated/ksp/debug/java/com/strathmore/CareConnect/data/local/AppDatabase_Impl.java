package com.strathmore.CareConnect.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.strathmore.CareConnect.data.local.dao.CaregiverDao;
import com.strathmore.CareConnect.data.local.dao.CaregiverDao_Impl;
import com.strathmore.CareConnect.data.local.dao.PatientDao;
import com.strathmore.CareConnect.data.local.dao.PatientDao_Impl;
import com.strathmore.CareConnect.data.local.dao.RoleDao;
import com.strathmore.CareConnect.data.local.dao.RoleDao_Impl;
import com.strathmore.CareConnect.data.local.dao.SymptomDao;
import com.strathmore.CareConnect.data.local.dao.SymptomDao_Impl;
import com.strathmore.CareConnect.data.local.dao.UserDao;
import com.strathmore.CareConnect.data.local.dao.UserDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile RoleDao _roleDao;

  private volatile UserDao _userDao;

  private volatile CaregiverDao _caregiverDao;

  private volatile PatientDao _patientDao;

  private volatile SymptomDao _symptomDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `roles` (`roleId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`userId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `roleId` INTEGER NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `email` TEXT NOT NULL, `password` TEXT NOT NULL, `phoneNumber` TEXT NOT NULL, FOREIGN KEY(`roleId`) REFERENCES `roles`(`roleId`) ON UPDATE NO ACTION ON DELETE RESTRICT )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_users_roleId` ON `users` (`roleId`)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `caregivers` (`caregiverId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` INTEGER NOT NULL, `relationship` TEXT NOT NULL, FOREIGN KEY(`userId`) REFERENCES `users`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_caregivers_userId` ON `caregivers` (`userId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `patients` (`patientId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `caregiverId` INTEGER NOT NULL, `name` TEXT NOT NULL, `dateOfBirth` INTEGER NOT NULL, `medicalHistory` TEXT NOT NULL, `medication` TEXT NOT NULL, FOREIGN KEY(`caregiverId`) REFERENCES `caregivers`(`caregiverId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_patients_caregiverId` ON `patients` (`caregiverId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `symptoms` (`symptomId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `patientId` INTEGER NOT NULL, `type` TEXT NOT NULL, `rating` INTEGER NOT NULL, `date` INTEGER NOT NULL, `notes` TEXT NOT NULL, FOREIGN KEY(`patientId`) REFERENCES `patients`(`patientId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_symptoms_patientId` ON `symptoms` (`patientId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '15b4b58b4f4993db862515ae5db39b3b')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `roles`");
        db.execSQL("DROP TABLE IF EXISTS `users`");
        db.execSQL("DROP TABLE IF EXISTS `caregivers`");
        db.execSQL("DROP TABLE IF EXISTS `patients`");
        db.execSQL("DROP TABLE IF EXISTS `symptoms`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsRoles = new HashMap<String, TableInfo.Column>(3);
        _columnsRoles.put("roleId", new TableInfo.Column("roleId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoles.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoles.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRoles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRoles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRoles = new TableInfo("roles", _columnsRoles, _foreignKeysRoles, _indicesRoles);
        final TableInfo _existingRoles = TableInfo.read(db, "roles");
        if (!_infoRoles.equals(_existingRoles)) {
          return new RoomOpenHelper.ValidationResult(false, "roles(com.strathmore.CareConnect.data.local.entities.Role).\n"
                  + " Expected:\n" + _infoRoles + "\n"
                  + " Found:\n" + _existingRoles);
        }
        final HashMap<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(7);
        _columnsUsers.put("userId", new TableInfo.Column("userId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("roleId", new TableInfo.Column("roleId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("firstName", new TableInfo.Column("firstName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("lastName", new TableInfo.Column("lastName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("password", new TableInfo.Column("password", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("phoneNumber", new TableInfo.Column("phoneNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysUsers.add(new TableInfo.ForeignKey("roles", "RESTRICT", "NO ACTION", Arrays.asList("roleId"), Arrays.asList("roleId")));
        final HashSet<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(2);
        _indicesUsers.add(new TableInfo.Index("index_users_roleId", false, Arrays.asList("roleId"), Arrays.asList("ASC")));
        _indicesUsers.add(new TableInfo.Index("index_users_email", true, Arrays.asList("email"), Arrays.asList("ASC")));
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(db, "users");
        if (!_infoUsers.equals(_existingUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "users(com.strathmore.CareConnect.data.local.entities.User).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsCaregivers = new HashMap<String, TableInfo.Column>(3);
        _columnsCaregivers.put("caregiverId", new TableInfo.Column("caregiverId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCaregivers.put("userId", new TableInfo.Column("userId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCaregivers.put("relationship", new TableInfo.Column("relationship", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCaregivers = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysCaregivers.add(new TableInfo.ForeignKey("users", "CASCADE", "NO ACTION", Arrays.asList("userId"), Arrays.asList("userId")));
        final HashSet<TableInfo.Index> _indicesCaregivers = new HashSet<TableInfo.Index>(1);
        _indicesCaregivers.add(new TableInfo.Index("index_caregivers_userId", true, Arrays.asList("userId"), Arrays.asList("ASC")));
        final TableInfo _infoCaregivers = new TableInfo("caregivers", _columnsCaregivers, _foreignKeysCaregivers, _indicesCaregivers);
        final TableInfo _existingCaregivers = TableInfo.read(db, "caregivers");
        if (!_infoCaregivers.equals(_existingCaregivers)) {
          return new RoomOpenHelper.ValidationResult(false, "caregivers(com.strathmore.CareConnect.data.local.entities.Caregiver).\n"
                  + " Expected:\n" + _infoCaregivers + "\n"
                  + " Found:\n" + _existingCaregivers);
        }
        final HashMap<String, TableInfo.Column> _columnsPatients = new HashMap<String, TableInfo.Column>(6);
        _columnsPatients.put("patientId", new TableInfo.Column("patientId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatients.put("caregiverId", new TableInfo.Column("caregiverId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatients.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatients.put("dateOfBirth", new TableInfo.Column("dateOfBirth", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatients.put("medicalHistory", new TableInfo.Column("medicalHistory", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatients.put("medication", new TableInfo.Column("medication", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPatients = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysPatients.add(new TableInfo.ForeignKey("caregivers", "CASCADE", "NO ACTION", Arrays.asList("caregiverId"), Arrays.asList("caregiverId")));
        final HashSet<TableInfo.Index> _indicesPatients = new HashSet<TableInfo.Index>(1);
        _indicesPatients.add(new TableInfo.Index("index_patients_caregiverId", false, Arrays.asList("caregiverId"), Arrays.asList("ASC")));
        final TableInfo _infoPatients = new TableInfo("patients", _columnsPatients, _foreignKeysPatients, _indicesPatients);
        final TableInfo _existingPatients = TableInfo.read(db, "patients");
        if (!_infoPatients.equals(_existingPatients)) {
          return new RoomOpenHelper.ValidationResult(false, "patients(com.strathmore.CareConnect.data.local.entities.Patient).\n"
                  + " Expected:\n" + _infoPatients + "\n"
                  + " Found:\n" + _existingPatients);
        }
        final HashMap<String, TableInfo.Column> _columnsSymptoms = new HashMap<String, TableInfo.Column>(6);
        _columnsSymptoms.put("symptomId", new TableInfo.Column("symptomId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSymptoms.put("patientId", new TableInfo.Column("patientId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSymptoms.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSymptoms.put("rating", new TableInfo.Column("rating", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSymptoms.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSymptoms.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSymptoms = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysSymptoms.add(new TableInfo.ForeignKey("patients", "CASCADE", "NO ACTION", Arrays.asList("patientId"), Arrays.asList("patientId")));
        final HashSet<TableInfo.Index> _indicesSymptoms = new HashSet<TableInfo.Index>(1);
        _indicesSymptoms.add(new TableInfo.Index("index_symptoms_patientId", false, Arrays.asList("patientId"), Arrays.asList("ASC")));
        final TableInfo _infoSymptoms = new TableInfo("symptoms", _columnsSymptoms, _foreignKeysSymptoms, _indicesSymptoms);
        final TableInfo _existingSymptoms = TableInfo.read(db, "symptoms");
        if (!_infoSymptoms.equals(_existingSymptoms)) {
          return new RoomOpenHelper.ValidationResult(false, "symptoms(com.strathmore.CareConnect.data.local.entities.Symptom).\n"
                  + " Expected:\n" + _infoSymptoms + "\n"
                  + " Found:\n" + _existingSymptoms);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "15b4b58b4f4993db862515ae5db39b3b", "e16c5b5ffb0fbe5c8d7917c0bbb66b8d");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "roles","users","caregivers","patients","symptoms");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `users`");
      _db.execSQL("DELETE FROM `roles`");
      _db.execSQL("DELETE FROM `caregivers`");
      _db.execSQL("DELETE FROM `patients`");
      _db.execSQL("DELETE FROM `symptoms`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(RoleDao.class, RoleDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CaregiverDao.class, CaregiverDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PatientDao.class, PatientDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SymptomDao.class, SymptomDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public RoleDao roleDao() {
    if (_roleDao != null) {
      return _roleDao;
    } else {
      synchronized(this) {
        if(_roleDao == null) {
          _roleDao = new RoleDao_Impl(this);
        }
        return _roleDao;
      }
    }
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public CaregiverDao caregiverDao() {
    if (_caregiverDao != null) {
      return _caregiverDao;
    } else {
      synchronized(this) {
        if(_caregiverDao == null) {
          _caregiverDao = new CaregiverDao_Impl(this);
        }
        return _caregiverDao;
      }
    }
  }

  @Override
  public PatientDao patientDao() {
    if (_patientDao != null) {
      return _patientDao;
    } else {
      synchronized(this) {
        if(_patientDao == null) {
          _patientDao = new PatientDao_Impl(this);
        }
        return _patientDao;
      }
    }
  }

  @Override
  public SymptomDao symptomDao() {
    if (_symptomDao != null) {
      return _symptomDao;
    } else {
      synchronized(this) {
        if(_symptomDao == null) {
          _symptomDao = new SymptomDao_Impl(this);
        }
        return _symptomDao;
      }
    }
  }
}

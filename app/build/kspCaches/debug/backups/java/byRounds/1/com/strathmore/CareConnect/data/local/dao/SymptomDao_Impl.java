package com.strathmore.CareConnect.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.strathmore.CareConnect.data.local.entities.Symptom;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SymptomDao_Impl implements SymptomDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Symptom> __insertionAdapterOfSymptom;

  private final EntityDeletionOrUpdateAdapter<Symptom> __deletionAdapterOfSymptom;

  private final EntityDeletionOrUpdateAdapter<Symptom> __updateAdapterOfSymptom;

  public SymptomDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSymptom = new EntityInsertionAdapter<Symptom>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `symptoms` (`symptomId`,`patientId`,`type`,`rating`,`date`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Symptom entity) {
        statement.bindLong(1, entity.getSymptomId());
        statement.bindLong(2, entity.getPatientId());
        statement.bindString(3, entity.getType());
        statement.bindLong(4, entity.getRating());
        statement.bindLong(5, entity.getDate());
        statement.bindString(6, entity.getNotes());
      }
    };
    this.__deletionAdapterOfSymptom = new EntityDeletionOrUpdateAdapter<Symptom>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `symptoms` WHERE `symptomId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Symptom entity) {
        statement.bindLong(1, entity.getSymptomId());
      }
    };
    this.__updateAdapterOfSymptom = new EntityDeletionOrUpdateAdapter<Symptom>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `symptoms` SET `symptomId` = ?,`patientId` = ?,`type` = ?,`rating` = ?,`date` = ?,`notes` = ? WHERE `symptomId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Symptom entity) {
        statement.bindLong(1, entity.getSymptomId());
        statement.bindLong(2, entity.getPatientId());
        statement.bindString(3, entity.getType());
        statement.bindLong(4, entity.getRating());
        statement.bindLong(5, entity.getDate());
        statement.bindString(6, entity.getNotes());
        statement.bindLong(7, entity.getSymptomId());
      }
    };
  }

  @Override
  public Object insert(final Symptom symptom, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSymptom.insertAndReturnId(symptom);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Symptom symptom, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSymptom.handle(symptom);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Symptom symptom, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSymptom.handle(symptom);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Symptom>> getHistoryForPatient(final long patientId) {
    final String _sql = "SELECT * FROM symptoms WHERE patientId = ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, patientId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"symptoms"}, new Callable<List<Symptom>>() {
      @Override
      @NonNull
      public List<Symptom> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSymptomId = CursorUtil.getColumnIndexOrThrow(_cursor, "symptomId");
          final int _cursorIndexOfPatientId = CursorUtil.getColumnIndexOrThrow(_cursor, "patientId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<Symptom> _result = new ArrayList<Symptom>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Symptom _item;
            final long _tmpSymptomId;
            _tmpSymptomId = _cursor.getLong(_cursorIndexOfSymptomId);
            final long _tmpPatientId;
            _tmpPatientId = _cursor.getLong(_cursorIndexOfPatientId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final int _tmpRating;
            _tmpRating = _cursor.getInt(_cursorIndexOfRating);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new Symptom(_tmpSymptomId,_tmpPatientId,_tmpType,_tmpRating,_tmpDate,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getSevereSymptoms(final long patientId, final int minRating, final long sinceDate,
      final Continuation<? super List<Symptom>> $completion) {
    final String _sql = "SELECT * FROM symptoms WHERE patientId = ? AND rating >= ? AND date >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, patientId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, minRating);
    _argIndex = 3;
    _statement.bindLong(_argIndex, sinceDate);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Symptom>>() {
      @Override
      @NonNull
      public List<Symptom> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSymptomId = CursorUtil.getColumnIndexOrThrow(_cursor, "symptomId");
          final int _cursorIndexOfPatientId = CursorUtil.getColumnIndexOrThrow(_cursor, "patientId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<Symptom> _result = new ArrayList<Symptom>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Symptom _item;
            final long _tmpSymptomId;
            _tmpSymptomId = _cursor.getLong(_cursorIndexOfSymptomId);
            final long _tmpPatientId;
            _tmpPatientId = _cursor.getLong(_cursorIndexOfPatientId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final int _tmpRating;
            _tmpRating = _cursor.getInt(_cursorIndexOfRating);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new Symptom(_tmpSymptomId,_tmpPatientId,_tmpType,_tmpRating,_tmpDate,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

package com.strathmore.CareConnect.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.strathmore.CareConnect.data.local.entities.Caregiver;
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
public final class CaregiverDao_Impl implements CaregiverDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Caregiver> __insertionAdapterOfCaregiver;

  private final EntityDeletionOrUpdateAdapter<Caregiver> __deletionAdapterOfCaregiver;

  private final EntityDeletionOrUpdateAdapter<Caregiver> __updateAdapterOfCaregiver;

  public CaregiverDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCaregiver = new EntityInsertionAdapter<Caregiver>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `caregivers` (`caregiverId`,`userId`,`relationship`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Caregiver entity) {
        statement.bindLong(1, entity.getCaregiverId());
        statement.bindLong(2, entity.getUserId());
        statement.bindString(3, entity.getRelationship());
      }
    };
    this.__deletionAdapterOfCaregiver = new EntityDeletionOrUpdateAdapter<Caregiver>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `caregivers` WHERE `caregiverId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Caregiver entity) {
        statement.bindLong(1, entity.getCaregiverId());
      }
    };
    this.__updateAdapterOfCaregiver = new EntityDeletionOrUpdateAdapter<Caregiver>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `caregivers` SET `caregiverId` = ?,`userId` = ?,`relationship` = ? WHERE `caregiverId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Caregiver entity) {
        statement.bindLong(1, entity.getCaregiverId());
        statement.bindLong(2, entity.getUserId());
        statement.bindString(3, entity.getRelationship());
        statement.bindLong(4, entity.getCaregiverId());
      }
    };
  }

  @Override
  public Object insert(final Caregiver caregiver, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfCaregiver.insertAndReturnId(caregiver);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Caregiver caregiver, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfCaregiver.handle(caregiver);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Caregiver caregiver, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCaregiver.handle(caregiver);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getById(final long caregiverId, final Continuation<? super Caregiver> $completion) {
    final String _sql = "SELECT * FROM caregivers WHERE caregiverId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, caregiverId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Caregiver>() {
      @Override
      @Nullable
      public Caregiver call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCaregiverId = CursorUtil.getColumnIndexOrThrow(_cursor, "caregiverId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "relationship");
          final Caregiver _result;
          if (_cursor.moveToFirst()) {
            final long _tmpCaregiverId;
            _tmpCaregiverId = _cursor.getLong(_cursorIndexOfCaregiverId);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final String _tmpRelationship;
            _tmpRelationship = _cursor.getString(_cursorIndexOfRelationship);
            _result = new Caregiver(_tmpCaregiverId,_tmpUserId,_tmpRelationship);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getByUserId(final long userId, final Continuation<? super Caregiver> $completion) {
    final String _sql = "SELECT * FROM caregivers WHERE userId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Caregiver>() {
      @Override
      @Nullable
      public Caregiver call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCaregiverId = CursorUtil.getColumnIndexOrThrow(_cursor, "caregiverId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "relationship");
          final Caregiver _result;
          if (_cursor.moveToFirst()) {
            final long _tmpCaregiverId;
            _tmpCaregiverId = _cursor.getLong(_cursorIndexOfCaregiverId);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final String _tmpRelationship;
            _tmpRelationship = _cursor.getString(_cursorIndexOfRelationship);
            _result = new Caregiver(_tmpCaregiverId,_tmpUserId,_tmpRelationship);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Caregiver>> getAll() {
    final String _sql = "SELECT * FROM caregivers";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"caregivers"}, new Callable<List<Caregiver>>() {
      @Override
      @NonNull
      public List<Caregiver> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCaregiverId = CursorUtil.getColumnIndexOrThrow(_cursor, "caregiverId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "relationship");
          final List<Caregiver> _result = new ArrayList<Caregiver>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Caregiver _item;
            final long _tmpCaregiverId;
            _tmpCaregiverId = _cursor.getLong(_cursorIndexOfCaregiverId);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final String _tmpRelationship;
            _tmpRelationship = _cursor.getString(_cursorIndexOfRelationship);
            _item = new Caregiver(_tmpCaregiverId,_tmpUserId,_tmpRelationship);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

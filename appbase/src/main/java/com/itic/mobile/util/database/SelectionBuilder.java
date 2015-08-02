/*
 * Modifications:
 * -Imported from AOSP frameworks/base/core/java/com/android/internal/content
 * -Changed package name
 */

package com.itic.mobile.util.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.itic.mobile.util.data.Lists;
import com.itic.mobile.util.data.Maps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Helper类用来构造selection子句,这个类不是线程安全的
 * @author Andrea Ji
 */
public class SelectionBuilder {
    private static final String TAG = "SelectionBuilder";

    private String mTable = null;
    private Map<String, String> mProjectionMap = Maps.newHashMap();
    private StringBuilder mSelection = new StringBuilder();
    private ArrayList<String> mSelectionArgs = Lists.newArrayList();
    private String mGroupBy = null;
    private String mHaving = null;

    /**
     * Reset any internal state, allowing this builder to be recycled.
     */
    public SelectionBuilder reset() {
        mTable = null;
        mGroupBy = null;
        mHaving = null;
        mSelection.setLength(0);
        mSelectionArgs.clear();
        return this;
    }

    /**
     * 构造查询字句
     */
    public SelectionBuilder where(String selection, String... selectionArgs) {
        if (TextUtils.isEmpty(selection)) {
            if (selectionArgs != null && selectionArgs.length > 0) {
                throw new IllegalArgumentException(
                        "Valid selection required when including arguments=");
            }

            // Shortcut when clause is empty
            return this;
        }

        if (mSelection.length() > 0) {
            mSelection.append(" AND ");
        }

        mSelection.append("(").append(selection).append(")");
        if (selectionArgs != null) {
            Collections.addAll(mSelectionArgs, selectionArgs);
        }

        return this;
    }

    /**
     *  构造groupby字句
     */
    public SelectionBuilder groupBy(String groupBy) {
        mGroupBy = groupBy;
        return this;
    }

    /**
     *  构造having字句
     */
    public SelectionBuilder having(String having) {
        mHaving = having;
        return this;
    }

    /**
     *  指定table
     */
    public SelectionBuilder table(String table) {
        mTable = table;
        return this;
    }

    /**
     * 构造JOIN ON查询
     */
    public SelectionBuilder table(String table, String... tableParams) {
        if (tableParams != null && tableParams.length > 0) {
            String[] parts = table.split("[?]", tableParams.length + 1);
            StringBuilder sb = new StringBuilder(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                sb.append('"').append(tableParams[i - 1]).append('"')
                        .append(parts[i]);
            }
            mTable = sb.toString();
        } else {
            mTable = table;
        }
        return this;
    }

    private void assertTable() {
        if (mTable == null) {
            throw new IllegalStateException("Table not specified");
        }
    }

    public SelectionBuilder mapToTable(String column, String table) {
        mProjectionMap.put(column, table + "." + column);
        return this;
    }

    public SelectionBuilder map(String fromColumn, String toClause) {
        mProjectionMap.put(fromColumn, toClause + " AS " + fromColumn);
        return this;
    }

    /**
     * 获取查询条件
     * @see #getSelectionArgs()
     */
    public String getSelection() {
        return mSelection.toString();
    }

    /**
     * 获取查询条件值
     * @see #getSelection()
     */
    public String[] getSelectionArgs() {
        return mSelectionArgs.toArray(new String[mSelectionArgs.size()]);
    }

    private void mapColumns(String[] columns) {
        for (int i = 0; i < columns.length; i++) {
            final String target = mProjectionMap.get(columns[i]);
            if (target != null) {
                columns[i] = target;
            }
        }
    }

    @Override
    public String toString() {
        return "SelectionBuilder[table=" + mTable + ", selection=" + getSelection()
                + ", selectionArgs=" + Arrays.toString(getSelectionArgs())
                + "projectionMap = " + mProjectionMap + " ]";
    }

    /**
     * 执行查询
     */
    public Cursor query(SQLiteDatabase db, String[] columns, String orderBy) {
        return query(db, false, columns, orderBy, null);
    }

    /**
     * 执行查询
     */
    public Cursor query(SQLiteDatabase db, boolean distinct, String[] columns, String orderBy,
                        String limit) {
        assertTable();
        if (columns != null) mapColumns(columns);
        Log.v(TAG, "query(columns=" + Arrays.toString(columns)
                + ", distinct=" + distinct + ") " + this);
        return db.query(distinct, mTable, columns, getSelection(), getSelectionArgs(), mGroupBy,
                mHaving, orderBy, limit);
    }

    public Cursor query(SQLiteDatabase db, boolean distinct, String sql) {
        return null;
    }

    /**
     * 生成SQL
     */
    public String buildQueryString(boolean distinct, String[] columns, String orderBy, String limit) {
        return SQLiteQueryBuilder.buildQueryString(
                distinct, mTable, columns, getSelection(), mGroupBy, mHaving, orderBy, limit);
    }

    /**
     * 构造Union子查询SQL
     */
    public String buildUnionSubQueryString(
            boolean distinct, String typeDiscriminatorColumn,
            String[] unionColumns, Set<String> columnsPresentInTable,
            int computedColumnsOffset, String typeDiscriminatorValue,
            String selection, String groupBy, String having) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(mTable);
        builder.setDistinct(distinct);
        return builder.buildUnionSubQuery(typeDiscriminatorColumn, unionColumns, columnsPresentInTable, computedColumnsOffset, typeDiscriminatorValue, selection, groupBy, having);
    }

    /**
     * 构造Union查询SQL
     */
    public String buildUnionQueryString(boolean distinct, String[] subQueries, String sortOrder, String limit) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setDistinct(distinct);
        return builder.buildUnionQuery(subQueries, sortOrder, limit);
    }

    /**
     * 执行Union查询
     */
    public Cursor unionQuery(SQLiteDatabase db, String sql, String[] selectionArgs) {
        Collections.addAll(mSelectionArgs, selectionArgs);
        return db.rawQuery(sql, getSelectionArgs());
    }

    /**
     * 执行update操作
     */
    public int update(SQLiteDatabase db, ContentValues values) {
        assertTable();
        Log.v(TAG, "update() " + this);
        return db.update(mTable, values, getSelection(), getSelectionArgs());
    }

    /**
     * 执行delete操作
     */
    public int delete(SQLiteDatabase db) {
        assertTable();
        Log.v(TAG, "delete() " + this);
        return db.delete(mTable, getSelection(), getSelectionArgs());
    }
}
